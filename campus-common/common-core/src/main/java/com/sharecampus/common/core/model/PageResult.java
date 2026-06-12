package com.sharecampus.common.core.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 分页响应体
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageResult<T> {

    /** 当前页数据 */
    private List<T> records;
    /** 总条数 */
    private long total;
    /** 当前页码 */
    private int page;
    /** 每页条数 */
    private int size;
    /** 总页数 */
    private int pages;

    public static <T> PageResult<T> of(List<T> records, long total, int page, int size) {
        int pages = (int) Math.ceil((double) total / size);
        return new PageResult<>(records, total, page, size, pages);
    }
}
