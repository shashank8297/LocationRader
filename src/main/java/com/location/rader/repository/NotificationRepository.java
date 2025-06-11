package com.location.rader.repository;

import com.location.rader.model.Notification;
import com.location.rader.utils.NotificationRequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByTargetUserIdAndStatus(Long userId, NotificationRequestStatus notificationRequestStatus);

    List<Notification> findByCurrentUserIdAndStatus(Long userId, NotificationRequestStatus notificationRequestStatus);
}
