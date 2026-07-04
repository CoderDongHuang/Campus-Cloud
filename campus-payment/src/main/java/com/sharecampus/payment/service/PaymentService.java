package com.sharecampus.payment.service;

import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sharecampus.common.core.exception.BizException;
import com.sharecampus.common.core.exception.ErrorCode;
import com.sharecampus.common.mq.MqConstants;
import com.sharecampus.common.mq.MqMessage;
import com.sharecampus.common.mq.MqSender;
import com.sharecampus.common.security.UserContext;
import com.sharecampus.payment.channel.PaymentChannel;
import com.sharecampus.payment.entity.PayOrder;
import com.sharecampus.payment.entity.PayRefund;
import com.sharecampus.payment.mapper.PayOrderMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PayOrderMapper payOrderMapper;
    private final PaymentChannel paymentChannel;
    private final MqSender mqSender;
    private final RedissonClient redissonClient;

    /** 发起支付 */
    @Transactional
    public PayOrder pay(Long orderId, String orderNo, BigDecimal amount) {
        String payOrderNo = "PAY" + IdUtil.getSnowflake().nextIdStr();
        PayOrder payOrder = new PayOrder();
        payOrder.setPayOrderNo(payOrderNo);
        payOrder.setOrderId(orderId);
        payOrder.setOrderNo(orderNo);
        payOrder.setUserId(UserContext.currentUserId());
        payOrder.setAmount(amount);
        payOrder.setChannel(paymentChannel.channelName());
        payOrder.setStatus("WAIT_PAY");
        payOrder.setExpireTime(LocalDateTime.now().plusMinutes(15));
        payOrderMapper.insert(payOrder);

        // 调用支付渠道
        String tradeNo = paymentChannel.pay(payOrderNo, amount, "校享云订单-" + orderNo);
        payOrder.setChannelTradeNo(tradeNo);
        payOrder.setStatus("PAYING");
        payOrderMapper.updateById(payOrder);
        return payOrder;
    }

    /** 支付回调（幂等） */
    @Transactional
    public void handleCallback(String channelTradeNo, Map<String, String> params) {
        RLock lock = redissonClient.getLock("pay:callback:" + channelTradeNo);
        try {
            if (!lock.tryLock(10, TimeUnit.SECONDS)) return;
            // 幂等检查
            PayOrder existing = payOrderMapper.selectOne(
                    new LambdaQueryWrapper<PayOrder>().eq(PayOrder::getChannelTradeNo, channelTradeNo));
            if (existing != null && "PAY_SUCCESS".equals(existing.getStatus())) return;
            // 验签
            if (!paymentChannel.verifyCallback(params)) {
                throw new BizException(ErrorCode.PAY_CALLBACK_ERROR);
            }
            // 更新支付单
            existing.setStatus("PAY_SUCCESS");
            existing.setPayTime(LocalDateTime.now());
            existing.setCallbackData(params.toString());
            payOrderMapper.updateById(existing);
            // 通知订单服务
            mqSender.send(MqConstants.PAYMENT_EXCHANGE, "payment.callback.success",
                    MqMessage.of("pay.success", existing.getOrderNo()));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            if (lock.isHeldByCurrentThread()) lock.unlock();
        }
    }

    /** 退款 */
    @Transactional
    public PayRefund refund(String orderNo, BigDecimal amount, String reason) {
        PayOrder payOrder = payOrderMapper.selectOne(
                new LambdaQueryWrapper<PayOrder>().eq(PayOrder::getOrderNo, orderNo));
        if (payOrder == null || !"PAY_SUCCESS".equals(payOrder.getStatus())) {
            throw new BizException(ErrorCode.ORDER_STATUS_NOT_ALLOW);
        }
        if (amount.compareTo(payOrder.getAmount()) > 0) {
            throw new BizException(ErrorCode.REFUND_AMOUNT_EXCEED);
        }
        PayRefund refund = new PayRefund();
        refund.setRefundNo("RFD" + IdUtil.getSnowflake().nextIdStr());
        refund.setPayOrderNo(payOrder.getPayOrderNo());
        refund.setOrderNo(orderNo);
        refund.setRefundAmount(amount);
        refund.setReason(reason);
        refund.setStatus("REFUNDING");
        String channelRefundNo = paymentChannel.refund(payOrder.getChannelTradeNo(), amount, reason);
        refund.setChannelRefundNo(channelRefundNo);
        refund.setStatus("REFUND_SUCCESS");
        return refund;
    }
}
