package com.lmserver.repository.gg;

import com.lmserver.entity.gg.VideoConsumption;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VideoConsumptionRepository extends JpaRepository<VideoConsumption, Long> {
}