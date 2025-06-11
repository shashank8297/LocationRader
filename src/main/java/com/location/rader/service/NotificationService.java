package com.location.rader.service;

import com.location.rader.model.Notification;
import com.location.rader.repository.NotificationRepository;
import com.location.rader.utils.NotificationRequestStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class NotificationService {

    @Autowired
    private NotificationRepository notificationRepository;

    public Notification savingNotificationRequestes(Notification notification){
        Notification newNotification = new Notification();
        newNotification.setRequestId(UUID.randomUUID());
        newNotification.setCurrentUserId(notification.getCurrentUserId());
        newNotification.setTargetUserId(notification.getTargetUserId());
        newNotification.setStatus(NotificationRequestStatus.PENDING);

        return notificationRepository.save(newNotification);
    }

    public List<Notification> pendingNotifications(Long userId){
         List<Notification> pendingList =  notificationRepository.findByCurrentUserIdAndStatus(userId, NotificationRequestStatus.PENDING);
         return pendingList;
    }
}

