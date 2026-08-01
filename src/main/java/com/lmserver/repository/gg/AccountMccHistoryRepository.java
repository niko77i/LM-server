package com.lmserver.repository.gg;

import com.lmserver.entity.gg.AccountMccHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AccountMccHistoryRepository extends JpaRepository<AccountMccHistory, Long> {
}