package com.sharecampus.order.state;

import com.sharecampus.order.state.impl.*;

import java.util.Map;

/** 状态工厂 — 根据状态字符串返回对应实现 */
public class OrderStateFactory {
    private static final Map<String, OrderState> STATE_MAP = Map.of(
            "PENDING_PAY", new PendingPayState(),
            "PENDING_ACCEPT", new PendingAcceptState(),
            "IN_PROGRESS", new InProgressState(),
            "COMPLETED", new CompletedState(),
            "CANCELED", new CanceledState(),
            "REFUNDING", new RefundingState(),
            "REFUNDED", new RefundedState()
    );

    public static OrderState get(String status) {
        OrderState state = STATE_MAP.get(status);
        if (state == null) throw new IllegalArgumentException("未知订单状态: " + status);
        return state;
    }
}
