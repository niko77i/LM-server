package com.lmserver.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 广告线简要信息 DTO — 内嵌在 FbProductDto 中。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LineBriefDto {

    private Long id;

    private String lineName;

    private String link;

    private Long pixelId;
}
