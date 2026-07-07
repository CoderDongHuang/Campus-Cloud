package com.sharecampus.im.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Data
@Document(collection = "im_message")
@CompoundIndexes({
    @CompoundIndex(name = "idx_session_time", def = "{'sessionId': 1, 'createdAt': -1}"),
    @CompoundIndex(name = "idx_sender_time", def = "{'senderId': 1, 'createdAt': -1}")
})
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
