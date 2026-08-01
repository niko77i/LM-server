package com.lmserver.repository.fb;

import com.lmserver.entity.fb.FbPixelBms;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FbPixelBmsRepository extends JpaRepository<FbPixelBms, Long> {
}