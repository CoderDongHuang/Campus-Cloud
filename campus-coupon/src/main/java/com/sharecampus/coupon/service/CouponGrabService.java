package com.sharecampus.coupon.service;

import com.sharecampus.common.mq.MqConstants;
import com.sharecampus.common.mq.MqMessage;
import com.sharecampus.common.mq.MqSender;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 抢券服务 — Redis + Lua 原子扣库存
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CouponGrabService {

    private final StringRedisTemplate redisTemplate;
    private final MqSender mqSender;

    /**
     * Lua 脚本：原子扣库存 + 防重
     * KEYS[1] = coupon:stock:{templateId}
     * KEYS[2] = coupon:grabbed:{templateId}
     * ARGV[1] = userId
     * 返回值: 1=成功, -1=已抢光, -2=已抢过
     */
    private static final String GRAB_LUA = """
            local stock = redis.call('GET', KEYS[1])
            if not stock or tonumber(stock) <= 0 then
                return -1
            end
            local grabbed = redis.call('SISMEMBER', KEYS[2], ARGV[1])
            if grabbed == 1 then
                return -2
            end
            redis.call('DECR', KEYS[1])
            redis.call('SADD', KEYS[2], ARGV[1])
            return 1
            """;

    /** 抢券 */
    public int grab(Long templateId, Long userId) {
        String stockKey = "coupon:stock:" + templateId;
        String grabbedKey = "coupon:grabbed:" + templateId;

        DefaultRedisScript<Long> script = new DefaultRedisScript<>();
        script.setResultType(Long.class);
        script.setScriptText(GRAB_LUA);

        Long result = redisTemplate.execute(script, List.of(stockKey, grabbedKey),
                String.valueOf(userId));
        if (result == null) return -1;

        if (result == 1) {
            // 异步落库
            mqSender.send(MqConstants.COUPON_EXCHANGE, MqConstants.COUPON_GRAB_KEY,
                    MqMessage.of("coupon.grab", templateId + ":" + userId));
            log.info("抢券成功: templateId={}, userId={}", templateId, userId);
        } else if (result == -1) {
            log.debug("券已抢光: templateId={}", templateId);
        } else if (result == -2) {
            log.debug("用户已抢过: templateId={}, userId={}", templateId, userId);
        }
        return result.intValue();
    }

    /** 预热库存到 Redis */
    public void preloadStock(Long templateId, int stock) {
        redisTemplate.opsForValue().set("coupon:stock:" + templateId, String.valueOf(stock));
        redisTemplate.delete("coupon:grabbed:" + templateId);
        log.info("库存预热: templateId={}, stock={}", templateId, stock);
    }
}
