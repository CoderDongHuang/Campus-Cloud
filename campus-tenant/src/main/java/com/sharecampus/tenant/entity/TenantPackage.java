package com.sharecampus.tenant.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("tenant_package")
public class TenantPackage {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private String name;
    private BigDecimal price;
    private Integer maxOrders;
    private Integer maxStorageMb;
    private Integer maxWorkers;
    private String features;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
