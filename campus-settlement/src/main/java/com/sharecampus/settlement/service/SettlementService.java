package com.sharecampus.settlement.service;

import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sharecampus.common.core.exception.BizException;
import com.sharecampus.common.core.exception.ErrorCode;
import com.sharecampus.common.mq.MqConstants;
import com.sharecampus.common.security.UserContext;
import com.sharecampus.settlement.entity.*;
import com.sharecampus.settlement.mapper.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SettlementService {

    private final SettlementOrderMapper settlementOrderMapper;
    private final WorkerWalletMapper walletMapper;
    private final WithdrawApplyMapper withdrawMapper;
    private final SettlementConfigMapper configMapper;

    /** 支付成功 → 创建分账单 */
    @Transactional
    public void createSettlement(Long orderId, String orderNo, Long workerId, BigDecimal amount, Long categoryId) {
        // 查分账规则
        SettlementConfig config = configMapper.selectOne(
                new LambdaQueryWrapper<SettlementConfig>().eq(SettlementConfig::getCategoryId, categoryId));
        BigDecimal platformRate = config != null ? config.getPlatformRate() : new BigDecimal("0.15");
        BigDecimal workerRate = new BigDecimal("1.00").subtract(platformRate);

        SettlementOrder settlement = new SettlementOrder();
        settlement.setOrderId(orderId);
        settlement.setOrderNo(orderNo);
        settlement.setUserId(workerId);
        settlement.setTotalAmount(amount);
        settlement.setCommissionRate(platformRate);
        settlement.setPlatformCommission(amount.multiply(platformRate).setScale(2, RoundingMode.HALF_UP));
        settlement.setWorkerAmount(amount.multiply(workerRate).setScale(2, RoundingMode.HALF_UP));
        settlement.setStatus("WAIT_SETTLE");
        settlementOrderMapper.insert(settlement);

        // 增加待结算余额
        WorkerWallet wallet = getOrCreateWallet(workerId);
        wallet.setPendingAmount(wallet.getPendingAmount().add(settlement.getWorkerAmount()));
        walletMapper.updateById(wallet);
    }

    /** T+1 定时结算 — 待结算 → 可提现 （每天凌晨 2 点执行） */
    @Transactional
    @org.springframework.scheduling.annotation.Scheduled(cron = "0 0 2 * * ?")
    public void dailySettlement() {
        List<SettlementOrder> pendingList = settlementOrderMapper.selectList(
                new LambdaQueryWrapper<SettlementOrder>()
                        .eq(SettlementOrder::getStatus, "WAIT_SETTLE")
                        .le(SettlementOrder::getCreateTime, LocalDateTime.now().toLocalDate()));

        for (SettlementOrder s : pendingList) {
            s.setStatus("SETTLED");
            s.setSettledTime(LocalDateTime.now());
            settlementOrderMapper.updateById(s);

            WorkerWallet wallet = walletMapper.selectById(s.getUserId());
            if (wallet != null) {
                wallet.setPendingAmount(wallet.getPendingAmount().subtract(s.getWorkerAmount()));
                wallet.setAvailableAmount(wallet.getAvailableAmount().add(s.getWorkerAmount()));
                wallet.setTotalEarned(wallet.getTotalEarned().add(s.getWorkerAmount()));
                walletMapper.updateById(wallet);
            }
        }
        log.info("T+1结算完成: {} 笔", pendingList.size());
    }

    /** 师傅申请提现（乐观锁） */
    @Transactional
    public WithdrawApply applyWithdraw(Long userId, BigDecimal amount) {
        WorkerWallet wallet = walletMapper.selectById(userId);
        if (wallet == null || wallet.getAvailableAmount().compareTo(amount) < 0) {
            throw new BizException(ErrorCode.SETTLE_BALANCE_NOT_ENOUGH);
        }
        wallet.setAvailableAmount(wallet.getAvailableAmount().subtract(amount));
        wallet.setFrozenAmount(wallet.getFrozenAmount().add(amount));
        int rows = walletMapper.updateById(wallet);
        if (rows == 0) throw new BizException(ErrorCode.SETTLE_BALANCE_NOT_ENOUGH);

        WithdrawApply apply = new WithdrawApply();
        apply.setWithdrawNo("WD" + IdUtil.getSnowflake().nextIdStr());
        apply.setUserId(userId);
        apply.setAmount(amount);
        apply.setChannel("WECHAT_WALLET");
        apply.setStatus("PENDING");
        apply.setApplyTime(LocalDateTime.now());
        withdrawMapper.insert(apply);
        return apply;
    }

    /** 我的钱包 */
    public WorkerWallet myWallet() {
        return getOrCreateWallet(UserContext.currentUserId());
    }

    /** 收入明细 */
    public List<SettlementOrder> incomeList() {
        return settlementOrderMapper.selectList(
                new LambdaQueryWrapper<SettlementOrder>()
                        .eq(SettlementOrder::getUserId, UserContext.currentUserId())
                        .orderByDesc(SettlementOrder::getCreateTime));
    }

    private WorkerWallet getOrCreateWallet(Long userId) {
        WorkerWallet wallet = walletMapper.selectById(userId);
        if (wallet == null) {
            wallet = new WorkerWallet();
            wallet.setUserId(userId);
            wallet.setPendingAmount(BigDecimal.ZERO);
            wallet.setAvailableAmount(BigDecimal.ZERO);
            wallet.setFrozenAmount(BigDecimal.ZERO);
            wallet.setWithdrawnAmount(BigDecimal.ZERO);
            wallet.setTotalEarned(BigDecimal.ZERO);
            wallet.setVersion(0);
            walletMapper.insert(wallet);
        }
        return wallet;
    }
}
