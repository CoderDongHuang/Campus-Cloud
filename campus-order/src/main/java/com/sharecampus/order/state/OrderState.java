package com.sharecampus.order.state;

import com.sharecampus.common.core.exception.BizException;
import com.sharecampus.common.core.exception.ErrorCode;
import com.sharecampus.order.entity.Order;

/** 订单状态接口 — 状态模式核心 */
public interface OrderState {
    OrderStatus getStatus();

    default void pay(Order order) { throw new BizException(ErrorCode.ORDER_STATUS_NOT_ALLOW); }
    default void accept(Order order, Long workerId) { throw new BizException(ErrorCode.ORDER_STATUS_NOT_ALLOW); }
    default void complete(Order order) { throw new BizException(ErrorCode.ORDER_STATUS_NOT_ALLOW); }
    default void cancel(Order order, String reason) { throw new BizException(ErrorCode.ORDER_STATUS_NOT_ALLOW); }
    default void applyRefund(Order order) { throw new BizException(ErrorCode.ORDER_STATUS_NOT_ALLOW); }
}
