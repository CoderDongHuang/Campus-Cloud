package com.sharecampus.common.core.exception;

import lombok.Getter;

/**
 * 业务异常
 * <p>
 * 所有业务逻辑中需要中断并返回错误时抛此异常，
 * 由 {@link GlobalExceptionHandler} 统一捕获并转换为 Result 响应
 */
@Getter
public class BizException extends RuntimeException {

    private final int code;

    public BizException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.code = errorCode.getCode();
    }

    public BizException(int code, String message) {
        super(message);
        this.code = code;
    }

    public BizException(ErrorCode errorCode, String customMessage) {
        super(customMessage);
        this.code = errorCode.getCode();
    }
}
