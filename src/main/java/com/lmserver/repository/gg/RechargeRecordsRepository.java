package com.lmserver.repository.gg;

import com.lmserver.entity.gg.RechargeRecords;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RechargeRecordsRepository extends JpaRepository<RechargeRecords, Long> {
}