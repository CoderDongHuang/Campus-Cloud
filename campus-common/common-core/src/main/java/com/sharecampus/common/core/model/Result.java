package com.sharecampus.common.core.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 统一响应体
 * <p>
 * 所有 Controller 的返回值都包在此类中，前端统一解析
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Result<T> {

    /** 状态码，200 表示成功，其他表示异常 */
    private int code;
    /** 提示信息 */
    private String message;
    /** 数据体 */
    private T data;
    /** 时间戳 */
    private long timestamp;

    // ========== 工厂方法 ==========

    public static <T> Result<T> success() {
        return new Result<>(200, "success", null, System.currentTimeMillis());
    }

    public static <T> Result<T> success(T data) {
        return new Result<>(200, "success", data, System.currentTimeMillis());
    }

    public static <T> Result<T> success(String message, T data) {
        return new Result<>(200, message, data, System.currentTimeMillis());
    }

    public static <T> Result<T> fail(int code, String message) {
        return new Result<>(code, message, null, System.currentTimeMillis());
    }

    public static <T> Result<T> fail(int code, String message, T data) {
        return new Result<>(code, message, data, System.currentTimeMillis());
    }
}
