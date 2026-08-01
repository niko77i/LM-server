package com.lmserver.repository.fb;

import com.lmserver.entity.fb.FbAccounts;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FbAccountsRepository extends JpaRepository<FbAccounts, Long> {
}