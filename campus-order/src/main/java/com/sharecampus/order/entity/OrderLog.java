package com.sharecampus.order.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("t_order_log")
public class OrderLog {
    @TableId(type = IdType.ASSIGN_ID)
    private Long logId;
    private Long orderId;
    private Long tenantId;
    private String orderNo;
    private String fromStatus;
    private String toStatus;
    private Long operatorId;
    private String remark;
    private LocalDateTime createTime;
}
