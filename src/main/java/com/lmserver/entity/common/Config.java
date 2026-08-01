package com.lmserver.entity.common;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Entity mapped to table: config */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("config")
public class Config {

        private String key;

    private String value;

}