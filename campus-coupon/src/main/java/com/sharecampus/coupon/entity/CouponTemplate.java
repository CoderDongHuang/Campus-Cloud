package com.sharecampus.coupon.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.sharecampus.common.mybatis.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("coupon_template")
public class CouponTemplate extends BaseEntity {
    private String name;
    private String type;           // FULL_REDUCTION/DISCOUNT/DIRECT_REDUCTION
    private BigDecimal useThreshold;
    private BigDecimal discountValue;
    private Integer totalStock;
    private String grabType;       // GRAB/SEND/RECEIVE
    private LocalDateTime grabStartTime;
    private LocalDateTime grabEndTime;
    private Integer userLimit;
    private Integer validDays;
    private Integer status;        // 1启用 0停用
}
