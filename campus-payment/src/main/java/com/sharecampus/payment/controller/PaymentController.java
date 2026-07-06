package com.sharecampus.payment.controller;

import com.sharecampus.common.core.model.Result;
import com.sharecampus.payment.entity.PayOrder;
import com.sharecampus.payment.entity.PayRefund;
import com.sharecampus.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/payment")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/pay")
    public Result<PayOrder> pay(@RequestBody Map<String, Object> body) {
        Long orderId = Long.valueOf(body.get("orderId").toString());
        String orderNo = (String) body.get("orderNo");
        BigDecimal amount = new BigDecimal(body.get("amount").toString());
        Long userId = Long.valueOf(body.getOrDefault("userId", "1").toString());
        return Result.success(paymentService.pay(orderId, orderNo, amount, userId));
    }

    @PostMapping("/callback/{channel}")
    public Result<String> callback(@PathVariable String channel, @RequestBody Map<String, String> params) {
        paymentService.handleCallback(params.get("tradeNo"), params);
        return Result.success("OK");
    }

    @PostMapping("/refund")
    public Result<PayRefund> refund(@RequestBody Map<String, Object> body) {
        String orderNo = (String) body.get("orderNo");
        BigDecimal amount = new BigDecimal(body.get("amount").toString());
        String reason = (String) body.getOrDefault("reason", "用户申请退款");
        PayRefund refund = paymentService.refund(orderNo, amount, reason);
        return Result.success(refund);
    }
}
