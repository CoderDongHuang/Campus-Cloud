package com.sharecampus.data.service;

import com.sharecampus.data.feign.OrderFeignClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service @RequiredArgsConstructor
public class DataService {

    private final OrderFeignClient orderFeignClient;

    public Map<String, Object> overview() {
        try {
            return orderFeignClient.todayStats().getData();
        } catch (Exception e) {
            Map<String, Object> fallback = new HashMap<>();
            fallback.put("todayOrders", 0);
            fallback.put("todayGMV", 0);
            return fallback;
        }
    }

    public Map<String, Object> trend(String period) {
        try {
            return orderFeignClient.trend(period).getData();
        } catch (Exception e) {
            Map<String, Object> fallback = new HashMap<>();
            fallback.put("period", period);
            return fallback;
        }
    }
}
