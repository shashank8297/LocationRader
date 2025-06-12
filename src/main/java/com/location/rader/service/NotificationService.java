package com.location.rader.service;

import com.location.rader.model.Notification;
import com.location.rader.model.User;
import com.location.rader.repository.NotificationRepository;
import com.location.rader.repository.UserRepositoty;
import com.location.rader.utils.NotificationRequestStatus;
import jakarta.persistence.EntityNotFoundException;
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

    @Autowired
    private UserRepositoty userRepositoty;

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

    public void accepctNotifications(UUID requestID){
        Optional<Notification> notification = notificationRepository.findById(requestID);

        if(notification.isPresent()){
            Notification requestedNotification = notification.get();
            Optional<User> userWhoGotTheNotification = userRepositoty.findById(requestedNotification.getTargetUserId()); //We are in User who got the request
            if(userWhoGotTheNotification.isPresent()){
                User targetUser = userWhoGotTheNotification.get();
                if(targetUser.getSharedUsers() == null && targetUser.getSharedUsers().isEmpty()){
                    List<Long> sharedList = new ArrayList<>();
                    sharedList.add(requestedNotification.getCurrentUserId()); //userId who send the request.
                    targetUser.setSharedUsers(sharedList);
                }else {
                    List<Long> sharedUserIDs = targetUser.getSharedUsers();
                    sharedUserIDs.add(requestedNotification.getCurrentUserId());
                    targetUser.setSharedUsers(sharedUserIDs);
                }
                userRepositoty.save(targetUser);
            }
            Optional<User> userWhoSendTheNotofication = userRepositoty.findById(requestedNotification.getCurrentUserId());
            if(userWhoSendTheNotofication.isPresent()){
                User currentUser = userWhoSendTheNotofication.get();
                if(currentUser.getAccessibleUsers() == null && currentUser.getAccessibleUsers().isEmpty()){
                    List<Long> accessList = new ArrayList<>();
                    accessList.add(requestedNotification.getTargetUserId());
                    currentUser.setAccessibleUsers(accessList);
                }else {
                    List<Long> accessUserIDs = currentUser.getAccessibleUsers();
                    accessUserIDs.add(requestedNotification.getTargetUserId());
                    currentUser.setAccessibleUsers(accessUserIDs);
                }
                userRepositoty.save(currentUser);
            }
            requestedNotification.setStatus(NotificationRequestStatus.ACCEPTED);
            notificationRepository.save(requestedNotification);
        }else {
            throw new EntityNotFoundException("Notification not found with ID: " + requestID);
        }
    }
}

