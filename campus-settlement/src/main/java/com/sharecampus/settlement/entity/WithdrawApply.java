package com.sharecampus.settlement.entity;
import com.baomidou.mybatisplus.annotation.TableName;
import com.sharecampus.common.mybatis.BaseEntity;
import lombok.Data; import lombok.EqualsAndHashCode;
import java.math.BigDecimal; import java.time.LocalDateTime;
@Data @EqualsAndHashCode(callSuper=true) @TableName("withdraw_apply")
public class WithdrawApply extends BaseEntity {
    private String withdrawNo; private Long userId; private BigDecimal amount;
    private String channel; private String status;
    private LocalDateTime applyTime; private LocalDateTime auditTime; private LocalDateTime completeTime;
}
