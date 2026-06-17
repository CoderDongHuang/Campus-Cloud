package com.sharecampus.order.state.impl;
import com.sharecampus.order.entity.Order;
import com.sharecampus.order.state.OrderState;
import com.sharecampus.order.state.OrderStatus;
import java.time.LocalDateTime;

public class InProgressState implements OrderState {
    public OrderStatus getStatus() { return OrderStatus.IN_PROGRESS; }
    public void complete(Order order) {
        order.setStatus(OrderStatus.COMPLETED.name());
        order.setCompleteTime(LocalDateTime.now());
    }
    public void applyRefund(Order order) {
        order.setStatus(OrderStatus.REFUNDING.name());
    }
}
