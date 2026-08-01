package com.lmserver.repository.gg;

import com.lmserver.entity.gg.DelistChecks;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DelistChecksRepository extends JpaRepository<DelistChecks, Long> {
}