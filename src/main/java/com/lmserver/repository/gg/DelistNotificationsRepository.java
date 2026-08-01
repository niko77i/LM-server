package com.lmserver.repository.gg;

import com.lmserver.entity.gg.DelistNotifications;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DelistNotificationsRepository extends JpaRepository<DelistNotifications, Long> {
}