package com.lmserver.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 字体 DTO — 独立 POJO，用于 /api/fonts/list 端点。
 * 来源可以是用户上传(user)或系统字体(system)。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FontDto {

    /** 字体 ID */
    private String id;

    /** 字体名称 */
    private String name;

    /** 来源: user / system */
    private String source;
}
