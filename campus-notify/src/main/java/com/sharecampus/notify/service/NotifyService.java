package com.sharecampus.notify.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sharecampus.common.mq.MqConstants;
import com.sharecampus.common.mq.MqMessage;
import com.sharecampus.notify.entity.MessageRecord;
import com.sharecampus.notify.entity.UserMessage;
import com.sharecampus.notify.mapper.MessageRecordMapper;
import com.sharecampus.notify.mapper.UserMessageMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotifyService {

    private final MessageRecordMapper recordMapper;
    private final UserMessageMapper userMessageMapper;
    private final StringRedisTemplate redisTemplate;

    /** MQ消费：订单通知 → 站内信入库（含频率限制：同用户同类消息5分钟内不重复） */
    @RabbitListener(queues = MqConstants.NOTIFY_INBOX_QUEUE)
    public void handleInbox(MqMessage msg) {
        try {
            String[] parts = msg.getData().split(":");
            String orderNo = parts.length > 0 ? parts[0] : "";
            Long userId = parts.length > 1 ? Long.valueOf(parts[1]) : null;
            if (userId == null) return;

            // 频率限制
            String rateKey = "notify:rate:" + userId + ":" + msg.getType();
            if (Boolean.FALSE.equals(redisTemplate.opsForValue()
                    .setIfAbsent(rateKey, "1", Duration.ofMinutes(5)))) {
                return;
            }

            String title = msg.getType().contains("paid") ? "订单支付成功"
                    : msg.getType().contains("completed") ? "服务已完成" : "订单通知";

            MessageRecord record = new MessageRecord();
            record.setUserId(userId);
            record.setType("INBOX");
            record.setTitle(title);
            record.setContent(orderNo);
            record.setStatus(1);
            recordMapper.insert(record);

            UserMessage um = new UserMessage();
            um.setUserId(userId);
            um.setTitle(title);
            um.setContent("订单 " + orderNo + " " + title);
            um.setMsgType("INBOX");
            um.setIsRead(0);
            userMessageMapper.insert(um);

            log.info("站内信已发送: userId={}, orderNo={}", userId, orderNo);
        } catch (Exception e) {
            log.error("站内信处理失败: {}", msg.getMessageId(), e);
            throw e;
        }
    }

    @RabbitListener(queues = MqConstants.NOTIFY_SMS_QUEUE)
    public void handleSms(MqMessage msg) {
        log.info("短信通知(待对接渠道): {}", msg.getData());
    }

    public List<MessageRecord> myMessages(Long userId) {
        return recordMapper.selectList(
                new LambdaQueryWrapper<MessageRecord>()
                        .eq(MessageRecord::getUserId, userId)
                        .orderByDesc(MessageRecord::getCreateTime));
    }

    public List<UserMessage> myInbox(Long userId, int page, int size) {
        return userMessageMapper.selectList(
                new LambdaQueryWrapper<UserMessage>()
                        .eq(UserMessage::getUserId, userId)
                        .orderByDesc(UserMessage::getCreateTime)
                        .last("LIMIT " + ((page - 1) * size) + "," + size));
    }
}
