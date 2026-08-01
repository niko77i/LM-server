package com.lmserver.repository.fb;

import com.lmserver.entity.fb.FbLines;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FbLinesRepository extends JpaRepository<FbLines, Long> {
}