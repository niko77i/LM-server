package com.lmserver.repository.gg;

import com.lmserver.entity.gg.Videos;
import com.lmserver.entity.gg.VideosId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VideosRepository extends JpaRepository<Videos, VideosId> {
}