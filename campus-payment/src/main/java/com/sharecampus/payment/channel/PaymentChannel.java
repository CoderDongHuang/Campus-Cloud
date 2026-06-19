package com.sharecampus.payment.channel;

import java.math.BigDecimal;
import java.util.Map;

/** 支付渠道接口（策略模式） */
public interface PaymentChannel {
    String channelName();
    /** 支付下单，返回渠道交易号 */
    String pay(String payOrderNo, BigDecimal amount, String desc);
    /** 查询支付状态 */
    String query(String channelTradeNo);
    /** 退款 */
    String refund(String channelTradeNo, BigDecimal amount, String reason);
    /** 回调验签 */
    boolean verifyCallback(Map<String, String> params);
}
