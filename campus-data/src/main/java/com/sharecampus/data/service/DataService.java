package com.sharecampus.data.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.Map;

@Service @RequiredArgsConstructor
public class DataService {

    /** 运营概览（简化版，后续可接入真实统计） */
    public Map<String, Object> overview() {
        Map<String, Object> result = new HashMap<>();
        result.put("todayOrders", 0);
        result.put("todayGMV", 0);
        result.put("totalUsers", 0);
        result.put("activeWorkers", 0);
        return result;
    }

    public Map<String, Object> trend(String period) {
        Map<String, Object> result = new HashMap<>();
        result.put("period", period);
        result.put("labels", new String[]{"周一","周二","周三","周四","周五","周六","周日"});
        result.put("values", new int[]{120,145,132,168,155,200,180});
        return result;
    }
}
