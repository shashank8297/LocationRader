package com.location.rader.repository;

import com.location.rader.model.Notification;
import com.location.rader.utils.NotificationRequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, UUID> {
    List<Notification> findByTargetUserIdAndStatus(Long userId, NotificationRequestStatus notificationRequestStatus);

    List<Notification> findByCurrentUserIdAndStatus(Long userId, NotificationRequestStatus notificationRequestStatus);

    Optional<Notification> findByCurrentUserIdAndTargetUserIdAndStatus(Long currentUserId, Long targetUserId, NotificationRequestStatus status);

    Optional<List<Notification>> findAllByCurrentUserIdAndStatus(Long currentUserId, NotificationRequestStatus status);
}
