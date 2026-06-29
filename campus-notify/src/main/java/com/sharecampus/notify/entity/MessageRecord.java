package com.sharecampus.notify.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.sharecampus.common.mybatis.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data @EqualsAndHashCode(callSuper=true) @TableName("message_record")
public class MessageRecord extends BaseEntity {
    private Long userId;
    private String type;      // SMS/PUSH/INBOX
    private String title;
    private String content;
    private String templateCode;
    private Integer status;   // 0待发送 1成功 2失败
}
