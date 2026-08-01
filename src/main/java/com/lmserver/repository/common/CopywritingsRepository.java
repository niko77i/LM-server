package com.lmserver.repository.common;

import com.lmserver.entity.common.Copywritings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CopywritingsRepository extends JpaRepository<Copywritings, Long> {
}