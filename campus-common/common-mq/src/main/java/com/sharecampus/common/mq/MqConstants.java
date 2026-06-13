package com.sharecampus.common.mq;

/**
 * RabbitMQ 交换机、队列、路由键常量
 * <p>
 * 所有服务使用统一的常量定义，避免硬编码字符串
 */
public final class MqConstants {

    private MqConstants() {}

    // ==================== 订单相关 ====================
    public static final String ORDER_EXCHANGE = "order.exchange";
    public static final String ORDER_CREATE_QUEUE = "order.create.queue";
    public static final String ORDER_CREATE_KEY = "order.create";
    public static final String ORDER_CANCEL_QUEUE = "order.cancel.queue";
    public static final String ORDER_CANCEL_KEY = "order.cancel";
    public static final String ORDER_DELAY_EXCHANGE = "order.delay.exchange";
    public static final String ORDER_DELAY_QUEUE = "order.delay.queue";

    // ==================== 优惠券相关 ====================
    public static final String COUPON_EXCHANGE = "coupon.exchange";
    public static final String COUPON_GRAB_QUEUE = "coupon.grab.queue";
    public static final String COUPON_GRAB_KEY = "coupon.grab.success";
    public static final String COUPON_BATCH_QUEUE = "coupon.batch.queue";
    public static final String COUPON_BATCH_KEY = "coupon.batch.send";

    // ==================== 支付相关 ====================
    public static final String PAYMENT_EXCHANGE = "payment.exchange";
    public static final String PAYMENT_CALLBACK_QUEUE = "payment.callback.queue";
    public static final String PAYMENT_CALLBACK_KEY = "payment.callback.#";

    // ==================== 结算相关 ====================
    public static final String SETTLEMENT_EXCHANGE = "settlement.exchange";
    public static final String SETTLEMENT_SETTLE_QUEUE = "settlement.settle.queue";
    public static final String SETTLEMENT_SETTLE_KEY = "settlement.settle";
    public static final String SETTLEMENT_WITHDRAW_QUEUE = "settlement.withdraw.queue";
    public static final String SETTLEMENT_WITHDRAW_KEY = "settlement.withdraw";

    // ==================== 通知相关 ====================
    public static final String NOTIFY_EXCHANGE = "notify.exchange";
    public static final String NOTIFY_SMS_QUEUE = "notify.sms.queue";
    public static final String NOTIFY_PUSH_QUEUE = "notify.push.queue";
    public static final String NOTIFY_INBOX_QUEUE = "notify.inbox.queue";

    // ==================== IM 相关 ====================
    public static final String IM_EXCHANGE = "im.exchange";
    public static final String IM_MESSAGE_QUEUE = "im.message.queue";
    public static final String IM_MESSAGE_KEY = "im.message.send";
    public static final String IM_SYSTEM_QUEUE = "im.system.queue";
    public static final String IM_SYSTEM_KEY = "im.system.notify";

    // ==================== 数据同步 ====================
    public static final String DB_SYNC_EXCHANGE = "db.sync.exchange";
    public static final String DB_SYNC_PRODUCT_QUEUE = "db.sync.product.queue";
    public static final String DB_SYNC_PRODUCT_KEY = "db.sync.product.#";
}
