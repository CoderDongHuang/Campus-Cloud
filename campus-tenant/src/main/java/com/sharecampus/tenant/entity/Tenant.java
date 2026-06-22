package com.sharecampus.tenant.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDate;

@Data @TableName("tenant")
public class Tenant {
    private Long id;
    private String name;
    private String shortName;
    private String logo;
    private String contactName;
    private String contactPhone;
    private Long packageId;
    private Integer status;       // 0待审核 1正式 2试用 3欠费 4关闭
    private LocalDate expireTime;
    private java.time.LocalDateTime createTime;
}
