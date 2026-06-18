package com.sharecampus.payment.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.sharecampus.common.mybatis.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("pay_refund")
public class PayRefund extends BaseEntity {
    private String refundNo;
    private String payOrderNo;
    private String orderNo;
    private BigDecimal refundAmount;
    private String reason;
    private String status;       // REFUNDING/REFUND_SUCCESS/REFUND_FAIL
    private String channelRefundNo;
}
