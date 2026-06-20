package com.sharecampus.coupon.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sharecampus.coupon.entity.CouponTemplate;
import com.sharecampus.coupon.entity.UserCoupon;
import com.sharecampus.coupon.mapper.CouponTemplateMapper;
import com.sharecampus.coupon.mapper.UserCouponMapper;
import com.sharecampus.common.mq.MqMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class CouponService {

    private final CouponTemplateMapper templateMapper;
    private final UserCouponMapper userCouponMapper;
    private final RedissonClient redissonClient;

    private final ThreadPoolExecutor executor = new ThreadPoolExecutor(
            10, 20, 60, TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(1000),
            new ThreadPoolExecutor.CallerRunsPolicy()
    );

    // ===== 券模板管理 =====

    public void createTemplate(CouponTemplate template) {
        templateMapper.insert(template);
    }

    public List<CouponTemplate> listTemplates() {
        return templateMapper.selectList(new LambdaQueryWrapper<CouponTemplate>()
                .eq(CouponTemplate::getStatus, 1));
    }

    // ===== 异步落库消费者 =====

    @Transactional
    public void handleGrabSuccess(Long templateId, Long userId) {
        CouponTemplate template = templateMapper.selectById(templateId);
        if (template == null) return;

        // 幂等检查
        Long count = userCouponMapper.selectCount(new LambdaQueryWrapper<UserCoupon>()
                .eq(UserCoupon::getTemplateId, templateId)
                .eq(UserCoupon::getUserId, userId));
        if (count > 0) return;

        // 扣 MySQL 库存
        templateMapper.decrStock(templateId);

        // 发券
        UserCoupon coupon = new UserCoupon();
        coupon.setTemplateId(templateId);
        coupon.setUserId(userId);
        coupon.setStatus("UNUSED");
        coupon.setDiscountValue(template.getDiscountValue());
        coupon.setUseThreshold(template.getUseThreshold());
        coupon.setExpireTime(LocalDateTime.now().plusDays(template.getValidDays()));
        userCouponMapper.insert(coupon);
    }

    // ===== 批量发券（分布式锁防重） =====

    public void batchSend(Long templateId, List<Long> userIds) {
        List<List<Long>> partitions = cn.hutool.core.collection.ListUtil.partition(userIds, 100);
        CountDownLatch latch = new CountDownLatch(partitions.size());

        for (List<Long> partition : partitions) {
            executor.submit(() -> {
                try {
                    for (Long userId : partition) {
                        sendOne(templateId, userId);
                    }
                } finally {
                    latch.countDown();
                }
            });
        }
        try { latch.await(); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
    }

    private void sendOne(Long templateId, Long userId) {
        RLock lock = redissonClient.getLock("coupon:send:" + templateId + ":" + userId);
        try {
            if (lock.tryLock(3, 10, TimeUnit.SECONDS)) {
                Long count = userCouponMapper.selectCount(new LambdaQueryWrapper<UserCoupon>()
                        .eq(UserCoupon::getTemplateId, templateId)
                        .eq(UserCoupon::getUserId, userId));
                if (count > 0) return;

                UserCoupon coupon = new UserCoupon();
                coupon.setTemplateId(templateId);
                coupon.setUserId(userId);
                coupon.setStatus("UNUSED");
                userCouponMapper.insert(coupon);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            if (lock.isHeldByCurrentThread()) lock.unlock();
        }
    }

    // ===== 我的优惠券 =====

    public List<UserCoupon> myCoupons(String status) {
        return userCouponMapper.selectList(new LambdaQueryWrapper<UserCoupon>()
                .eq(status != null, UserCoupon::getStatus, status)
                .orderByDesc(UserCoupon::getCreateTime));
    }
}
