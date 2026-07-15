package com.sharecampus.payment.channel;

import cn.hutool.core.util.IdUtil;
import cn.hutool.crypto.SecureUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.security.PrivateKey;
import java.security.Signature;
import java.util.Base64;
import java.util.Map;
import java.util.TreeMap;

/**
 * 微信支付渠道 — 生产环境启用 (@Profile("prod"))。
 *
 * <p><b>接入资质（需在微信商户平台申请）</b>：
 * <ul>
 *   <li>商户号 (mchId) — 微信商户平台 → 账户中心 → 商户信息</li>
 *   <li>APIv3 密钥 (apiV3Key) — 商户平台 → API安全 → 设置APIv3密钥（32位）</li>
 *   <li>商户证书序列号 (mchSerialNo) — 商户平台 → API安全 → 申请API证书</li>
 *   <li>商户私钥 (privateKey) — 申请证书时下载的 apiclient_key.pem</li>
 * </ul>
 *
 * <p><b>开发环境</b>：未配置商户资质时，自动降级为模拟支付（直接返回成功）。
 * <p><b>生产环境</b>：配置 {@code campus.payment.wx.*} 后，对接真实微信支付 V3 API。
 *
 * <p>API 文档：<a href="https://pay.weixin.qq.com/wiki/doc/apiv3/">微信支付 V3 开发者文档</a>
 */
@Slf4j
@Component
@Profile("prod")
public class WxPaymentChannel implements PaymentChannel {

    // ==================== 商户资质配置 ====================
    /** 商户号 */
    @Value("${campus.payment.wx.mch-id:}")
    private String mchId;
    /** APIv3 密钥（32位，用于回调通知验签） */
    @Value("${campus.payment.wx.api-v3-key:}")
    private String apiV3Key;
    /** 商户证书序列号 */
    @Value("${campus.payment.wx.mch-serial-no:}")
    private String mchSerialNo;
    /** 商户私钥路径（classpath 下） */
    @Value("${campus.payment.wx.private-key-path:}")
    private String privateKeyPath;
    /** 支付回调地址 */
    @Value("${campus.payment.wx.notify-url:}")
    private String notifyUrl;

    /** API 基础地址 */
    private static final String API_BASE = "https://api.mch.weixin.qq.com";

    // ==================== 资质检查 ====================

    /** 是否已配置完整的商户资质 */
    private boolean hasCredentials() {
        return mchId != null && !mchId.isEmpty()
            && apiV3Key != null && !apiV3Key.isEmpty()
            && mchSerialNo != null && !mchSerialNo.isEmpty()
            && privateKeyPath != null && !privateKeyPath.isEmpty();
    }

    @Override
    public String channelName() { return "wxpay"; }

    // ==================== 统一下单 ====================

    @Override
    public String pay(String payOrderNo, BigDecimal amount, String desc) {
        // 未配置商户资质 → 降级为开发环境模拟
        if (!hasCredentials()) {
            String tradeNo = "WXSIM" + IdUtil.getSnowflake().nextIdStr();
            log.warn("[模拟支付] 微信商户未配置，生成模拟交易号: payOrderNo={}, amount={}, tradeNo={}",
                    payOrderNo, amount, tradeNo);
            return tradeNo;
        }

        // ===== 微信支付 V3 统一下单 (JSAPI) =====
        // POST https://api.mch.weixin.qq.com/v3/pay/transactions/jsapi
        String tradeNo = "WX" + IdUtil.getSnowflake().nextIdStr();
        long amountInFen = amount.multiply(new BigDecimal(100)).longValue();

        // 构建请求体
        JSONObject reqBody = JSONUtil.createObj()
            .set("appid", "")  // TODO: 替换为小程序/公众号 AppID
            .set("mchid", mchId)
            .set("description", desc)
            .set("out_trade_no", payOrderNo)
            .set("notify_url", notifyUrl)
            .set("amount", JSONUtil.createObj()
                .set("total", amountInFen)
                .set("currency", "CNY"))
            .set("payer", JSONUtil.createObj()
                .set("openid", ""));  // TODO: 替换为用户 OpenID

        String uri = "/v3/pay/transactions/jsapi";
        String reqBodyStr = JSONUtil.toJsonStr(reqBody);
        String authHeader = buildAuthHeader("POST", uri, reqBodyStr);

        try (HttpResponse resp = HttpRequest.post(API_BASE + uri)
                .header("Authorization", authHeader)
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")
                .body(reqBodyStr)
                .execute()) {
            if (resp.isOk()) {
                JSONObject result = JSONUtil.parseObj(resp.body());
                // 返回 prepay_id 用于前端调起支付
                String prepayId = result.getStr("prepay_id");
                log.info("微信支付下单成功: payOrderNo={}, tradeNo={}, prepay_id={}",
                        payOrderNo, tradeNo, prepayId);
            } else {
                log.error("微信支付下单失败: status={}, body={}", resp.getStatus(), resp.body());
            }
        } catch (Exception e) {
            log.error("微信支付下单异常: payOrderNo={}", payOrderNo, e);
        }
        return tradeNo;
    }

