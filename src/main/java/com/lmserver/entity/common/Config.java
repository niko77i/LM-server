package com.lmserver.entity.common;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("config")
public class Config {

    @TableId
    /** 键 */
    private String key;

    /** 值(JSON) */
    private String value;

}