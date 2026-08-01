package com.lmserver.repository.common;

import com.lmserver.entity.common.Regions;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RegionsRepository extends JpaRepository<Regions, Long> {
    java.util.List<Regions> findByPlatform(String platform);
}