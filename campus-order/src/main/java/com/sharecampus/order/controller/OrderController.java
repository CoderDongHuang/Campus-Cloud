package com.sharecampus.order.controller;

import com.sharecampus.common.core.model.Result;
import com.sharecampus.order.entity.Order;
import com.sharecampus.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/order")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping("/orders")
    public Result<Order> create(@RequestBody Order order) {
        return Result.success(orderService.createOrder(order));
    }

    @GetMapping("/orders/{orderNo}")
    public Result<Map<String, Object>> detail(@PathVariable String orderNo) {
        return Result.success(orderService.detail(orderNo));
    }

    @GetMapping("/orders")
    public Result<List<Order>> myOrders(@RequestParam(required = false) String status,
                                        @RequestParam(defaultValue = "1") int page,
                                        @RequestParam(defaultValue = "20") int size) {
        return Result.success(orderService.myOrders(status, page, size));
    }

    @PutMapping("/orders/{orderNo}/pay")
    public Result<Void> pay(@PathVariable String orderNo) {
        orderService.pay(orderNo);
        return Result.success();
    }

    @PutMapping("/orders/{orderNo}/cancel")
    public Result<Void> cancel(@PathVariable String orderNo, @RequestBody Map<String, String> body) {
        orderService.cancel(orderNo, body.getOrDefault("reason", "用户取消"));
        return Result.success();
    }

    @PutMapping("/orders/{orderNo}/refund")
    public Result<Void> refund(@PathVariable String orderNo) {
        orderService.applyRefund(orderNo);
        return Result.success();
    }

    // ===== 师傅端 =====
    @PutMapping("/worker/orders/{orderNo}/accept")
    public Result<Void> accept(@PathVariable String orderNo) {
        orderService.accept(orderNo, com.sharecampus.common.security.UserContext.getUserId());
        return Result.success();
    }

    @PutMapping("/worker/orders/{orderNo}/complete")
    public Result<Void> complete(@PathVariable String orderNo) {
        orderService.complete(orderNo);
        return Result.success();
    }

    // ===== 数据统计（给 data-service 调） =====
    @GetMapping("/stats/today")
    public Result<Map<String, Object>> todayStats() {
        return Result.success(orderService.todayStats());
    }

    @GetMapping("/stats/trend")
    public Result<Map<String, Object>> trend(@RequestParam(defaultValue = "week") String period) {
        return Result.success(orderService.trend(period));
    }
}
