package com.sharecampus.coupon.controller;

import com.sharecampus.common.core.model.Result;
import com.sharecampus.common.security.UserContext;
import com.sharecampus.coupon.entity.CouponTemplate;
import com.sharecampus.coupon.entity.UserCoupon;
import com.sharecampus.coupon.service.CouponGrabService;
import com.sharecampus.coupon.service.CouponService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/coupon")
@RequiredArgsConstructor
public class CouponController {

    private final CouponGrabService grabService;
    private final CouponService couponService;

    /** 抢券（高并发核心接口） */
    @PostMapping("/grab/{templateId}")
    public Result<String> grab(@PathVariable Long templateId, @RequestHeader("X-User-Id") Long userId) {
        int result = grabService.grab(templateId, userId);
        return switch (result) {
            case 1 -> Result.success("抢券成功");
            case -1 -> Result.fail(3001, "已被抢光");
            case -2 -> Result.fail(3002, "已抢过");
            default -> Result.fail(500, "系统繁忙");
        };
    }

    /** 普通领取 */
    @PostMapping("/receive/{templateId}")
    public Result<Void> receive(@PathVariable Long templateId, @RequestHeader("X-User-Id") Long userId) {
        couponService.handleGrabSuccess(templateId, userId);
        return Result.success();
    }

    /** 我的优惠券 */
    @GetMapping("/my")
    public Result<List<UserCoupon>> my(@RequestParam(required = false) String status) {
        return Result.success(couponService.myCoupons(status));
    }

    // ===== 运营后台 =====
    @PostMapping("/admin/template")
    public Result<Void> createTemplate(@RequestBody CouponTemplate template) {
        couponService.createTemplate(template);
        return Result.success();
    }

    @GetMapping("/admin/templates")
    public Result<List<CouponTemplate>> listTemplates() {
        return Result.success(couponService.listTemplates());
    }

    @PostMapping("/admin/batch-send")
    public Result<Void> batchSend(@RequestBody Map<String, Object> body) {
        Long templateId = Long.valueOf(body.get("templateId").toString());
        @SuppressWarnings("unchecked")
        List<Long> userIds = (List<Long>) body.get("userIds");
        couponService.batchSend(templateId, userIds);
        return Result.success();
    }
}
