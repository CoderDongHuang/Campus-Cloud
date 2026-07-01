package com.sharecampus.data.feign;

import com.sharecampus.common.core.model.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

/** Feign 调用 order-service */
@FeignClient("campus-order")
public interface OrderFeignClient {

    @GetMapping("/api/v1/order/stats/today")
    Result<Map<String, Object>> todayStats();

    @GetMapping("/api/v1/order/stats/trend")
    Result<Map<String, Object>> trend(@RequestParam("period") String period);
}
