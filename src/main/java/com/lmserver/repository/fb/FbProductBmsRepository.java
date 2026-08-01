package com.lmserver.repository.fb;

import com.lmserver.entity.fb.FbProductBms;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FbProductBmsRepository extends JpaRepository<FbProductBms, Long> {
}