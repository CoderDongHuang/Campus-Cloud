package com.sharecampus.order.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单实体 — 分库分表
 * <p>
 * order_id 作为分表键，tenant_id 作为分库键
 */
@Data
@TableName("t_order")
public class Order {
    @TableId
    private Long orderId;
    private Long tenantId;
    private String orderNo;
    private Long userId;
    private Long skuId;
    private Long spuId;
    private Long addressId;
    private String status;
    private BigDecimal originalAmount;
    private BigDecimal discountAmount;
    private BigDecimal actualAmount;
    private Long couponId;
    private Long workerId;
    private LocalDateTime appointmentTime;
    private LocalDateTime paidTime;
    private LocalDateTime acceptedTime;
    private LocalDateTime completeTime;
    private LocalDateTime cancelTime;
    private String cancelReason;
    private Integer version;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    // ===== 状态委托 =====
    public void pay() { state().pay(this); }
    public void accept(Long workerId) { state().accept(this, workerId); }
    public void complete() { state().complete(this); }
    public void cancel(String reason) { state().cancel(this, reason); }
    public void applyRefund() { state().applyRefund(this); }

    private com.sharecampus.order.state.OrderState state() {
        return com.sharecampus.order.state.OrderStateFactory.get(this.status);
    }
}
