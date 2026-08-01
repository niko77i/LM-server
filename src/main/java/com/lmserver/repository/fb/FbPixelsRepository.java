package com.lmserver.repository.fb;

import com.lmserver.entity.fb.FbPixels;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FbPixelsRepository extends JpaRepository<FbPixels, Long> {
}