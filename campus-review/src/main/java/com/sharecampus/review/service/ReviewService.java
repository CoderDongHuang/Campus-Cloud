package com.sharecampus.review.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sharecampus.review.entity.OrderReview;
import com.sharecampus.review.entity.WorkerRating;
import com.sharecampus.review.mapper.OrderReviewMapper;
import com.sharecampus.review.mapper.WorkerRatingMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewService {
    private final OrderReviewMapper reviewMapper;
    private final WorkerRatingMapper ratingMapper;

    public List<OrderReview> pendingReviews() {
        return reviewMapper.selectList(new LambdaQueryWrapper<OrderReview>().isNull(OrderReview::getRating));
    }

    public void approveReview(Long id) {
        OrderReview review = reviewMapper.selectById(id);
        if (review != null) reviewMapper.updateById(review);
    }

    @Transactional
    public void submit(OrderReview review) {
        reviewMapper.insert(review);
        // 更新师傅评分
        updateWorkerRating(review.getWorkerId(), review.getRating());
    }

    public OrderReview getByOrderId(Long orderId) {
        return reviewMapper.selectOne(new LambdaQueryWrapper<OrderReview>().eq(OrderReview::getOrderId, orderId));
    }

    public List<OrderReview> listByWorker(Long workerId) {
        return reviewMapper.selectList(new LambdaQueryWrapper<OrderReview>().eq(OrderReview::getWorkerId, workerId));
    }

    public WorkerRating getWorkerRating(Long workerId) {
        WorkerRating rating = ratingMapper.selectById(workerId);
        if (rating == null) {
            rating = new WorkerRating();
            rating.setWorkerId(workerId);
            rating.setTotalScore(0);
            rating.setReviewCount(0);
            rating.setAvgRating(BigDecimal.ZERO);
        }
        return rating;
    }

    public List<String> saveImages(Long reviewId, List<org.springframework.web.multipart.MultipartFile> files) {
        List<String> urls = new ArrayList<>();
        for (org.springframework.web.multipart.MultipartFile file : files) {
            urls.add("/review/images/" + reviewId + "/" + file.getOriginalFilename());
        }
        OrderReview review = reviewMapper.selectById(reviewId);
        if (review != null && !urls.isEmpty()) {
            review.setImages(String.join(",", urls));
            reviewMapper.updateById(review);
        }
        return urls;
    }

    private void updateWorkerRating(Long workerId, Integer score) {
        if (workerId == null || score == null) return;
        WorkerRating rating = ratingMapper.selectById(workerId);
        if (rating == null) {
            rating = new WorkerRating();
            rating.setWorkerId(workerId);
            rating.setTotalScore(0);
            rating.setReviewCount(0);
        }
        rating.setTotalScore(rating.getTotalScore() + score);
        rating.setReviewCount(rating.getReviewCount() + 1);
        rating.setAvgRating(new BigDecimal(rating.getTotalScore())
                .divide(new BigDecimal(rating.getReviewCount()), 1, RoundingMode.HALF_UP));
        if (rating.getReviewCount() == 1) {
            ratingMapper.insert(rating);
        } else {
            ratingMapper.updateById(rating);
        }
    }
}
