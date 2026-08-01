package com.lmserver.entity.common;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Entity mapped to table: tags */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("tags")
public class Tags {

        private String key;

    private String value;

}