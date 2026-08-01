package com.lmserver.entity.gg;

import com.baomidou.mybatisplus.annotation.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Entity mapped to table: sheetssynclog */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("sheets_sync_log")
public class SheetsSyncLog {

        
    private Long id;

    @TableField("user_id")
    private Long userId;

    @TableField("product_name")
    private String productName;

    @TableField("spreadsheet_id")
    private String spreadsheetId;

    @TableField("sheet_gid")
    private String sheetGid;

    private String status;

    @TableField("error_msg")
    private String errorMsg;

    @TableField("rows_json")
    private String rowsJson;

    @TableField("retry_count")
    private Long retryCount;

    @TableField("created_at")
    private LocalDateTime createdAt;

    @TableField("updated_at")
    private LocalDateTime updatedAt;

}