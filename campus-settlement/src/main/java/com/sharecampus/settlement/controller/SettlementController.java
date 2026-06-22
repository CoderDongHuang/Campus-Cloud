package com.sharecampus.settlement.controller;

import com.sharecampus.common.core.model.Result;
import com.sharecampus.common.security.UserContext;
import com.sharecampus.settlement.entity.SettlementOrder;
import com.sharecampus.settlement.entity.WorkerWallet;
import com.sharecampus.settlement.entity.WithdrawApply;
import com.sharecampus.settlement.service.SettlementService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/settlement")
@RequiredArgsConstructor
public class SettlementController {

    private final SettlementService settlementService;

    @GetMapping("/wallet")
    public Result<WorkerWallet> myWallet() {
        return Result.success(settlementService.myWallet());
    }

    @GetMapping("/orders")
    public Result<List<SettlementOrder>> incomeList() {
        return Result.success(settlementService.incomeList());
    }

    @PostMapping("/withdraw")
    public Result<WithdrawApply> withdraw(@RequestBody Map<String, Object> body) {
        BigDecimal amount = new BigDecimal(body.get("amount").toString());
        return Result.success(settlementService.applyWithdraw(UserContext.getUserId(), amount));
    }
}
