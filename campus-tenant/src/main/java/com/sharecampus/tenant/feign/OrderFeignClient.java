package com.sharecampus.tenant.feign;

import com.sharecampus.common.core.model.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

/** Feign 调用 order-service 查询租户订单用量 */
@FeignClient("campus-order")
public interface OrderFeignClient {

    /** 查询今日订单量 */
    @GetMapping("/api/v1/order/stats/today")
    Result<Map<String, Object>> todayStats();

    /** 查询指定租户的今日订单量 */
    @GetMapping("/api/v1/order/stats/tenant-today")
    Result<Long> tenantTodayOrders(@RequestParam("tenantId") Long tenantId);
}
