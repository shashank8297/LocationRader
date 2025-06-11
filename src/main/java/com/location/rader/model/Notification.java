package com.location.rader.model;

import com.location.rader.utils.NotificationRequestStatus;
import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Table(name = "request_notification")
public class Notification {

    @Id
    private UUID requestId;

    private Long currentUserId;

    private  Long targetUserId;

    @Enumerated(EnumType.STRING)
    private NotificationRequestStatus status;

    public UUID getRequestId() {
        return requestId;
    }

    public void setRequestId(UUID requestId) {
        this.requestId = requestId;
    }

    public Long getCurrentUserId() {
        return currentUserId;
    }

    public void setCurrentUserId(Long currentUserId) {
        this.currentUserId = currentUserId;
    }

    public Long getTargetUserId() {
        return targetUserId;
    }

    public void setTargetUserId(Long targetUserId) {
        this.targetUserId = targetUserId;
    }

    public NotificationRequestStatus getStatus() {
        return status;
    }

    public void setStatus(NotificationRequestStatus status) {
        this.status = status;
    }
}
