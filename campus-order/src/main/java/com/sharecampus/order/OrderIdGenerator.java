package com.sharecampus.order;

import cn.hutool.core.util.IdUtil;
import org.springframework.stereotype.Component;

/** 订单 ID 生成器 — 雪花算法，保证全局唯一 */
@Component
public class OrderIdGenerator {
    private final cn.hutool.core.lang.Snowflake snowflake;

    public OrderIdGenerator() {
        this.snowflake = IdUtil.getSnowflake(1, 1);
    }

    public long nextId() { return snowflake.nextId(); }

    /** 生成订单编号（业务号） */
    public String nextOrderNo() {
        return "ORD" + snowflake.nextIdStr();
    }
}
