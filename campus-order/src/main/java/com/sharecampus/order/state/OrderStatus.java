package com.sharecampus.order.state;

/** 订单状态枚举 */
public enum OrderStatus {
    PENDING_PAY("待支付"),
    PENDING_ACCEPT("待接单"),
    IN_PROGRESS("服务中"),
    COMPLETED("已完成"),
    CANCELED("已取消"),
    REFUNDING("退款中"),
    REFUNDED("已退款");

    private final String desc;
    OrderStatus(String desc) { this.desc = desc; }
    public String getDesc() { return desc; }
}
