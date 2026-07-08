package com.sharecampus.order.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

/** 订单快照 — 下单时固化服务信息 JSON */
@Data
@TableName("t_order_snapshot")
public class OrderSnapshot {
    private Long snapshotId;
    private Long orderId;
    private Long tenantId;
    private String orderNo;
    private String snapshotData; // JSON
    private LocalDateTime createTime;
}
