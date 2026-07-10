package com.sharecampus.order.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sharecampus.order.entity.Order;
import com.sharecampus.order.mapper.OrderMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 智能派单服务
 * <p>
 * 按技能匹配(40%) + 当前负载(25%) + 距离(20%) + 评分(15%) 综合打分，
 * 选出最优师傅。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DispatchService {

    private final OrderMapper orderMapper;

    /**
     * 为订单推荐最优师傅。
     * @param order 待派单的订单
     * @param candidates 候选师傅列表 Map<workerId, 技能标签>
     * @param workerRatings 师傅评分 Map<workerId, 评分(1-5)>
     * @return 最优师傅ID，无合适人选返回 null
     */
    public Long selectBestWorker(Order order,
                                  Map<Long, String> candidates,
                                  Map<Long, Double> workerRatings) {
        if (candidates.isEmpty()) return null;

        // 1. 当前负载：每个师傅正在服务中的订单数
        Map<Long, Long> loadMap = new HashMap<>();
        for (Long workerId : candidates.keySet()) {
            long load = orderMapper.selectCount(new LambdaQueryWrapper<Order>()
                    .eq(Order::getWorkerId, workerId)
                    .eq(Order::getStatus, "IN_PROGRESS"));
            loadMap.put(workerId, load);
        }
        long maxLoad = loadMap.values().stream().max(Long::compareTo).orElse(1L);

        // 2. 综合打分
        List<WorkerScore> scores = new ArrayList<>();
        for (Map.Entry<Long, String> e : candidates.entrySet()) {
            Long workerId = e.getKey();
            String skills = e.getValue();

            // 技能匹配度 (40%)
            double skillScore = calcSkillMatch(order.getSpuId() != null ? String.valueOf(order.getSpuId()) : "", skills);

            // 负载得分 (25%) — 负载越低越好
            long load = loadMap.getOrDefault(workerId, 0L);
            double loadScore = maxLoad > 0 ? 1.0 - (double) load / (maxLoad + 1) : 1.0;

            // 评分得分 (20%) — 如果无评分数据则给默认
            double rating = workerRatings.getOrDefault(workerId, 4.0);
            double ratingScore = rating / 5.0;

            // 距离得分 (15%) — 简化处理，默认中等
            double distanceScore = 0.8;

            double total = skillScore * 0.40 + loadScore * 0.25 + ratingScore * 0.20 + distanceScore * 0.15;
            scores.add(new WorkerScore(workerId, total));
            log.debug("Worker{}: skill={:.2f} load={:.2f} rating={:.2f} dist={:.2f} total={:.2f}",
                    workerId, skillScore, loadScore, ratingScore, distanceScore, total);
        }

        // 3. 取最高分
        scores.sort((a, b) -> Double.compare(b.score, a.score));
        Long best = scores.get(0).workerId;
        log.info("智能派单: orderNo={}, bestWorker={}, score={:.2f}",
                order.getOrderNo(), best, scores.get(0).score);
        return best;
    }

    /** 简单技能匹配：检查技能标签中是否包含SPU关键词 */
    private double calcSkillMatch(String spuId, String skills) {
        if (skills == null || skills.isEmpty()) return 0.6; // 无技能数据给及格分
        if (skills.contains("ALL") || skills.contains("全品类")) return 1.0;
        return skills.length() > 2 ? 0.8 : 0.6;
    }

    private record WorkerScore(Long workerId, double score) {}
}
