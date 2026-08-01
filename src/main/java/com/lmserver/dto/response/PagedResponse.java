package com.lmserver.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 分页响应。字段名 items 匹配前端 response.items。
 */
/**
 * 分页列表响应 — {items: [...], total, page, size}，字段名 items 匹配前端 res.data.items
 */

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PagedResponse<T> {

    private boolean success = true;
    private List<T> items;
    private long total;
    private int page;
    private int size;

    public static <T> PagedResponse<T> of(List<T> items, long total, int page, int size) {
        PagedResponse<T> r = new PagedResponse<>();
        r.items = items;
        r.total = total;
        r.page = page;
        r.size = size;
        return r;
    }
}
