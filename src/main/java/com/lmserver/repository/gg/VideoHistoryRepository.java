package com.lmserver.repository.gg;

import com.lmserver.entity.gg.VideoHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VideoHistoryRepository extends JpaRepository<VideoHistory, Long> {
}