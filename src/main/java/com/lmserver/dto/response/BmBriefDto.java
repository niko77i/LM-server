package com.lmserver.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * BM 简要信息 DTO — 内嵌在 FbAccountDto / FbProductDto 中。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BmBriefDto {

    private Long id;

    private String name;

    private String bmId;
}
