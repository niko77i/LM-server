package com.lmserver.entity.gg;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "delist_notifications")
public class DelistNotifications {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "package_id")
    private Long packageId;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "first_notified")
    private Long firstNotified;

    @Column(name = "dismissed_at")
    private LocalDateTime dismissedAt;

    @Column(name = "reminder_count")
    private Long reminderCount;

}