package com.lmserver.repository.common;

import com.lmserver.entity.common.MccLevels;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MccLevelsRepository extends JpaRepository<MccLevels, Long> {
}