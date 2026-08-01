package com.lmserver.entity.common;

import com.baomidou.mybatisplus.annotation.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("copywritings")
public class Copywritings {

        
    private Long id;

    private String region;

    private String content;

    @TableField("created_at")
    private LocalDateTime createdAt;

    @TableField("owner_id")
    private Long ownerId;

    private String effectiveness;

    @TableField("is_public")
    private Long isPublic;

}