package com.sharecampus.product.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sharecampus.product.entity.ProductCategory;
import com.sharecampus.product.entity.ProductSku;
import com.sharecampus.product.entity.ProductSpu;
import com.sharecampus.product.mapper.ProductCategoryMapper;
import com.sharecampus.product.mapper.ProductSkuMapper;
import com.sharecampus.product.mapper.ProductSpuMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductCategoryMapper categoryMapper;
    private final ProductSpuMapper spuMapper;
    private final ProductSkuMapper skuMapper;
    private final StringRedisTemplate redisTemplate;

    // ==================== 类目 ====================

    public List<ProductCategory> categoryTree() {
        List<ProductCategory> all = categoryMapper.selectList(
                new LambdaQueryWrapper<ProductCategory>()
                        .eq(ProductCategory::getStatus, 1)
                        .orderByAsc(ProductCategory::getSortOrder));
        // 构建树：先返回顶级，再嵌套子级
        Map<Long, List<ProductCategory>> childrenMap = all.stream()
                .filter(c -> c.getParentId() != 0)
                .collect(Collectors.groupingBy(ProductCategory::getParentId));
        return all.stream()
                .filter(c -> c.getParentId() == 0)
                .peek(c -> c.setChildren(childrenMap.getOrDefault(c.getId(), Collections.emptyList())))
                .collect(Collectors.toList());
    }

    // ==================== 商品列表 ====================

    public List<ProductSpu> spuList(Long categoryId, String keyword) {
        LambdaQueryWrapper<ProductSpu> wrapper = new LambdaQueryWrapper<ProductSpu>()
                .eq(ProductSpu::getStatus, 1)
                .orderByDesc(ProductSpu::getSalesCount);
        if (categoryId != null) {
            wrapper.eq(ProductSpu::getCategoryId, categoryId);
        }
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.like(ProductSpu::getName, keyword);
            // 记录搜索热词
            redisTemplate.opsForZSet().incrementScore("hot:search", keyword, 1);
        }
        return spuMapper.selectList(wrapper);
    }

    // ==================== 商品详情 ====================

    public Map<String, Object> spuDetail(Long spuId) {
        // 查缓存
        String cacheKey = "product:detail:" + spuId;
        String cached = redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            // 这里的JSON解析省略，实际使用Fastjson/Jackson
            log.debug("缓存命中: {}", cacheKey);
        }

        ProductSpu spu = spuMapper.selectById(spuId);
        if (spu == null) return null;

        List<ProductSku> skus = skuMapper.selectList(
                new LambdaQueryWrapper<ProductSku>().eq(ProductSku::getSpuId, spuId));
        Map<String, Object> result = new HashMap<>();
        result.put("spu", spu);
        result.put("skus", skus);
        // 缓存
        redisTemplate.opsForValue().set(cacheKey, "1", Duration.ofMinutes(30));
        return result;
    }

    // ==================== 后台管理 ====================

    public void saveSpu(ProductSpu spu) {
        spuMapper.insert(spu);
        redisTemplate.delete("product:detail:" + spu.getId());
    }

    public void updateSpu(ProductSpu spu) {
        spuMapper.updateById(spu);
        redisTemplate.delete("product:detail:" + spu.getId());
    }

    public void updateSpuStatus(Long id, Integer status) {
        ProductSpu spu = new ProductSpu();
        spu.setId(id);
        spu.setStatus(status);
        spuMapper.updateById(spu);
    }

    public void saveSku(ProductSku sku) {
        skuMapper.insert(sku);
    }

    public void updateSku(ProductSku sku) {
        skuMapper.updateById(sku);
    }
}
