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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderMapper orderMapper;
    private final OrderIdGenerator idGenerator;
    private final StringRedisTemplate redisTemplate;
    private final MqSender mqSender;

    /** 创建订单 */
    @Transactional
    public Order createOrder(Order order) {
        Long userId = UserContext.getUserId();
        Long tenantId = UserContext.getTenantId();
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
        orderMapper.updateById(order);
        redisTemplate.delete("order:detail:" + orderNo);
        mqSender.send(MqConstants.ORDER_EXCHANGE, MqConstants.ORDER_CREATE_KEY,
                MqMessage.of("order.create", orderNo));
    }

    /** 师傅接单 */
    @Transactional
    public void accept(String orderNo, Long workerId) {
        Order order = getByOrderNo(orderNo);
        order.accept(workerId);
        order.setUpdateTime(java.time.LocalDateTime.now());
        orderMapper.updateById(order);
        redisTemplate.delete("order:detail:" + orderNo);
    }

    /** 完成服务 */
    @Transactional
    public void complete(String orderNo) {
        Order order = getByOrderNo(orderNo);
        order.complete();
        order.setUpdateTime(java.time.LocalDateTime.now());
        orderMapper.updateById(order);
        redisTemplate.delete("order:detail:" + orderNo);
    }

    /** 取消订单 */
    @Transactional
    public void cancel(String orderNo, String reason) {
        Order order = getByOrderNo(orderNo);
        order.cancel(reason);
        order.setUpdateTime(java.time.LocalDateTime.now());
        orderMapper.updateById(order);
        redisTemplate.delete("order:detail:" + orderNo);
    }

    /** 申请退款 */
    @Transactional
    public void applyRefund(String orderNo) {
        Order order = getByOrderNo(orderNo);
        order.applyRefund();
        order.setUpdateTime(java.time.LocalDateTime.now());
        orderMapper.updateById(order);
        redisTemplate.delete("order:detail:" + orderNo);
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
    public List<Order> myOrders(String status, int page, int size) {
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<Order>()
                .eq(Order::getUserId, UserContext.getUserId())
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
        snapshot.setOrderNo(order.getOrderNo());
        Map<String, Object> data = Map.of(
                "actualAmount", order.getActualAmount(),
                "createTime", order.getCreateTime().toString()
        );
        snapshot.setSnapshotData(JSONUtil.toJsonStr(data));
        // 用 JDBC 插入快照（简单方式）
        // snapshotMapper.insert(snapshot);
    }

    private OrderSnapshot getSnapshot(String orderNo) {
        return null; // TODO: 从 t_order_snapshot 查询
    }

    private void cacheOrderDetail(Order order) {
        redisTemplate.opsForValue().set("order:detail:" + order.getOrderNo(),
                JSONUtil.toJsonStr(Map.of("order", order)),
                Duration.ofMinutes(30));
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
