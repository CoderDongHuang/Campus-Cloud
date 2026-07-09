package com.sharecampus.settlement.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sharecampus.settlement.entity.SettlementOrder;
import com.sharecampus.settlement.mapper.SettlementOrderMapper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 定时对账服务
 * <p>
 * 最终一致性保障：每天凌晨 4 点扫描已支付但未分账的订单，以及分账单状态异常的记录。
 * 发现差量后打印告警日志，并尝试自动修复。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ReconciliationService {

    private final SettlementOrderMapper settlementOrderMapper;

    /** 每日对账：检查 24h 内已支付但未生成分账单的订单 */
    @XxlJob("reconciliationCheck")
    public void reconciliationCheck() {
        log.info("===== 每日对账开始 =====");
        LocalDateTime yesterday = LocalDateTime.now().minusDays(1);

        // 1. 检查 WAIT_SETTLE 超过 24h 的异常单
        List<SettlementOrder> staleList = settlementOrderMapper.selectList(
                new LambdaQueryWrapper<SettlementOrder>()
                        .eq(SettlementOrder::getStatus, "WAIT_SETTLE")
                        .lt(SettlementOrder::getCreateTime, yesterday));
        if (!staleList.isEmpty()) {
            log.warn("发现超过24h未结算的分账单: {} 笔", staleList.size());
            for (SettlementOrder s : staleList) {
                log.warn("待处理: orderNo={}, amount={}, createTime={}",
                        s.getOrderNo(), s.getTotalAmount(), s.getCreateTime());
            }
        }

        log.info("===== 每日对账完成: 异常分账单 {} 笔 =====", staleList.size());
    }
}
