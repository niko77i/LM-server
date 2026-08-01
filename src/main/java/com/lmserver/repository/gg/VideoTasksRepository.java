package com.lmserver.repository.gg;

import com.lmserver.entity.gg.VideoTasks;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VideoTasksRepository extends JpaRepository<VideoTasks, Long> {
}