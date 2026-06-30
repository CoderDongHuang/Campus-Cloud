package com.sharecampus.review.entity;
import com.baomidou.mybatisplus.annotation.TableName;
import com.sharecampus.common.mybatis.BaseEntity;
import lombok.Data; import lombok.EqualsAndHashCode;
@Data @EqualsAndHashCode(callSuper=true) @TableName("order_review")
public class OrderReview extends BaseEntity {
    private Long orderId; private Long userId; private Long workerId;
    private Integer rating; private String content; private String images;
}
