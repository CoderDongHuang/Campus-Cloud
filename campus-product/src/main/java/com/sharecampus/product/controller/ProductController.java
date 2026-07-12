package com.sharecampus.product.controller;

import com.sharecampus.common.core.model.Result;
import com.sharecampus.product.entity.ProductCategory;
import com.sharecampus.product.entity.ProductSku;
import com.sharecampus.product.entity.ProductSpu;
import com.sharecampus.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/product")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @GetMapping("/spu/top")
    public Result<List<ProductSpu>> spuTop(@RequestParam(defaultValue = "5") int n) {
        return Result.success(productService.spuTop(n));
    }

    @GetMapping("/categories/tree")
    public Result<List<ProductCategory>> categoryTree() {
        return Result.success(productService.categoryTree());
    }

    @GetMapping("/spu/list")
    public Result<List<ProductSpu>> spuList(@RequestParam(name = "categoryId", required = false) Long categoryId,
                                            @RequestParam(name = "keyword", required = false) String keyword) {
        return Result.success(productService.spuList(categoryId, keyword));
    }

    @GetMapping("/spu/{id}")
    public Result<Map<String, Object>> spuDetail(@PathVariable Long id) {
        return Result.success(productService.spuDetail(id));
    }

    // ===== 后台管理 =====
    @PostMapping("/admin/spu")
    public Result<Void> saveSpu(@RequestBody ProductSpu spu) {
        productService.saveSpu(spu);
        return Result.success();
    }

    @PutMapping("/admin/spu/{id}")
    public Result<Void> updateSpu(@PathVariable Long id, @RequestBody ProductSpu spu) {
        spu.setId(id);
        productService.updateSpu(spu);
        return Result.success();
    }

    @PutMapping("/admin/spu/{id}/status")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestBody Map<String, Integer> body) {
        productService.updateSpuStatus(id, body.get("status"));
        return Result.success();
    }

    @PostMapping("/admin/sku")
    public Result<Void> saveSku(@RequestBody ProductSku sku) {
        productService.saveSku(sku);
        return Result.success();
    }

    @PutMapping("/admin/sku/{id}")
    public Result<Void> updateSku(@PathVariable Long id, @RequestBody ProductSku sku) {
        sku.setId(id);
        productService.updateSku(sku);
        return Result.success();
    }
}
