package com.sharecampus.payment.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.sharecampus.common.mybatis.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("pay_order")
public class PayOrder extends BaseEntity {
    private String payOrderNo;
    private Long orderId;
    private String orderNo;
    private Long userId;
    private BigDecimal amount;
    private String channel;
    private String channelTradeNo;
    private String status;       // WAIT_PAY/PAYING/PAY_SUCCESS/PAY_FAIL/PAY_CLOSED
    private LocalDateTime payTime;
    private LocalDateTime expireTime;
    private String callbackData;
}
