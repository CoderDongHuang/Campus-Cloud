package com.sharecampus.notify.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sharecampus.common.mq.MqConstants;
import com.sharecampus.common.mq.MqMessage;
import com.sharecampus.notify.entity.MessageRecord;
import com.sharecampus.notify.mapper.MessageRecordMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotifyService {

    private final MessageRecordMapper recordMapper;

    /** MQ 消费：订单通知 → 站内信 */
    @RabbitListener(queues = MqConstants.NOTIFY_INBOX_QUEUE)
    public void handleInbox(MqMessage msg) {
        MessageRecord record = new MessageRecord();
        record.setUserId(1L); // 简化：从消息中解析
        record.setType("INBOX");
        record.setTitle("订单通知");
        record.setContent(msg.getData());
        record.setStatus(1);
        recordMapper.insert(record);
        log.debug("站内信已发送: {}", msg.getMessageId());
    }

    public List<MessageRecord> myMessages(Long userId) {
        return recordMapper.selectList(
                new LambdaQueryWrapper<MessageRecord>()
                        .eq(MessageRecord::getUserId, userId)
                        .orderByDesc(MessageRecord::getCreateTime));
    }
}
