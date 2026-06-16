package com.sharecampus.product.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.sharecampus.common.mybatis.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("product_spu")
public class ProductSpu extends BaseEntity {
    private Long categoryId;
    private String name;
    private String description;
    private String mainImage;
    private Integer status;       // 1上架 0下架
    private Integer salesCount;
}
