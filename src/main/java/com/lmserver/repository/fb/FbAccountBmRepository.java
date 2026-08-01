package com.lmserver.repository.fb;

import com.lmserver.entity.fb.FbAccountBm;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FbAccountBmRepository extends JpaRepository<FbAccountBm, Long> {
}