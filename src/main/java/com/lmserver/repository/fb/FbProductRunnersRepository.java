package com.lmserver.repository.fb;

import com.lmserver.entity.fb.FbProductRunners;
import com.lmserver.entity.fb.FbProductRunnersId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FbProductRunnersRepository extends JpaRepository<FbProductRunners, FbProductRunnersId> {
}