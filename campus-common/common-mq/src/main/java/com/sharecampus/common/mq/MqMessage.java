package com.sharecampus.common.mq;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * RabbitMQ 消息体
 * <p>
 * 所有 MQ 消息统一使用此包装，messageId 用于幂等去重
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MqMessage implements Serializable {

    /** 消息唯一 ID，用于幂等 */
    private String messageId;
    /** 消息类型（可选） */
    private String type;
    /** 业务数据（JSON 字符串） */
    private String data;
    /** 时间戳 */
    private long timestamp;
    /** 重试次数 */
    private int retryCount;

    public static MqMessage of(String type, String data) {
        return MqMessage.builder()
                .messageId(java.util.UUID.randomUUID().toString())
                .type(type)
                .data(data)
                .timestamp(System.currentTimeMillis())
                .retryCount(0)
                .build();
    }
}
