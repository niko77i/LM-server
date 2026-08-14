package com.lmserver.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 跨用户对比返回 DTO — 用于 /api/ad-reports/cross-user 端点。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CrossUserResultDto {

    private List<CrossUserDto> users;
}
