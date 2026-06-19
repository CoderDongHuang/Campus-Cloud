package com.sharecampus.payment.channel;

import cn.hutool.core.util.IdUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Map;

/** Mock 支付渠道 — 开发环境用，生产替换为微信/支付宝 */
@Slf4j
@Component
public class MockPaymentChannel implements PaymentChannel {

    @Override public String channelName() { return "mock"; }

    @Override
    public String pay(String payOrderNo, BigDecimal amount, String desc) {
        String tradeNo = "MOCK" + IdUtil.getSnowflake().nextIdStr();
        log.info("Mock支付: payOrderNo={}, amount={}, tradeNo={}", payOrderNo, amount, tradeNo);
        return tradeNo;
    }

    @Override
    public String query(String channelTradeNo) {
        return "SUCCESS";
    }

    @Override
    public String refund(String channelTradeNo, BigDecimal amount, String reason) {
        String refundNo = "REF" + IdUtil.getSnowflake().nextIdStr();
        log.info("Mock退款: tradeNo={}, amount={}, refundNo={}", channelTradeNo, amount, refundNo);
        return refundNo;
    }

    @Override
    public boolean verifyCallback(Map<String, String> params) {
        return true; // Mock 直接通过
    }
}
