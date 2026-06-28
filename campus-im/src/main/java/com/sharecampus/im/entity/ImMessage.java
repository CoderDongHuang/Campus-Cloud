package com.sharecampus.im.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Data
@Document(collection = "im_message")
public class ImMessage {
    @Id
    private String id;
    private String sessionId;
    private Long senderId;
    private String senderType;
    private String msgType;
    private String content;
    private Date createdAt;
}
