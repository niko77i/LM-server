package com.lmserver.entity.common;

import com.baomidou.mybatisplus.annotation.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Entity mapped to table: agents */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("agents")
public class Agents {

        
    private Long id;

    private String name;

    @TableField("owner_id")
    private Long ownerId;

    @TableField("created_at")
    private LocalDateTime createdAt;

}