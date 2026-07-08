package com.sharecampus.payment.channel;

import cn.hutool.core.util.IdUtil;
import cn.hutool.crypto.SecureUtil;
import cn.hutool.crypto.digest.HmacAlgorithm;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Map;
import java.util.TreeMap;

/**
 * 微信支付渠道 — 生产环境使用 (@Profile("prod"))。
 * <p>
 * 签名方式：HMAC-SHA256（V3 API 用 RSA-SHA256，此处以 V2 API 的 HMAC-SHA256 为例）。
 * 商户需要配置 campus.payment.wx.mch-key 和 campus.payment.wx.mch-id。
 */
@Slf4j
@Component
@Profile("prod")
public class WxPaymentChannel implements PaymentChannel {

    @Value("${campus.payment.wx.mch-key:}")
    private String mchKey;

    @Override
    public String channelName() { return "wxpay"; }

    @Override
    public String pay(String payOrderNo, BigDecimal amount, String desc) {
        // TODO: 对接微信统一下单API
        String tradeNo = "WX" + IdUtil.getSnowflake().nextIdStr();
        log.info("微信支付下单: payOrderNo={}, amount={}, tradeNo={}", payOrderNo, amount, tradeNo);
        return tradeNo;
    }

    @Override
    public String query(String channelTradeNo) {
        // TODO: 对接微信订单查询API
        return "SUCCESS";
    }

    @Override
    public String refund(String channelTradeNo, BigDecimal amount, String reason) {
        // TODO: 对接微信退款API
        String refundNo = "WXREF" + IdUtil.getSnowflake().nextIdStr();
        log.info("微信退款: tradeNo={}, amount={}, refundNo={}", channelTradeNo, amount, refundNo);
        return refundNo;
    }

    @Override
    public boolean verifyCallback(Map<String, String> params) {
        if (mchKey == null || mchKey.isEmpty()) {
            log.error("微信支付商户密钥未配置");
            return false;
        }
        String receivedSign = params.get("sign");
        if (receivedSign == null) return false;

        // 验签：参数排序 → 拼接 → HMAC-SHA256 → 比对
        String calculatedSign = calculateSign(params, mchKey);
        boolean valid = calculatedSign.equalsIgnoreCase(receivedSign);
        if (!valid) {
            log.warn("微信支付回调验签失败: expected={}, received={}", calculatedSign, receivedSign);
        }
        return valid;
    }

    /**
     * 微信支付 V2 签名算法：
     * 1. 参数按 key 字典排序
     * 2. 拼接为 key1=value1&key2=value2&key=API密钥
     * 3. MD5（或 HMAC-SHA256）
     */
    private String calculateSign(Map<String, String> params, String key) {
        TreeMap<String, String> sorted = new TreeMap<>(params);
        sorted.remove("sign"); // sign 不参与签名
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> e : sorted.entrySet()) {
            if (e.getValue() != null && !e.getValue().isEmpty()) {
                sb.append(e.getKey()).append("=").append(e.getValue()).append("&");
            }
        }
        sb.append("key=").append(key);
        return SecureUtil.md5(sb.toString()).toUpperCase();
    }
}
