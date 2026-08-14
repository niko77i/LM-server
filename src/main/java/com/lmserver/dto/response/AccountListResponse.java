package com.lmserver.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * 账户列表响应 — PagedResponse 字段 + 前端筛选所需的元数据。
 * 对齐 GG-Server /api/accounts/list 返回结构。
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AccountListResponse {

    private boolean success = true;

    private List<AccountDto> items;

    private long total;

    private int page;

    private int size;

    /** 各状态账户数（按状态名称分组），用于状态筛选按钮 */
    @JsonProperty("status_counts")
    private Map<String, Long> statusCounts;

    /** MCC 下拉选项 [{id, name, mcc_id}] */
    @JsonProperty("mcc_options")
    private List<Map<String, Object>> mccOptions;

    /** 时区下拉选项 [timezone] */
    @JsonProperty("timezone_options")
    private List<String> timezoneOptions;
}
