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
@TableName("scrape_cache")
public class ScrapeCache {

        @TableField("package_name")
    private String packageName;

    @TableField("image_count")
    private Long imageCount;

    @TableField("saved_path")
    private String savedPath;

    @TableField("logo_path")
    private String logoPath;

    @TableField("last_scraped")
    private LocalDateTime lastScraped;

    @TableField("scraped_by")
    private Long scrapedBy;

}