package com.sharecampus.common.core.model;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

/**
 * 分页请求参数
 * <p>
 * Controller 层接收分页查询参数时继承此类
 */
@Data
public class PageRequest {

    /** 页码，从 1 开始 */
    @Min(value = 1, message = "页码最小为 1")
    private int page = 1;

    /** 每页条数，最大 100 */
    @Min(value = 1, message = "每页最少 1 条")
    @Max(value = 100, message = "每页最多 100 条")
    private int size = 20;
}
