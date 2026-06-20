package com.sharecampus.coupon.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.sharecampus.common.mybatis.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("user_coupon")
public class UserCoupon extends BaseEntity {
    private Long templateId;
    private Long userId;
    private String status;         // UNUSED/USED/EXPIRED
    private BigDecimal discountValue;
    private BigDecimal useThreshold;
    private LocalDateTime expireTime;
    private String usedOrderNo;
    private LocalDateTime usedTime;
}
