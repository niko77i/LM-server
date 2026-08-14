package com.lmserver.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 趋势系列 DTO — 包含系列名和数据点列表。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TrendSeriesDto {

    private String name;

    private List<TrendPointDto> data;
}
