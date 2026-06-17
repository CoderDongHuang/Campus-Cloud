package com.sharecampus.order.state.impl;
import com.sharecampus.order.entity.Order;
import com.sharecampus.order.state.OrderState;
import com.sharecampus.order.state.OrderStatus;
import java.time.LocalDateTime;

public class PendingPayState implements OrderState {
    public OrderStatus getStatus() { return OrderStatus.PENDING_PAY; }
    public void pay(Order order) {
        order.setStatus(OrderStatus.PENDING_ACCEPT.name());
        order.setPaidTime(LocalDateTime.now());
    }
    public void cancel(Order order, String reason) {
        order.setStatus(OrderStatus.CANCELED.name());
        order.setCancelTime(LocalDateTime.now());
        order.setCancelReason(reason);
    }
}
