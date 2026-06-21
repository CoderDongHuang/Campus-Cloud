package com.sharecampus.settlement.entity;
import com.baomidou.mybatisplus.annotation.TableName;
import com.sharecampus.common.mybatis.BaseEntity;
import lombok.Data; import lombok.EqualsAndHashCode;
import java.math.BigDecimal; import java.time.LocalDateTime;
@Data @EqualsAndHashCode(callSuper=true) @TableName("settlement_order")
public class SettlementOrder extends BaseEntity {
    private Long orderId; private String orderNo; private Long userId;
    private BigDecimal totalAmount; private BigDecimal platformCommission;
    private BigDecimal workerAmount; private BigDecimal commissionRate;
    private String status; private LocalDateTime settledTime;
}
