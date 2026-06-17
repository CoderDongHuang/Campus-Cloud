package com.sharecampus.order.state.impl;
import com.sharecampus.order.entity.Order;
import com.sharecampus.order.state.OrderState;
import com.sharecampus.order.state.OrderStatus;

public class CanceledState implements OrderState {
    public OrderStatus getStatus() { return OrderStatus.CANCELED; }
}
