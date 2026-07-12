package com.sharecampus.im.service;

import com.sharecampus.common.mq.MqConstants;
import com.sharecampus.common.mq.MqMessage;
import com.sharecampus.common.security.UserContext;
import com.sharecampus.im.entity.ImMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class ImService {

    private final MongoTemplate mongoTemplate;
    private final StringRedisTemplate redisTemplate;

    /** MQ 消费：系统消息卡片 → IM消息持久化 + 推送 */
    @RabbitListener(queues = MqConstants.IM_SYSTEM_QUEUE)
    public void handleSystemCard(MqMessage msg) {
        try {
            // msg.data 格式: "orderNo:userId:title:content"
            String[] parts = msg.getData().split(":", 4);
            String orderNo = parts.length > 0 ? parts[0] : "";
            Long userId = parts.length > 1 ? Long.valueOf(parts[1]) : null;
            String title = parts.length > 2 ? parts[2] : "系统通知";
            String content = parts.length > 3 ? parts[3] : "";

            ImMessage imMsg = new ImMessage();
            imMsg.setSessionId(orderNo);
            imMsg.setSenderId(0L);
            imMsg.setSenderType("SYSTEM");
            imMsg.setMsgType("SYSTEM_CARD");
            imMsg.setContent(title + ": " + content);
            imMsg.setCreatedAt(new Date());
            mongoTemplate.insert(imMsg);

            // 离线消息推送到Redis
            saveOffline(userId, content);
            log.info("系统卡片已推送: userId={}, orderNo={}", userId, orderNo);
        } catch (Exception e) {
            log.error("系统卡片处理失败: {}", msg.getMessageId(), e);
        }
    }

    /** 保存消息到 MongoDB */
    public ImMessage saveMessage(ImMessage msg) {
        msg.setCreatedAt(new Date());
        return mongoTemplate.insert(msg);
    }

    /** 查询会话历史消息 */
    public List<ImMessage> getMessages(String sessionId, int page, int size) {
        Query query = Query.query(Criteria.where("sessionId").is(sessionId));
        query.skip((long) (page - 1) * size).limit(size);
        return mongoTemplate.find(query, ImMessage.class);
    }

    /** 拉取离线消息 */
    public List<String> pullOffline(Long userId) {
        String key = "im:offline:" + userId;
        return redisTemplate.opsForList().range(key, 0, -1);
    }

    /** 存储离线消息 */
    public void saveOffline(Long userId, String message) {
        String key = "im:offline:" + userId;
        redisTemplate.opsForList().rightPush(key, message);
    }

    /** 未读计数 +1 */
    public void incrUnread(Long userId, String sessionId) {
        redisTemplate.opsForHash().increment("im:unread:" + userId, sessionId, 1);
    }
}