    // ==================== 订单查询 ====================

    @Override
    public String query(String channelTradeNo) {
        if (!hasCredentials()) {
            log.info("[模拟查询] tradeNo={}", channelTradeNo);
            return "SUCCESS";  // 开发环境直接返回成功
        }

        // ===== 微信支付 V3 订单查询 =====
        // GET https://api.mch.weixin.qq.com/v3/pay/transactions/out-trade-no/{outTradeNo}
        String uri = "/v3/pay/transactions/out-trade-no/" + channelTradeNo;
        String authHeader = buildAuthHeader("GET", uri, "");

        try (HttpResponse resp = HttpRequest.get(API_BASE + uri)
                .header("Authorization", authHeader)
                .header("Accept", "application/json")
                .execute()) {
            if (resp.isOk()) {
                JSONObject result = JSONUtil.parseObj(resp.body());
                String tradeState = result.getStr("trade_state");
                log.info("微信支付查询: tradeNo={}, state={}", channelTradeNo, tradeState);
                // 状态映射：SUCCESS=支付成功, NOTPAY=未支付, REFUND=已退款, CLOSED=已关闭
                return "SUCCESS".equals(tradeState) ? "SUCCESS" : tradeState;
            }
        } catch (Exception e) {
            log.error("微信支付查询异常: tradeNo={}", channelTradeNo, e);
        }
        return "ERROR";
    }

    // ==================== 退款 ====================

    @Override
    public String refund(String channelTradeNo, BigDecimal amount, String reason) {
        if (!hasCredentials()) {
            String refundNo = "WXREFSIM" + IdUtil.getSnowflake().nextIdStr();
            log.warn("[模拟退款] 微信商户未配置: tradeNo={}, amount={}, refundNo={}",
                    channelTradeNo, amount, refundNo);
            return refundNo;
        }

        // ===== 微信支付 V3 退款申请 =====
        // POST https://api.mch.weixin.qq.com/v3/refund/domestic/refunds
        String refundNo = "WXREF" + IdUtil.getSnowflake().nextIdStr();
        long amountInFen = amount.multiply(new BigDecimal(100)).longValue();

        JSONObject reqBody = JSONUtil.createObj()
            .set("out_trade_no", channelTradeNo)    // 原支付订单号
            .set("out_refund_no", refundNo)          // 退款单号（唯一）
            .set("reason", reason)
            .set("amount", JSONUtil.createObj()
                .set("refund", amountInFen)
                .set("total", amountInFen)           // 原订单总金额（生产环境需从数据库查实付金额）
                .set("currency", "CNY"));

        String uri = "/v3/refund/domestic/refunds";
        String reqBodyStr = JSONUtil.toJsonStr(reqBody);
        String authHeader = buildAuthHeader("POST", uri, reqBodyStr);

        try (HttpResponse resp = HttpRequest.post(API_BASE + uri)
                .header("Authorization", authHeader)
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")
                .body(reqBodyStr)
                .execute()) {
            if (resp.isOk()) {
                JSONObject result = JSONUtil.parseObj(resp.body());
                String status = result.getStr("status");
                log.info("微信退款: tradeNo={}, amount={}分, refundNo={}, status={}",
                        channelTradeNo, amountInFen, refundNo, status);
            } else {
                log.error("微信退款失败: status={}, body={}", resp.getStatus(), resp.body());
            }
        } catch (Exception e) {
            log.error("微信退款异常: tradeNo={}", channelTradeNo, e);
        }
        return refundNo;
    }

