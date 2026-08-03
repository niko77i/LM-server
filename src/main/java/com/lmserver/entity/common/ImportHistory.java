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

        
    /** 主键ID */
    private Long id;

    @TableField("user_id")
    /** 用户ID */
    private Long userId;

    @TableField("file_name")
    /** 文件名 */
    private String fileName;

    @TableField("file_type")
    /** 文件类型 */
    private String fileType;

    @TableField("products_count")
    /** 产品数量 */
    private Long productsCount;

    @TableField("packages_count")
    /** 包数量 */
    private Long packagesCount;

    @TableField("accounts_count")
    /** 账户数量 */
    private Long accountsCount;

    @TableField("mcc_count")
    /** MCC数量 */
    private Long mccCount;

    @TableField("videos_count")
    /** 视频数量 */
    private Long videosCount;

    @TableField("copywritings_count")
    /** 文案数量 */
    private Long copywritingsCount;

    @TableField("tags_count")
    /** 标签数量 */
    private Long tagsCount;

    @TableField("skipped_count")
    /** 跳过数量 */
    private Long skippedCount;

    /** 状态 */
    private String status;

    @TableField("error_msg")
    /** 错误信息 */
    private String errorMsg;

    @TableField("created_at")
    /** 创建时间 */
    private LocalDateTime createdAt;

}