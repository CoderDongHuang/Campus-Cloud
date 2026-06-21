package com.sharecampus.settlement.entity;
import com.baomidou.mybatisplus.annotation.TableName;
import com.sharecampus.common.mybatis.BaseEntity;
import lombok.Data; import lombok.EqualsAndHashCode;
import java.math.BigDecimal;
@Data @EqualsAndHashCode(callSuper=true) @TableName("settlement_config")
public class SettlementConfig extends BaseEntity {
    private Long categoryId;
    private BigDecimal platformRate;
    private BigDecimal workerRate;
    private Integer status;
}
