package com.sharecampus.common.mq;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** RabbitMQ 交换机/队列/绑定 自动声明 */
@Configuration
public class MqDeclareConfig {

    // ===== 订单 =====
    @Bean public TopicExchange orderExchange() { return new TopicExchange(MqConstants.ORDER_EXCHANGE); }
    @Bean public Queue orderCreateQueue() { return new Queue(MqConstants.ORDER_CREATE_QUEUE, true); }
    @Bean public Queue orderCancelQueue() { return new Queue(MqConstants.ORDER_CANCEL_QUEUE, true); }
    @Bean public Binding orderCreateBinding() { return BindingBuilder.bind(orderCreateQueue()).to(orderExchange()).with(MqConstants.ORDER_CREATE_KEY); }
    @Bean public Binding orderCancelBinding() { return BindingBuilder.bind(orderCancelQueue()).to(orderExchange()).with(MqConstants.ORDER_CANCEL_KEY); }

    // ===== 延迟取消 =====
    @Bean public TopicExchange orderDelayExchange() { return new TopicExchange(MqConstants.ORDER_DELAY_EXCHANGE); }
    @Bean public Queue orderDelayQueue() { return QueueBuilder.durable(MqConstants.ORDER_DELAY_QUEUE)
            .ttl(900000).deadLetterExchange(MqConstants.ORDER_EXCHANGE).deadLetterRoutingKey(MqConstants.ORDER_CANCEL_KEY).build(); }
    @Bean public Binding orderDelayBinding() { return BindingBuilder.bind(orderDelayQueue()).to(orderDelayExchange()).with("order.delay.close"); }

    // ===== 优惠券 =====
    @Bean public TopicExchange couponExchange() { return new TopicExchange(MqConstants.COUPON_EXCHANGE); }
    @Bean public Queue couponGrabQueue() { return new Queue(MqConstants.COUPON_GRAB_QUEUE, true); }
    @Bean public Queue couponBatchQueue() { return new Queue(MqConstants.COUPON_BATCH_QUEUE, true); }
    @Bean public Binding couponGrabBinding() { return BindingBuilder.bind(couponGrabQueue()).to(couponExchange()).with(MqConstants.COUPON_GRAB_KEY); }
    @Bean public Binding couponBatchBinding() { return BindingBuilder.bind(couponBatchQueue()).to(couponExchange()).with(MqConstants.COUPON_BATCH_KEY); }

    // ===== 支付 =====
    @Bean public TopicExchange paymentExchange() { return new TopicExchange(MqConstants.PAYMENT_EXCHANGE); }
    @Bean public Queue paymentCallbackQueue() { return new Queue(MqConstants.PAYMENT_CALLBACK_QUEUE, true); }
    @Bean public Binding paymentCallbackBinding() { return BindingBuilder.bind(paymentCallbackQueue()).to(paymentExchange()).with(MqConstants.PAYMENT_CALLBACK_KEY); }

    // ===== 通知 =====
    @Bean public FanoutExchange notifyExchange() { return new FanoutExchange(MqConstants.NOTIFY_EXCHANGE); }
    @Bean public Queue notifySmsQueue() { return new Queue(MqConstants.NOTIFY_SMS_QUEUE, true); }
    @Bean public Queue notifyPushQueue() { return new Queue(MqConstants.NOTIFY_PUSH_QUEUE, true); }
    @Bean public Queue notifyInboxQueue() { return new Queue(MqConstants.NOTIFY_INBOX_QUEUE, true); }
    @Bean public Binding notifySmsBinding() { return BindingBuilder.bind(notifySmsQueue()).to(notifyExchange()); }
    @Bean public Binding notifyPushBinding() { return BindingBuilder.bind(notifyPushQueue()).to(notifyExchange()); }
    @Bean public Binding notifyInboxBinding() { return BindingBuilder.bind(notifyInboxQueue()).to(notifyExchange()); }

    // ===== 数据同步 =====
    @Bean public TopicExchange dbSyncExchange() { return new TopicExchange(MqConstants.DB_SYNC_EXCHANGE); }
    @Bean public Queue dbSyncProductQueue() { return new Queue(MqConstants.DB_SYNC_PRODUCT_QUEUE, true); }
    @Bean public Binding dbSyncProductBinding() { return BindingBuilder.bind(dbSyncProductQueue()).to(dbSyncExchange()).with(MqConstants.DB_SYNC_PRODUCT_KEY); }
}
