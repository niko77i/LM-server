package com.lmserver.entity.common;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "import_history")
public class ImportHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "file_name")
    private String fileName;

    @Column(name = "file_type")
    private String fileType;

    @Column(name = "products_count")
    private Long productsCount;

    @Column(name = "packages_count")
    private Long packagesCount;

    @Column(name = "accounts_count")
    private Long accountsCount;

    @Column(name = "mcc_count")
    private Long mccCount;

    @Column(name = "videos_count")
    private Long videosCount;

    @Column(name = "copywritings_count")
    private Long copywritingsCount;

    @Column(name = "tags_count")
    private Long tagsCount;

    @Column(name = "skipped_count")
    private Long skippedCount;

    private String status;

    @Column(name = "error_msg")
    private String errorMsg;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

}