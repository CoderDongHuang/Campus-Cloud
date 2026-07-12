package com.sharecampus.notify.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("user_message")
public class UserMessage {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private Long tenantId;
    private Long userId;
    private String title;
    private String content;
    private String msgType;
    private Integer isRead;
    private LocalDateTime readTime;
    private LocalDateTime createTime;
    private Integer isDeleted;
}
