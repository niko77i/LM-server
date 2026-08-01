package com.lmserver.repository.fb;

import com.lmserver.entity.fb.FbBms;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FbBmsRepository extends JpaRepository<FbBms, Long> {
    java.util.List<FbBms> findByOwnerIdOrderByNameAsc(Long ownerId);
}