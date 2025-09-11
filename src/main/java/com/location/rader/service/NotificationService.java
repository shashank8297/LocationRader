package com.location.rader.service;

import com.location.rader.model.Notification;
import com.location.rader.model.User;
import com.location.rader.repository.NotificationRepository;
import com.location.rader.repository.UserRepositoty;
import com.location.rader.utils.NotificationRequestStatus;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class NotificationService {

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private UserRepositoty userRepositoty;

    public Notification savingNotificationRequestes(Notification notification){
        Optional<Notification> checkForDublicateNotification = notificationRepository.findByCurrentUserIdAndTargetUserIdAndStatus(
                notification.getCurrentUserId(), notification.getTargetUserId(), NotificationRequestStatus.PENDING);
        if(!checkForDublicateNotification.isPresent()){
            Notification newNotification = new Notification();
            newNotification.setRequestId(UUID.randomUUID());
            newNotification.setCurrentUserId(notification.getCurrentUserId());
            newNotification.setTargetUserId(notification.getTargetUserId());
            newNotification.setStatus(NotificationRequestStatus.PENDING);

            return notificationRepository.save(newNotification);
        }else {
            return null;
        }
    }

    public List<Notification> pendingNotifications(Long userId){
        return notificationRepository.findByTargetUserIdAndStatus(userId, NotificationRequestStatus.PENDING);
    }

    public void accepctNotifications(UUID requestId) {
        Notification notification = notificationRepository.findById(requestId)
                .orElseThrow(() -> new EntityNotFoundException("Notification not found with ID: " + requestId));

        updateSharedUsers(notification.getTargetUserId(), notification.getCurrentUserId());
        updateAccessibleUsers(notification.getCurrentUserId(), notification.getTargetUserId());

        notification.setStatus(NotificationRequestStatus.ACCEPTED);
        notificationRepository.save(notification);
    }

    private void updateSharedUsers(Long targetUserId, Long currentUserId) {
        User targetUser = userRepositoty.findById(targetUserId).orElseThrow(
                () -> new EntityNotFoundException("User not found with ID: " + targetUserId)
        );

        List<Long> sharedUsers = targetUser.getSharedUsers();
        if (sharedUsers == null) sharedUsers = new ArrayList<>();

        if (!sharedUsers.contains(currentUserId)) {
            sharedUsers.add(currentUserId);
            targetUser.setSharedUsers(sharedUsers);
            userRepositoty.save(targetUser);
        }
    }

    private void updateAccessibleUsers(Long currentUserId, Long targetUserId) {
        User currentUser = userRepositoty.findById(currentUserId).orElseThrow(
                () -> new EntityNotFoundException("User not found with ID: " + currentUserId)
        );

        List<Long> accessibleUsers = currentUser.getAccessibleUsers();
        if (accessibleUsers == null) accessibleUsers = new ArrayList<>();

        if (!accessibleUsers.contains(targetUserId)) {
            accessibleUsers.add(targetUserId);
            currentUser.setAccessibleUsers(accessibleUsers);
            userRepositoty.save(currentUser);
        }
    }


    public void rejectNotifications(UUID requestID){
        Optional<Notification> notification = notificationRepository.findById(requestID);
        if(notification.isPresent()){
            Notification requestedNotificationForReject = notification.get();
            requestedNotificationForReject.setStatus(NotificationRequestStatus.REJECTED);
            notificationRepository.save(requestedNotificationForReject);
        }else {
            throw new EntityNotFoundException("Notification not found with ID: " + requestID);
        }
    }

    public List<Notification> acceptHistory(Long currentUserId) {
        Optional<List<Notification>> optionalAcceptHistory = notificationRepository.findAllByCurrentUserIdAndStatus(currentUserId, NotificationRequestStatus.ACCEPTED);
        return optionalAcceptHistory.orElseGet(ArrayList::new);
    }

    public List<Notification> rejectHistory(Long currentUserId) {
        Optional<List<Notification>> optionalRejectHistory = notificationRepository.findAllByCurrentUserIdAndStatus(currentUserId, NotificationRequestStatus.REJECTED);
        return optionalRejectHistory.orElseGet(ArrayList::new);
    }

    public Map<String, List<Notification>> allNotifications(Long userId){
        Optional<List<Notification>> allNotificationsList = notificationRepository.findAllByCurrentUserId(userId);
        Map<String, List<Notification>> result = new HashMap<>();
        if(allNotificationsList.isPresent()) {
            List<Notification> notifications = allNotificationsList.get();
            result.put("PENDING", new ArrayList<>());
            result.put("ACCEPTED", new ArrayList<>());
            result.put("REJECTED", new ArrayList<>());

            for (Notification N : notifications) {
                switch (N.getStatus()) {
                    case PENDING : result.get("PENDING").add(N);
                    case ACCEPTED : result.get("ACCEPTED").add(N);
                    case REJECTED : result.get("REJECTED").add(N);
                    //case PENDING -> result.get("PENDING").add(N);
                   // case ACCEPTED -> result.get("ACCEPTED").add(N);
                    //case REJECTED -> result.get("REJECTED").add(N);
                }
            }
        }
        return result;
    }
}