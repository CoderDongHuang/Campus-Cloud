package com.sharecampus.im.service;

import com.sharecampus.common.security.UserContext;
import com.sharecampus.im.entity.ImMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ImService {

    private final MongoTemplate mongoTemplate;
    private final StringRedisTemplate redisTemplate;

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
