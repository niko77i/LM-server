package com.lmserver.repository.gg;

import com.lmserver.entity.gg.AdReports;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AdReportsRepository extends JpaRepository<AdReports, Long> {
}