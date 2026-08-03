package com.lmserver.entity.common;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("regions")
public class Regions {

        
    /** 主键ID */
    private Long id;

    /** 名称 */
    private String name;

    /** 时区 */
    private String timezone;

    /** 所属平台: gg/fb */
    private String platform;

}