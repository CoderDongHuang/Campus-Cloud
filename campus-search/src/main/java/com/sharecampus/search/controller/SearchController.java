package com.sharecampus.search.controller;

import com.sharecampus.common.core.model.Result;
import com.sharecampus.search.entity.ProductDoc;
import com.sharecampus.search.service.SearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/search")
@RequiredArgsConstructor
public class SearchController {

    private final SearchService searchService;

    @GetMapping("/product")
    public Result<List<ProductDoc>> search(@RequestParam String keyword, @RequestHeader(value = "X-Tenant-Id", defaultValue = "0") Long tenantId,
                                           @RequestParam(defaultValue = "1") int page,
                                           @RequestParam(defaultValue = "20") int size) throws Exception {
        return Result.success(searchService.search(keyword, page, size, tenantId));
    }
}
