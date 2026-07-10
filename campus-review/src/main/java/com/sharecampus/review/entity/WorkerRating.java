package com.sharecampus.review.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("worker_rating")
public class WorkerRating {
    @TableId
    private Long workerId;
    private Integer totalScore;
    private Integer reviewCount;
    private BigDecimal avgRating;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
