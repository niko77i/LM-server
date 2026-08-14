package com.lmserver.dto.response;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 应用包 DTO — 独立 DTO，对应 packages 表。
 * 用于 ProductDto 内嵌和独立查询。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PackageDto {

    private Long id;

    /** 所属产品 ID */
    @TableField("product_id")
    private Long productId;

    /** 系列名称 */
    @TableField("series_name")
    private String seriesName;

    /** 包名 */
    @TableField("package_name")
    private String packageName;

    /** 应用商店 URL */
    private String url;

    /** 状态 */
    private String status;

    /** 创建时间 */
    @TableField("created_at")
    private LocalDateTime createdAt;
}
