package com.sharecampus.common.core.exception;

/**
 * 业务错误码枚举
 * <p>
 * 按模块分段，便于快速定位问题：
 * 1xxx — 用户模块 | 2xxx — 订单模块 | 3xxx — 优惠券模块
 * 4xxx — 支付模块 | 5xxx — 结算模块 | 6xxx — 搜索模块
 * 7xxx — IM模块 | 8xxx — 租户模块 | 9xxx — 系统通用
 */
public enum ErrorCode {

    // ===== 系统通用 9000-9999 =====
    SUCCESS(200, "操作成功"),
    PARAM_ERROR(400, "参数校验失败"),
    UNAUTHORIZED(401, "未登录或 Token 已过期"),
    FORBIDDEN(403, "无权访问"),
    NOT_FOUND(404, "资源不存在"),
    INTERNAL_ERROR(500, "系统内部异常"),
    RATE_LIMIT(429, "请求过于频繁，请稍后重试"),

    // ===== 用户模块 1000-1999 =====
    USER_NOT_FOUND(1001, "用户不存在"),
    PASSWORD_ERROR(1002, "密码错误"),
    PHONE_ALREADY_EXISTS(1003, "手机号已注册"),
    LOGIN_FAILED_TOO_MANY(1004, "登录失败次数过多，请 30 分钟后再试"),
    SMS_CODE_ERROR(1005, "验证码错误或已过期"),
    USER_STATUS_DISABLED(1006, "账号已被禁用"),

    // ===== 订单模块 2000-2999 =====
    ORDER_NOT_FOUND(2001, "订单不存在"),
    ORDER_STATUS_NOT_ALLOW(2002, "当前订单状态不允许此操作"),
    ORDER_HAS_BEEN_GRABBED(2003, "订单已被其他师傅接单"),
    ORDER_TIMEOUT_CANCEL(2004, "订单超时已自动取消"),

    // ===== 优惠券模块 3000-3999 =====
    COUPON_STOCK_ZERO(3001, "优惠券已被抢光"),
    COUPON_ALREADY_GRABBED(3002, "您已经抢过了"),
    COUPON_EXPIRED(3003, "优惠券已过期"),
    COUPON_NOT_MATCH(3004, "不满足优惠券使用条件"),

    // ===== 支付模块 4000-4999 =====
    PAY_FAILED(4001, "支付失败"),
    REFUND_AMOUNT_EXCEED(4002, "退款金额超出原订单金额"),
    PAY_CALLBACK_ERROR(4003, "支付回调验签失败"),
    PAY_ORDER_NOT_FOUND(4004, "支付单不存在，请稍后重试"),

    // ===== 结算模块 5000-5999 =====
    SETTLE_BALANCE_NOT_ENOUGH(5001, "可提现余额不足"),
    WITHDRAW_BELOW_MINIMUM(5002, "提现金额低于最低限额"),

    // ===== 搜索模块 6000-6999 =====
    SEARCH_SYNC_FAILED(6001, "搜索索引同步失败"),

    // ===== IM 模块 7000-7999 =====
    IM_SESSION_NOT_FOUND(7001, "会话不存在"),
    IM_SEND_TOO_FREQUENT(7002, "消息发送频率过高"),

    // ===== 租户模块 8000-8999 =====
    TENANT_NOT_FOUND(8001, "租户不存在"),
    TENANT_EXPIRED(8002, "租户已过期，请联系管理员续费");

    private final int code;
    private final String message;

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() { return code; }

    public String getMessage() { return message; }
}
