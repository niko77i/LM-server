package com.lmserver.entity.common;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Entity mapped to table: regions */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("regions")
public class Regions {

        
    private Long id;

    private String name;

    private String timezone;

    private String platform;

}