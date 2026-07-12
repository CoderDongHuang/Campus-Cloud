package com.sharecampus.product.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("product_image")
public class ProductImage {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private Long tenantId;
    private Long spuId;
    private String imageUrl;
    private Integer sortOrder;
    private Integer isMain;
    private LocalDateTime createTime;
    private Integer isDeleted;
}
