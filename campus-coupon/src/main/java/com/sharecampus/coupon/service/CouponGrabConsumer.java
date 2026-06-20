package com.sharecampus.coupon.service;

import com.sharecampus.common.mq.MqConstants;
import com.sharecampus.common.mq.MqMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/** 抢券异步落库消费者 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CouponGrabConsumer {

    private final CouponService couponService;

    @RabbitListener(queues = MqConstants.COUPON_GRAB_QUEUE)
    public void handleGrab(MqMessage msg) {
        String[] parts = msg.getData().split(":");
        Long templateId = Long.valueOf(parts[0]);
        Long userId = Long.valueOf(parts[1]);
        try {
            couponService.handleGrabSuccess(templateId, userId);
        } catch (Exception e) {
            log.error("抢券落库失败: templateId={}, userId={}", templateId, userId, e);
            throw e; // 抛异常触发重试
        }
    }
}
