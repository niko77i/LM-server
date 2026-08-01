package com.lmserver.repository.gg;

import com.lmserver.entity.gg.ScrapeCache;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ScrapeCacheRepository extends JpaRepository<ScrapeCache, String> {
}