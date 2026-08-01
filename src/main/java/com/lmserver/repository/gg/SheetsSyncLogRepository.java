package com.lmserver.repository.gg;

import com.lmserver.entity.gg.SheetsSyncLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SheetsSyncLogRepository extends JpaRepository<SheetsSyncLog, Long> {
}