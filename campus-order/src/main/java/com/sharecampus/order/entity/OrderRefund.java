package com.sharecampus.order.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("t_order_refund")
public class OrderRefund {
    @TableId(type = IdType.ASSIGN_ID)
    private Long refundId;
    private Long orderId;
    private Long tenantId;
    private String orderNo;
    private Long userId;
    private BigDecimal refundAmount;
    private String reason;
    private String status;   // PENDING/APPROVED/REJECTED/COMPLETED
    private LocalDateTime applyTime;
    private LocalDateTime auditTime;
    private LocalDateTime completeTime;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
