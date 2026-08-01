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
@TableName("import_history")
public class ImportHistory {

        
    private Long id;

    @TableField("user_id")
    private Long userId;

    @TableField("file_name")
    private String fileName;

    @TableField("file_type")
    private String fileType;

    @TableField("products_count")
    private Long productsCount;

    @TableField("packages_count")
    private Long packagesCount;

    @TableField("accounts_count")
    private Long accountsCount;

    @TableField("mcc_count")
    private Long mccCount;

    @TableField("videos_count")
    private Long videosCount;

    @TableField("copywritings_count")
    private Long copywritingsCount;

    @TableField("tags_count")
    private Long tagsCount;

    @TableField("skipped_count")
    private Long skippedCount;

    private String status;

    @TableField("error_msg")
    private String errorMsg;

    @TableField("created_at")
    private LocalDateTime createdAt;

}