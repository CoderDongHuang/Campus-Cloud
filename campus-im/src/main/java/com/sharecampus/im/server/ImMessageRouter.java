package com.sharecampus.im.server;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

/** IM 消息路由 — 在线转发 / 离线存储 */
@Component
public class ImMessageRouter {

    private static StringRedisTemplate redis;

    public ImMessageRouter(StringRedisTemplate redisTemplate) {
        ImMessageRouter.redis = redisTemplate;
    }

    /** 存离线消息到 Redis List */
    public static void saveOffline(Long userId, String message) {
        if (redis != null) {
            redis.opsForList().rightPush("im:offline:" + userId, message);
        }
    }
}
