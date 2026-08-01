package com.lmserver.repository.common;

import com.lmserver.entity.common.AccountStatuses;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AccountStatusesRepository extends JpaRepository<AccountStatuses, Long> {
    java.util.List<AccountStatuses> findByOwnerIdAndPlatformOrderByCreatedAtDesc(Long ownerId, String platform);
}