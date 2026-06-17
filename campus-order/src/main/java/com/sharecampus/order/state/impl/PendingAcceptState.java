package com.sharecampus.order.state.impl;
import com.sharecampus.order.entity.Order;
import com.sharecampus.order.state.OrderState;
import com.sharecampus.order.state.OrderStatus;
import java.time.LocalDateTime;

public class PendingAcceptState implements OrderState {
    public OrderStatus getStatus() { return OrderStatus.PENDING_ACCEPT; }
    public void accept(Order order, Long workerId) {
        order.setStatus(OrderStatus.IN_PROGRESS.name());
        order.setWorkerId(workerId);
        order.setAcceptedTime(LocalDateTime.now());
    }
    public void cancel(Order order, String reason) {
        order.setStatus(OrderStatus.CANCELED.name());
        order.setCancelTime(LocalDateTime.now());
        order.setCancelReason(reason);
    }
}
