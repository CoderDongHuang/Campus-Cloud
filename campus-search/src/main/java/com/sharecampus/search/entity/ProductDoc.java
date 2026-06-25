package com.sharecampus.search.entity;

import lombok.Data;
import java.math.BigDecimal;

/** ES 商品文档 */
@Data
public class ProductDoc {
    private Long spuId;
    private Long tenantId;
    private String name;
    private String description;
    private String categoryName;
    private BigDecimal priceMin;
    private BigDecimal priceMax;
    private Float rating;
    private Integer salesCount;
    private Integer status;
}
