package com.lmserver.repository.fb;

import com.lmserver.entity.fb.FbAdReports;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FbAdReportsRepository extends JpaRepository<FbAdReports, Long> {
}