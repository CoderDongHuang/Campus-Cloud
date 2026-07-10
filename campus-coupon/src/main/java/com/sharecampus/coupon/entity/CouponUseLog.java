package com.sharecampus.coupon.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("coupon_use_log")
public class CouponUseLog {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private Long tenantId;
    private Long couponId;
    private Long templateId;
    private Long userId;
    private String orderNo;
    private String action;   // LOCK / USE / RETURN
    private LocalDateTime createTime;
    private Integer isDeleted;
}
