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
@Table(name = "scrape_cache")
public class ScrapeCache {

    @Id
    @Column(name = "package_name")
    private String packageName;

    @Column(name = "image_count")
    private Long imageCount;

    @Column(name = "saved_path")
    private String savedPath;

    @Column(name = "logo_path")
    private String logoPath;

    @Column(name = "last_scraped")
    private LocalDateTime lastScraped;

    @Column(name = "scraped_by")
    private Long scrapedBy;

}