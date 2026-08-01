package com.lmserver.repository.gg;

import com.lmserver.entity.gg.Mcc;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MccRepository extends JpaRepository<Mcc, Long> {
}