package com.sharecampus.settlement.service;

import com.sharecampus.common.mq.MqConstants;
import com.sharecampus.common.mq.MqMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * 结算事件消费者
 * <p>
 * 监听订单完成事件，自动创建分账单。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SettlementEventConsumer {

    private final SettlementService settlementService;

    /** 订单完成 → 创建分账单 */
    @RabbitListener(queues = MqConstants.SETTLEMENT_SETTLE_QUEUE)
    public void handleOrderCompleted(MqMessage msg) {
        log.info("收到结算事件: {}", msg.getData());
        try {
            // msg.data 格式: "orderId:orderNo:workerId:amount:categoryId"
            String[] parts = msg.getData().split(":");
            Long orderId = Long.valueOf(parts[0]);
            String orderNo = parts[1];
            Long workerId = Long.valueOf(parts[2]);
            BigDecimal amount = new BigDecimal(parts[3]);
            Long categoryId = parts.length > 4 ? Long.valueOf(parts[4]) : 1L;

            settlementService.createSettlement(orderId, orderNo, workerId, amount, categoryId);
            log.info("分账单创建成功: orderNo={}, workerAmount={}", orderNo, amount);
        } catch (Exception e) {
            log.error("结算处理失败: {}", msg.getData(), e);
            throw e;
        }
    }
}
