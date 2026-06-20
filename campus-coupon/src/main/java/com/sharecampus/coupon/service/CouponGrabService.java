package com.sharecampus.coupon.service;

import cn.hutool.core.util.IdUtil;
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
    private String scriptSha;

    /** 预加载 Lua 脚本 SHA */
    public void loadScript(String script) {
        this.scriptSha = redisTemplate.getConnectionFactory()
                .getConnection().scriptLoad(script.getBytes());
        log.info("Lua 脚本加载成功, SHA={}", scriptSha);
    }

    /** 抢券 */
    public int grab(Long templateId, Long userId) {
        String stockKey = "coupon:stock:" + templateId;
        String grabbedKey = "coupon:grabbed:" + templateId;

        DefaultRedisScript<Long> script = new DefaultRedisScript<>();
        script.setResultType(Long.class);
        script.setScriptText(scriptSha);

        Long result = redisTemplate.execute(script, List.of(stockKey, grabbedKey), userId, 1);
        if (result == null) return -1;

        if (result == 1) {
            // 异步落库
            mqSender.send(MqConstants.COUPON_EXCHANGE, MqConstants.COUPON_GRAB_KEY,
                    MqMessage.of("coupon.grab", templateId + ":" + userId));
            log.debug("抢券成功: templateId={}, userId={}", templateId, userId);
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
