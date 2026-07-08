package com.sharecampus.order.service;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sharecampus.common.core.exception.BizException;
import com.sharecampus.common.core.exception.ErrorCode;
import com.sharecampus.common.mq.MqConstants;
import com.sharecampus.common.mq.MqMessage;
import com.sharecampus.common.mq.MqSender;
import com.sharecampus.common.security.UserContext;
import com.sharecampus.order.OrderIdGenerator;
import com.sharecampus.order.entity.Order;
import com.sharecampus.order.entity.OrderSnapshot;
import com.sharecampus.order.mapper.OrderMapper;
import com.sharecampus.order.mapper.OrderSnapshotMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderMapper orderMapper;
    private final OrderSnapshotMapper snapshotMapper;
    private final OrderIdGenerator idGenerator;
    private final StringRedisTemplate redisTemplate;
    private final MqSender mqSender;

    /** 创建订单 */
    @Transactional
    public Order createOrder(Order order, Long userId, Long tenantId) {
        // userId passed as parameter
        // tenantId passed as parameter
        long orderId = idGenerator.nextId();
        String orderNo = idGenerator.nextOrderNo();

        order.setOrderId(orderId);
        order.setTenantId(tenantId);
        order.setOrderNo(orderNo);
        order.setUserId(userId);
        order.setStatus("PENDING_PAY");
        order.setCreateTime(java.time.LocalDateTime.now());
        order.setUpdateTime(java.time.LocalDateTime.now());
        orderMapper.insert(order);

        // 生成快照
        saveSnapshot(order);
        // 缓存
        cacheOrderDetail(order);
        // 延迟消息：15 分钟后取消
        mqSender.sendDelay(MqConstants.ORDER_DELAY_EXCHANGE, "order.delay.close",
                MqMessage.of("order.delay", orderNo), 900000);

        log.info("订单创建成功: orderNo={}, userId={}", orderNo, userId);
        return order;
    }

    /** 支付 */
    @Transactional
    public void pay(String orderNo) {
        Order order = getByOrderNo(orderNo);
        order.pay();
        order.setUpdateTime(java.time.LocalDateTime.now());
        updateStatus(orderNo, order);
        redisTemplate.delete("order:detail:" + orderNo);
        // 通知下游：订单创建/支付完成
        mqSender.send(MqConstants.ORDER_EXCHANGE, MqConstants.ORDER_CREATE_KEY,
                MqMessage.of("order.create", orderNo));
        // 发通知："您的订单已支付成功"
        mqSender.send(MqConstants.NOTIFY_EXCHANGE, "notify.inbox",
                MqMessage.of("order.paid", orderNo + ":" + order.getUserId()));
    }

    /** 师傅接单 */
    @Transactional
    public void accept(String orderNo, Long workerId) {
        Order order = getByOrderNo(orderNo);
        order.accept(workerId);
        order.setUpdateTime(java.time.LocalDateTime.now());
        // 精准更新：状态 + worker_id + update_time
        com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<Order> uw =
            new com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<>();
        uw.eq(Order::getOrderNo, orderNo)
          .set(Order::getStatus, order.getStatus())
          .set(Order::getWorkerId, workerId)
          .set(Order::getUpdateTime, order.getUpdateTime());
        orderMapper.update(null, uw);
        redisTemplate.delete("order:detail:" + orderNo);
    }

    /** 完成服务 */
    @Transactional
    public void complete(String orderNo) {
        Order order = getByOrderNo(orderNo);
        order.complete();
        order.setUpdateTime(java.time.LocalDateTime.now());
        updateStatus(orderNo, order);
        redisTemplate.delete("order:detail:" + orderNo);
        // 触发结算
        String settleData = order.getOrderId() + ":" + orderNo + ":" +
                (order.getWorkerId() != null ? order.getWorkerId() : "0") + ":" +
                order.getActualAmount() + ":" + (order.getSpuId() != null ? order.getSpuId() : "1");
        mqSender.send(MqConstants.SETTLEMENT_EXCHANGE, MqConstants.SETTLEMENT_SETTLE_KEY,
                MqMessage.of("order.completed", settleData));
        // 发通知
        mqSender.send(MqConstants.NOTIFY_EXCHANGE, "notify.inbox",
                MqMessage.of("order.completed", orderNo + ":" + order.getUserId()));
    }

    /** 取消订单 */
    @Transactional
    public void cancel(String orderNo, String reason) {
        Order order = getByOrderNo(orderNo);
        order.cancel(reason);
        order.setUpdateTime(java.time.LocalDateTime.now());
        updateStatus(orderNo, order);
        redisTemplate.delete("order:detail:" + orderNo);
    }

    /** 申请退款 */
    @Transactional
    public void applyRefund(String orderNo) {
        Order order = getByOrderNo(orderNo);
        order.applyRefund();
        order.setUpdateTime(java.time.LocalDateTime.now());
        updateStatus(orderNo, order);
        redisTemplate.delete("order:detail:" + orderNo);
    }

    /** 精准更新状态（避开分片键，避免 ShardingSphere 拒绝） */
    private void updateStatus(String orderNo, Order order) {
        com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<Order> uw =
            new com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<>();
        uw.eq(Order::getOrderNo, orderNo)
          .set(Order::getStatus, order.getStatus())
          .set(Order::getUpdateTime, order.getUpdateTime());
        orderMapper.update(null, uw);
    }

    /** 订单详情（缓存优先） */
    public Map<String, Object> detail(String orderNo) {
        String cacheKey = "order:detail:" + orderNo;
        String cached = redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            return JSONUtil.toBean(cached, Map.class);
        }
        Order order = getByOrderNo(orderNo);
        OrderSnapshot snapshot = getSnapshot(orderNo);
        Map<String, Object> result = new HashMap<>();
        result.put("order", order);
        result.put("snapshot", snapshot != null ? JSONUtil.parseObj(snapshot.getSnapshotData()) : null);
        redisTemplate.opsForValue().set(cacheKey, JSONUtil.toJsonStr(result), Duration.ofMinutes(30));
        return result;
    }

    /** 我的订单列表 */
    public List<Order> myOrders(String status, int page, int size, Long userId) {
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<Order>()
                .eq(Order::getUserId, userId)
                .eq(status != null && !status.isEmpty(), Order::getStatus, status)
                .orderByDesc(Order::getCreateTime);
        Page<Order> p = new Page<>(page, size);
        return orderMapper.selectPage(p, wrapper).getRecords();
    }

    // ===== 内部方法 =====

    Order getByOrderNo(String orderNo) {
        Order order = orderMapper.selectOne(
                new LambdaQueryWrapper<Order>().eq(Order::getOrderNo, orderNo));
        if (order == null) throw new BizException(ErrorCode.ORDER_NOT_FOUND);
        return order;
    }

    private void saveSnapshot(Order order) {
        OrderSnapshot snapshot = new OrderSnapshot();
        snapshot.setSnapshotId(idGenerator.nextId());
        snapshot.setOrderId(order.getOrderId());
        snapshot.setTenantId(order.getTenantId());
        snapshot.setOrderNo(order.getOrderNo());
        snapshot.setCreateTime(java.time.LocalDateTime.now());
        Map<String, Object> data = new HashMap<>();
        data.put("actualAmount", order.getActualAmount());
        data.put("createTime", order.getCreateTime().toString());
        data.put("skuId", order.getSkuId());
        data.put("spuId", order.getSpuId());
        snapshot.setSnapshotData(JSONUtil.toJsonStr(data));
        snapshotMapper.insert(snapshot);
    }

    private OrderSnapshot getSnapshot(String orderNo) {
        return snapshotMapper.selectOne(
                new LambdaQueryWrapper<OrderSnapshot>().eq(OrderSnapshot::getOrderNo, orderNo));
    }

    private void cacheOrderDetail(Order order) {
        redisTemplate.opsForValue().set("order:detail:" + order.getOrderNo(),
                JSONUtil.toJsonStr(Map.of("order", order)),
                Duration.ofMinutes(30));
    }

    // ===== 定时任务 =====

    /** 每 5 分钟自动取消超时未支付订单 */
    @org.springframework.scheduling.annotation.Scheduled(fixedDelay = 300000)
    public void autoCancelTimeoutOrders() {
        log.info("定时任务: 扫描超时订单...");
        java.time.LocalDateTime deadline = java.time.LocalDateTime.now().minusMinutes(15);
        java.util.List<Order> timeoutOrders = orderMapper.selectList(
            new LambdaQueryWrapper<Order>()
                .eq(Order::getStatus, "PENDING_PAY")
                .lt(Order::getCreateTime, deadline)
        );
        for (Order order : timeoutOrders) {
            try {
                cancel(order.getOrderNo(), "超时未支付自动取消");
                log.info("超时订单自动取消: orderNo={}", order.getOrderNo());
            } catch (Exception e) {
                log.error("自动取消失败: orderNo={}", order.getOrderNo(), e);
            }
        }
        log.info("定时任务完成: 取消 {} 笔超时订单", timeoutOrders.size());
    }

    // ===== 统计（给 data-service 调） =====

    public Map<String, Object> todayStats() {
        Long count = orderMapper.selectCount(new LambdaQueryWrapper<Order>()
                .ge(Order::getCreateTime, java.time.LocalDate.now()));
        return Map.of("todayOrders", count, "todayGMV", 0);
    }

    public Map<String, Object> trend(String period) {
        return Map.of("period", period,
                "labels", new String[]{"Mon","Tue","Wed","Thu","Fri","Sat","Sun"},
                "values", new int[]{120,145,132,168,155,200,180});
    }
}
