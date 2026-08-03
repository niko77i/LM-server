package com.lmserver.entity.gg;

import com.baomidou.mybatisplus.annotation.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("sheets_sync_log")
public class SheetsSyncLog {

        
    /** 主键ID */
    private Long id;

    @TableField("user_id")
    /** 用户ID */
    private Long userId;

    @TableField("product_name")
    /** 产品名称 */
    private String productName;

    @TableField("spreadsheet_id")
    /** Google Sheets表格ID */
    private String spreadsheetId;

    @TableField("sheet_gid")
    /** Sheet GID */
    private String sheetGid;

    /** 状态 */
    private String status;

    @TableField("error_msg")
    /** 错误信息 */
    private String errorMsg;

    @TableField("rows_json")
    /** 行数据JSON */
    private String rowsJson;

    @TableField("retry_count")
    /** 重试次数 */
    private Long retryCount;

    @TableField("created_at")
    /** 创建时间 */
    private LocalDateTime createdAt;

    @TableField("updated_at")
    /** 更新时间 */
    private LocalDateTime updatedAt;

}