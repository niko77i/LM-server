package com.lmserver.repository.fb;

import com.lmserver.entity.fb.FbAccountBmHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FbAccountBmHistoryRepository extends JpaRepository<FbAccountBmHistory, Long> {
}