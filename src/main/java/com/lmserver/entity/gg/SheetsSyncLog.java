package com.lmserver.entity.gg;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "sheets_sync_log")
public class SheetsSyncLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "product_name")
    private String productName;

    @Column(name = "spreadsheet_id")
    private String spreadsheetId;

    @Column(name = "sheet_gid")
    private String sheetGid;

    private String status;

    @Column(name = "error_msg")
    private String errorMsg;

    @Column(name = "rows_json")
    private String rowsJson;

    @Column(name = "retry_count")
    private Long retryCount;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

}