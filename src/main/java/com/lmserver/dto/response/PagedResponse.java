package com.lmserver.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;

/**
 * 分页响应。字段名 items 匹配前端 response.items。
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PagedResponse<T> {

    private boolean success = true;
    private List<T> items;
    private long total;
    private int page;
    private int size;

    private PagedResponse() {}

    public static <T> PagedResponse<T> of(List<T> items, long total, int page, int size) {
        PagedResponse<T> r = new PagedResponse<>();
        r.items = items;
        r.total = total;
        r.page = page;
        r.size = size;
        return r;
    }

    public boolean isSuccess() { return success; }
    public List<T> getItems() { return items; }
    public long getTotal() { return total; }
    public int getPage() { return page; }
    public int getSize() { return size; }
}
