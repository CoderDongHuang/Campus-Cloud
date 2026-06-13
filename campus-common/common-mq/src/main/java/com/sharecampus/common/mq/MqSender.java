package com.sharecampus.common.mq;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

/**
 * MQ 消息发送工具
 * <p>
 * 封装 RabbitTemplate，提供带确认和日志的发送方法
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MqSender {

    private final RabbitTemplate rabbitTemplate;

    /** 发送普通消息 */
    public void send(String exchange, String routingKey, MqMessage message) {
        try {
            rabbitTemplate.convertAndSend(exchange, routingKey, message);
            log.debug("MQ 发送成功: exchange={}, key={}, msgId={}", exchange, routingKey, message.getMessageId());
        } catch (Exception e) {
            log.error("MQ 发送失败: exchange={}, key={}, msgId={}", exchange, routingKey, message.getMessageId(), e);
            throw e;
        }
    }

    /** 发送延迟消息（需先配置死信队列） */
    public void sendDelay(String exchange, String routingKey, MqMessage message, int delayMs) {
        rabbitTemplate.convertAndSend(exchange, routingKey, message, msg -> {
            msg.getMessageProperties().setExpiration(String.valueOf(delayMs));
            return msg;
        });
        log.debug("MQ 延迟消息: exchange={}, key={}, delay={}ms, msgId={}",
                exchange, routingKey, delayMs, message.getMessageId());
    }
}
