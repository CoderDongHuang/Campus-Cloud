package com.sharecampus.data.controller;

import com.sharecampus.common.core.model.Result;
import com.sharecampus.data.service.DataService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController @RequestMapping("/api/v1/data") @RequiredArgsConstructor
public class DataController {
    private final DataService dataService;

    @GetMapping("/dashboard/overview")
    public Result<Map<String, Object>> overview() { return Result.success(dataService.overview()); }

    @GetMapping("/dashboard/trend")
    public Result<Map<String, Object>> trend(@RequestParam(defaultValue = "week") String period) {
        return Result.success(dataService.trend(period));
    }
}
