package com.lmserver.entity.common;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("tags")
public class Tags {

        @TableId
    @TableField("`key`")
    /** 键 */
    private String key;

    @TableField("`value`")
    /** 值(JSON) */
    private String value;

}