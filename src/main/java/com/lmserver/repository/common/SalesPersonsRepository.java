package com.lmserver.repository.common;

import com.lmserver.entity.common.SalesPersons;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SalesPersonsRepository extends JpaRepository<SalesPersons, Long> {
    java.util.List<SalesPersons> findByOwnerIdAndPlatformOrderByCreatedAtDesc(Long ownerId, String platform);
}