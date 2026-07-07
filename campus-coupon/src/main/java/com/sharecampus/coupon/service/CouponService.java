package com.sharecampus.coupon.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sharecampus.coupon.entity.CouponTemplate;
import com.sharecampus.coupon.entity.UserCoupon;
import com.sharecampus.coupon.mapper.CouponTemplateMapper;
import com.sharecampus.coupon.mapper.UserCouponMapper;
import com.sharecampus.common.mq.MqMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
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
    private final StringRedisTemplate redisTemplate;

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
        String lockKey = "coupon:send:" + templateId + ":" + userId;
        Boolean locked = redisTemplate.opsForValue()
                .setIfAbsent(lockKey, "1", Duration.ofSeconds(10));
        if (Boolean.FALSE.equals(locked)) return;

        try {
            Long count = userCouponMapper.selectCount(new LambdaQueryWrapper<UserCoupon>()
                    .eq(UserCoupon::getTemplateId, templateId)
                    .eq(UserCoupon::getUserId, userId));
            if (count > 0) return;
            UserCoupon coupon = new UserCoupon();
            coupon.setTemplateId(templateId);
            coupon.setUserId(userId);
            coupon.setStatus("UNUSED");
            userCouponMapper.insert(coupon);
        } finally {
            redisTemplate.delete(lockKey);
        }
    }

    // ===== 定时任务 =====

    /** 每天凌晨 2 点清理过期优惠券 */
    @org.springframework.scheduling.annotation.Scheduled(cron = "0 0 2 * * ?")
    public void expireOverdueCoupons() {
        log.info("定时任务: 清理过期优惠券...");
        java.util.List<UserCoupon> expiredCoupons = userCouponMapper.selectList(
            new LambdaQueryWrapper<UserCoupon>()
                .eq(UserCoupon::getStatus, "UNUSED")
                .lt(UserCoupon::getExpireTime, java.time.LocalDateTime.now())
        );
        for (UserCoupon coupon : expiredCoupons) {
            coupon.setStatus("EXPIRED");
            userCouponMapper.updateById(coupon);
        }
        log.info("定时任务完成: {} 张券已过期", expiredCoupons.size());
    }

    // ===== 我的优惠券 =====

    public List<UserCoupon> myCoupons(String status) {
        return userCouponMapper.selectList(new LambdaQueryWrapper<UserCoupon>()
                .eq(status != null, UserCoupon::getStatus, status)
                .orderByDesc(UserCoupon::getCreateTime));
    }
}
