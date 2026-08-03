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

    @TableId
    /** 包名称 */
    private String packageName;

    @TableField("image_count")
    /** 图片数量 */
    private Long imageCount;

    @TableField("saved_path")
    /** 保存路径 */
    private String savedPath;

    @TableField("logo_path")
    /** Logo路径 */
    private String logoPath;

    @TableField("last_scraped")
    /** 最后爬取时间 */
    private LocalDateTime lastScraped;

    @TableField("scraped_by")
    /** 爬取操作人ID */
    private Long scrapedBy;

}