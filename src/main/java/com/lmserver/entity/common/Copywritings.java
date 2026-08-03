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

        
    /** 主键ID */
    private Long id;

    /** 地区 */
    private String region;

    /** 内容 */
    private String content;

    @TableField("created_at")
    /** 创建时间 */
    private LocalDateTime createdAt;

    @TableField("owner_id")
    /** 归属用户ID */
    private Long ownerId;

    /** 成效标记 */
    private String effectiveness;

    @TableField("is_public")
    /** 是否公开: 0否/1是 */
    private Long isPublic;

}