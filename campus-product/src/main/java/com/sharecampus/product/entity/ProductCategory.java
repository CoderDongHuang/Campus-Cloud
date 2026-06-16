package com.sharecampus.product.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.sharecampus.common.mybatis.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("product_category")
public class ProductCategory extends BaseEntity {
    private Long parentId;
    private String name;
    private Integer sortOrder;
    private String icon;
    private Integer status;
    @TableField(exist = false)
    private List<ProductCategory> children;
}
