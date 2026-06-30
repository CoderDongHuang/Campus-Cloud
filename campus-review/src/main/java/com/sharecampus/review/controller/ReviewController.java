package com.sharecampus.review.controller;
import com.sharecampus.common.core.model.Result;
import com.sharecampus.common.security.UserContext;
import com.sharecampus.review.entity.OrderReview;
import com.sharecampus.review.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/api/v1/review") @RequiredArgsConstructor
public class ReviewController {
    private final ReviewService reviewService;
    @PostMapping("/reviews") public Result<Void> submit(@RequestBody OrderReview review) { review.setUserId(UserContext.getUserId()); reviewService.submit(review); return Result.success(); }
    @GetMapping("/reviews/{orderId}") public Result<OrderReview> getByOrder(@PathVariable Long orderId) { return Result.success(reviewService.getByOrderId(orderId)); }
    @GetMapping("/worker/{workerId}/reviews") public Result<java.util.List<OrderReview>> listByWorker(@PathVariable Long workerId) { return Result.success(reviewService.listByWorker(workerId)); }
}
