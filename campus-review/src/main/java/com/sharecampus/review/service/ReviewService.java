package com.sharecampus.review.service;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sharecampus.review.entity.OrderReview;
import com.sharecampus.review.mapper.OrderReviewMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
@Service @RequiredArgsConstructor
public class ReviewService {
    private final OrderReviewMapper reviewMapper;
    public void submit(OrderReview review) { reviewMapper.insert(review); }
    public OrderReview getByOrderId(Long orderId) {
        return reviewMapper.selectOne(new LambdaQueryWrapper<OrderReview>().eq(OrderReview::getOrderId, orderId));
    }
    public List<OrderReview> listByWorker(Long workerId) {
        return reviewMapper.selectList(new LambdaQueryWrapper<OrderReview>().eq(OrderReview::getWorkerId, workerId));
    }
}