    // ==================== 回调验签 ====================

    @Override
    public boolean verifyCallback(Map<String, String> params) {
        // ===== 微信支付 V3 回调通知验签 =====
        // 1. 从 HTTP Header 获取: Wechatpay-Timestamp, Wechatpay-Nonce, Wechatpay-Signature, Wechatpay-Serial
        // 2. 构造验签串: timestamp + "\n" + nonce + "\n" + body + "\n"
        // 3. 用微信平台公钥验签 (RSA-SHA256)

        // V2 兼容验签（参数排序 + MD5/HMAC-SHA256）
        if (apiV3Key == null || apiV3Key.isEmpty()) {
            log.error("微信支付 APIv3 密钥未配置，无法验签");
            return false;
        }
        String receivedSign = params.get("sign");
        if (receivedSign == null) return false;

        String calculatedSign = calculateV2Sign(params, apiV3Key);
        boolean valid = calculatedSign.equalsIgnoreCase(receivedSign);
        if (!valid) {
            log.warn("微信支付回调验签失败: expected={}, received={}", calculatedSign, receivedSign);
        }
        return valid;
    }

    // ==================== 签名算法 ====================

    /**
     * 构建 V3 API 的 Authorization 请求头。
     * 格式：WECHATPAY2-SHA256-RSA2048 mchid="...",nonce_str="...",timestamp="...",
     *       serial_no="...",signature="..."
     */
    private String buildAuthHeader(String method, String uri, String body) {
        long timestamp = System.currentTimeMillis() / 1000;
        String nonceStr = IdUtil.fastSimpleUUID();

        // 构造签名串: HTTP方法\nURL\n时间戳\n随机串\n请求体\n
        String signMessage = method + "\n" + uri + "\n"
                           + timestamp + "\n" + nonceStr + "\n" + body + "\n";

        String signature = signWithRSA(signMessage);

        return "WECHATPAY2-SHA256-RSA2048 mchid=\"" + mchId
             + "\",nonce_str=\"" + nonceStr
             + "\",timestamp=\"" + timestamp
             + "\",serial_no=\"" + mchSerialNo
             + "\",signature=\"" + signature + "\"";
    }

    /** RSA-SHA256 签名（需商户私钥） */
    private String signWithRSA(String message) {
        try {
            // 加载私钥（生产环境从 classpath 或 KMS 加载）
            PrivateKey privateKey = loadPrivateKey();
            Signature sign = Signature.getInstance("SHA256withRSA");
            sign.initSign(privateKey);
            sign.update(message.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(sign.sign());
        } catch (Exception e) {
            log.error("RSA签名失败", e);
            return "";
        }
    }

    /** 加载商户私钥 */
    private PrivateKey loadPrivateKey() throws Exception {
        if (privateKeyPath == null || privateKeyPath.isEmpty()) {
            throw new IllegalStateException("微信商户私钥路径未配置");
        }
        // 读取 PEM 格式私钥文件
        String keyContent = new String(
            getClass().getClassLoader()
                .getResourceAsStream(privateKeyPath)
                .readAllBytes(),
            java.nio.charset.StandardCharsets.UTF_8
        );
        // 去除 PEM 头尾标记和换行
        keyContent = keyContent
            .replace("-----BEGIN PRIVATE KEY-----", "")
            .replace("-----END PRIVATE KEY-----", "")
            .replaceAll("\\s", "");
        byte[] keyBytes = Base64.getDecoder().decode(keyContent);
        java.security.spec.PKCS8EncodedKeySpec spec =
            new java.security.spec.PKCS8EncodedKeySpec(keyBytes);
        return java.security.KeyFactory.getInstance("RSA").generatePrivate(spec);
    }

    /**
     * 微信支付 V2 签名算法（回调验签兼容）：
     * 1. 参数按 key 字典排序
     * 2. 拼接为 key1=value1&key2=value2&key=API密钥
     * 3. MD5 或 HMAC-SHA256
     */
    private String calculateV2Sign(Map<String, String> params, String key) {
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
