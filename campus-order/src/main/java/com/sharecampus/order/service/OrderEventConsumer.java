package com.sharecampus.order.service;

import com.sharecampus.common.mq.MqConstants;
import com.sharecampus.common.mq.MqMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * 订单事件消费者
 * <p>
 * 监听支付成功等外部事件，驱动订单状态流转。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OrderEventConsumer {

    private final OrderService orderService;

    /** 支付成功 → 更新订单为已支付 */
    @RabbitListener(queues = MqConstants.PAYMENT_CALLBACK_QUEUE)
    public void handlePaymentSuccess(MqMessage msg) {
        String orderNo = msg.getData();
        log.info("收到支付成功事件: orderNo={}", orderNo);
        try {
            orderService.pay(orderNo);
            log.info("订单已支付: orderNo={}", orderNo);
        } catch (Exception e) {
            log.error("支付回调处理失败: orderNo={}", orderNo, e);
            throw e; // 抛异常触发MQ重试
        }
    }
}
