package com.sharecampus.review.controller;

import com.sharecampus.common.core.model.Result;
import com.sharecampus.review.entity.OrderReview;
import com.sharecampus.review.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.*;

@RestController
@RequestMapping("/api/v1/review")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping("/reviews")
    public Result<Void> submit(@RequestBody OrderReview review,
                               @RequestHeader("X-User-Id") Long userId) {
        review.setUserId(userId);
        reviewService.submit(review);
        return Result.success();
    }

    @GetMapping("/reviews/{orderId}")
    public Result<OrderReview> getByOrder(@PathVariable Long orderId) {
        return Result.success(reviewService.getByOrderId(orderId));
    }

    @GetMapping("/worker/{workerId}/reviews")
    public Result<List<OrderReview>> listByWorker(@PathVariable Long workerId) {
        return Result.success(reviewService.listByWorker(workerId));
    }

    @GetMapping("/worker/{workerId}/rating")
    public Result<com.sharecampus.review.entity.WorkerRating> getRating(@PathVariable Long workerId) {
        return Result.success(reviewService.getWorkerRating(workerId));
    }

    @PostMapping("/reviews/{id}/images")
    public Result<List<String>> uploadImages(@PathVariable Long id,
                                              @RequestParam("files") List<MultipartFile> files) {
        return Result.success(reviewService.saveImages(id, files));
    }

    @GetMapping("/admin/pending")
    public Result<List<OrderReview>> pendingReviews() {
        return Result.success(reviewService.pendingReviews());
    }

    @PutMapping("/admin/reviews/{id}/approve")
    public Result<Void> approveReview(@PathVariable Long id) {
        reviewService.approveReview(id);
        return Result.success();
    }
}
