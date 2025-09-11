package com.location.rader.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.location.rader.model.Coordinates;
import com.location.rader.model.Notification;
import com.location.rader.model.User;
import com.location.rader.service.LocationRaderService;
import com.location.rader.service.NotificationService;
import com.location.rader.service.UserService;
import com.location.rader.utils.EndpointsConstants;
import com.location.rader.utils.NotificationRequestStatus;
import jakarta.persistence.EntityNotFoundException;
import org.checkerframework.checker.units.qual.N;
import org.jose4j.jwk.Use;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.internal.matchers.Null;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.*;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import static org.hamcrest.Matchers.containsString;


public class LocationRaderControllerTest {

    private MockMvc mockMvc;

    @Mock
    private UserService userService;

    @Mock
    private LocationRaderService locationRaderService;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private LocationRaderController locationRaderController;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(locationRaderController).build();
        objectMapper = new ObjectMapper(); // real ObjectMapper for request serialization
        objectMapper.registerModule(new JavaTimeModule());
    }

    @Test
    void testRegisterNewUser_Success() throws Exception {
        User user = new User();
        user.setUserId(1L);
        user.setFullName("user1");
        user.seteMailAddress("user1@gmail.ccom");
        user.setMobileNumber("1234567890");
        user.setPassword("password");

        when(userService.createNewUser(any(User.class))).thenReturn(user);

        mockMvc.perform(post(EndpointsConstants.NEW_USER_REGISTER_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(1L))
                .andExpect(jsonPath("$.fullName").value("user1"))
                .andExpect(jsonPath("$.eMailAddress").value("user1@gmail.ccom"))
                .andExpect(jsonPath("$.mobileNumber").value("1234567890"))
                .andExpect(jsonPath("$.password").value("password"));

        verify(userService, times(1)).createNewUser(any(User.class));
    }

    @Test
    void testRegisterNewUser_existUser() throws Exception {
        User user = new User();
        user.setUserId(1L);
        user.setFullName("user1");
        user.seteMailAddress("user1@gmail.ccom");
        user.setMobileNumber("1234567890");
        user.setPassword("password");

        when(userService.createNewUser(any(User.class))).thenReturn(null);

        mockMvc.perform(post(EndpointsConstants.NEW_USER_REGISTER_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isConflict());

        verify(userService, times(1)).createNewUser(any(User.class));
    }

    @Test
    void testUserLogin_ValidCred()  throws Exception{
        User user = new User();
        user.setUserId(1L);
        user.setPassword("password");

        when(userService.validatingCredentials(any(User.class))).thenReturn(true);

        mockMvc.perform(post(EndpointsConstants.LOGIN_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isOk());
        verify(userService, times(1)).validatingCredentials(any(User.class));
    }

    @Test
    void testUserLogin_InValidCred()  throws Exception{
        User user = new User();
        user.setUserId(1L);
        user.setPassword("password");

        when(userService.validatingCredentials(any(User.class))).thenReturn(false);

        mockMvc.perform(post(EndpointsConstants.LOGIN_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isUnauthorized());
        verify(userService, times(1)).validatingCredentials(any(User.class));
    }

    @Test
    void testUserDetails_IfUserExist() throws Exception{
        User user = new User();
        user.setUserId(1L);
        user.setFullName("user1");
        user.seteMailAddress("user1@gmail.com");
        user.setMobileNumber("1234567890");
        user.setPassword("password");

        when(userService.userDetails(user.getUserId())).thenReturn(user);

        mockMvc.perform(get(EndpointsConstants.USER_DETAILS_ENDPOINT)
                        .param("userId", String.valueOf(user.getUserId())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(1L))
                .andExpect(jsonPath("$.password").value("password"));
        verify(userService, times(1)).userDetails(user.getUserId());
    }

    @Test
    void testUserDetails_IfNotUserExist() throws Exception{
        User user = new User();
        user.setUserId(1L);
        user.setFullName("user1");
        user.seteMailAddress("user1@gmail.com");
        user.setMobileNumber("1234567890");
        user.setPassword("password");

        when(userService.userDetails(user.getUserId())).thenReturn(null);

        mockMvc.perform(get(EndpointsConstants.USER_DETAILS_ENDPOINT)
                        .param("userId", String.valueOf(user.getUserId())))
                .andExpect(status().isNotFound());
        verify(userService, times(1)).userDetails(user.getUserId());
    }

    @Test
    void testLocation() throws Exception {
        Coordinates coordinates = new Coordinates();
        coordinates.setLatitude(12.34);
        coordinates.setLongitude(56.78);
        coordinates.setUserId(1L);
        coordinates.setTimestamp(Instant.now());

        // Mock service to return the same coordinates (or a saved copy)
        when(locationRaderService.saveLocation(any(Coordinates.class)))
                .thenReturn(coordinates);

        mockMvc.perform(post(EndpointsConstants.LOCATION_UPDATE_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(coordinates)))
                .andExpect(status().isOk())
                .andExpect(content().string("Location saved to history."));

        verify(locationRaderService, times(1)).saveLocation(any(Coordinates.class));
    }

    @Test
    void testHistory_IsExist() throws Exception {
        User user = new User();
        user.setUserId(1L);
        user.setFullName("user1");
        user.seteMailAddress("user1@gmail.com");
        user.setMobileNumber("1234567890");
        user.setPassword("password");

        Coordinates coordinates = new Coordinates();
        coordinates.setLatitude(12.34);
        coordinates.setLongitude(56.78);
        coordinates.setUserId(1L);
        coordinates.setTimestamp(Instant.now());

        when(locationRaderService.getLocationHistory(user.getUserId())).thenReturn(List.of(coordinates));

        mockMvc.perform(get(EndpointsConstants.USER_HISTORY_ENDPOINT)
                        .param("userId", String.valueOf(user.getUserId())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].userId").value(1L))
                .andExpect(jsonPath("$[0].latitude").value(12.34))
                .andExpect(jsonPath("$[0].longitude").value(56.78));

        verify(locationRaderService, times(1)).getLocationHistory(user.getUserId());
    }

    @Test
    void testHistory_IsNotExist() throws Exception {
        User user = new User();
        user.setUserId(1L);
        user.setFullName("user1");
        user.seteMailAddress("user1@gmail.com");
        user.setMobileNumber("1234567890");
        user.setPassword("password");

        Coordinates coordinates = new Coordinates();
        coordinates.setLatitude(12.34);
        coordinates.setLongitude(56.78);
        coordinates.setUserId(1L);
        coordinates.setTimestamp(Instant.now());

        when(locationRaderService.getLocationHistory(user.getUserId())).thenReturn(null);

        mockMvc.perform(get(EndpointsConstants.USER_HISTORY_ENDPOINT)
                        .param("userId", String.valueOf(user.getUserId())))
                .andExpect(status().isNotFound());

        verify(locationRaderService, times(1)).getLocationHistory(user.getUserId());
    }

    @Test
    void testAccessLocationOfOtherUsers_IsExist() throws Exception {
        User user = new User();
        user.setUserId(1L);
        user.setFullName("user1");
        user.seteMailAddress("user1@gmail.com");
        user.setMobileNumber("1234567890");
        user.setPassword("password");

        // Accessible users list
        List<Long> accessibleUser = new ArrayList<>();
        accessibleUser.add(2L);
        accessibleUser.add(3L);

        user.setAccessibleUsers(accessibleUser);

        // Mock service behavior
        when(userService.saveWhichUserCanAccesOtherUsersLocations(any(User.class)))
                .thenReturn(user);

        mockMvc.perform(post(EndpointsConstants.REQUEST_FOR_LOCATION_ACCESS_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessibleUsers[0]").value(2L))
                .andExpect(jsonPath("$.accessibleUsers[1]").value(3L));

        verify(userService, times(1)).saveWhichUserCanAccesOtherUsersLocations(any(User.class));
    }

    @Test
    void testAccessLocationOfOtherUsers_IsNotExist() throws Exception {
        User user = new User();
        user.setUserId(1L);
        user.setFullName("user1");
        user.seteMailAddress("user1@gmail.com");
        user.setMobileNumber("1234567890");
        user.setPassword("password");

        // Accessible users list
        List<Long> accessibleUser = new ArrayList<>();
        accessibleUser.add(2L);
        accessibleUser.add(3L);

        user.setAccessibleUsers(accessibleUser);

        // Mock service behavior
        when(userService.saveWhichUserCanAccesOtherUsersLocations(any(User.class)))
                .thenReturn(null);

        mockMvc.perform(post(EndpointsConstants.REQUEST_FOR_LOCATION_ACCESS_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isNotFound());

        verify(userService, times(1)).saveWhichUserCanAccesOtherUsersLocations(any(User.class));
    }

    @Test
    void tsetUserHaveAccessTo_IsNotEmpty() throws Exception {
        User user = new User();
        user.setUserId(1L);
        user.setFullName("user1");
        user.seteMailAddress("user1@gmail.com");
        user.setMobileNumber("1234567890");
        user.setPassword("password");

        // Accessible users list
        List<Long> accessibleUser = new ArrayList<>();
        accessibleUser.add(2L);
        accessibleUser.add(3L);

        user.setAccessibleUsers(accessibleUser);

        List<Long> sharedUser = new ArrayList<>();
        sharedUser.add(3L);
        user.setSharedUsers(sharedUser);

        when(locationRaderService.listOfUserHaveAccess(user.getUserId())).thenReturn(sharedUser);

        mockMvc.perform(get(EndpointsConstants.USER_HAVE_ACCESS_TO_ENDPOINT)
                    .param("userId",String.valueOf(user.getUserId())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0]").value(3L));

        verify(locationRaderService, times(1)).listOfUserHaveAccess(user.getUserId());
    }

    @Test
    void tsetUserHaveAccessTo_IsNull() throws Exception {
        User user = new User();
        user.setUserId(1L);
        user.setFullName("user1");
        user.seteMailAddress("user1@gmail.com");
        user.setMobileNumber("1234567890");
        user.setPassword("password");

        // Accessible users list
        List<Long> accessibleUser = new ArrayList<>();
        accessibleUser.add(2L);
        accessibleUser.add(3L);

        user.setAccessibleUsers(accessibleUser);

        List<Long> sharedUser = new ArrayList<>();
        sharedUser.add(3L);
        user.setSharedUsers(sharedUser);

        when(locationRaderService.listOfUserHaveAccess(user.getUserId())).thenReturn(null);

        mockMvc.perform(get(EndpointsConstants.USER_HAVE_ACCESS_TO_ENDPOINT)
                        .param("userId",String.valueOf(user.getUserId())))
                .andExpect(status().isNotFound());

        verify(locationRaderService, times(1)).listOfUserHaveAccess(user.getUserId());
    }

    @Test
    void tsetUserHaveAccessTo_IsEmpty() throws Exception {
        User user = new User();
        user.setUserId(1L);
        user.setFullName("user1");
        user.seteMailAddress("user1@gmail.com");
        user.setMobileNumber("1234567890");
        user.setPassword("password");

        // Accessible users list
        List<Long> accessibleUser = new ArrayList<>();
        accessibleUser.add(2L);
        accessibleUser.add(3L);

        user.setAccessibleUsers(accessibleUser);

        List<Long> sharedUser = new ArrayList<>();
        sharedUser.add(3L);
        user.setSharedUsers(sharedUser);

        when(locationRaderService.listOfUserHaveAccess(user.getUserId())).thenReturn(new ArrayList<>());

        mockMvc.perform(get(EndpointsConstants.USER_HAVE_ACCESS_TO_ENDPOINT)
                        .param("userId",String.valueOf(user.getUserId())))
                .andExpect(status().isNotFound());

        verify(locationRaderService, times(1)).listOfUserHaveAccess(user.getUserId());
    }

    @Test
    void testnewNotification_FirstTime() throws Exception {
        Notification notification = new Notification();
        notification.setStatus(NotificationRequestStatus.PENDING);
        notification.setRequestId(UUID.fromString("5fc03087-d265-11e7-b8c6-83e29cd24f4c"));
        notification.setTargetUserId(2L);
        notification.setCurrentUserId(1L);

        when(notificationService.savingNotificationRequestes(any(Notification.class))).thenReturn(notification);

        mockMvc.perform(post(EndpointsConstants.NEW_NOTIFICATION_ENDPOINT)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(notification)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.targetUserId").value(2L))
                .andExpect(jsonPath("$.currentUserId").value(1L))
                .andExpect(jsonPath("$.status").value(String.valueOf(NotificationRequestStatus.PENDING)))
                .andExpect(jsonPath("$.requestId").value("5fc03087-d265-11e7-b8c6-83e29cd24f4c"));

        verify(notificationService, times(1)).savingNotificationRequestes(any(Notification.class));

    }

    @Test
    void testnewNotification_SameNotificationSecondTime() throws Exception {
        Notification notification = new Notification();
        notification.setStatus(NotificationRequestStatus.PENDING);
        notification.setRequestId(UUID.fromString("5fc03087-d265-11e7-b8c6-83e29cd24f4c"));
        notification.setTargetUserId(2L);
        notification.setCurrentUserId(1L);

        when(notificationService.savingNotificationRequestes(any(Notification.class))).thenReturn(null);

        mockMvc.perform(post(EndpointsConstants.NEW_NOTIFICATION_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(notification)))
                .andExpect(status().isConflict());

        verify(notificationService, times(1)).savingNotificationRequestes(any(Notification.class));

    }

    @Test
    void testListOfPendingNotification_NotEmpty() throws Exception {
        List<Notification> notificationPendingList = new ArrayList<>();

        Notification notification1 = new Notification();
        notification1.setStatus(NotificationRequestStatus.PENDING);
        notification1.setRequestId(UUID.fromString("5fc03087-d265-11e7-b8c6-83e29cd24f4c"));
        notification1.setTargetUserId(3L);
        notification1.setCurrentUserId(4L);

        Notification notification2 = new Notification();
        notification2.setStatus(NotificationRequestStatus.PENDING);
        notification2.setRequestId(UUID.fromString("078c8017-0616-4924-821c-810e0e036a5a"));
        notification2.setTargetUserId(3L);
        notification2.setCurrentUserId(2L);

        notificationPendingList.add(notification1);
        notificationPendingList.add(notification2);

        when(notificationService.pendingNotifications(3L)).thenReturn(notificationPendingList);

        mockMvc.perform(get(EndpointsConstants.GET_PENDING_NOTIFICATIONS_ENDPOINT)
                    .param("userId", String.valueOf(3L)))
                .andExpect(jsonPath(".[0].requestId").value("5fc03087-d265-11e7-b8c6-83e29cd24f4c"))
                .andExpect(jsonPath(".[1].requestId").value("078c8017-0616-4924-821c-810e0e036a5a"));

        verify(notificationService, times(1)).pendingNotifications(3L);
    }

    @Test
    void testListOfPendingNotification_IsEmpty() throws Exception {
        List<Notification> notificationPendingList = new ArrayList<>();

        Notification notification1 = new Notification();
        notification1.setStatus(NotificationRequestStatus.PENDING);
        notification1.setRequestId(UUID.fromString("5fc03087-d265-11e7-b8c6-83e29cd24f4c"));
        notification1.setTargetUserId(3L);
        notification1.setCurrentUserId(4L);

        Notification notification2 = new Notification();
        notification2.setStatus(NotificationRequestStatus.PENDING);
        notification2.setRequestId(UUID.fromString("078c8017-0616-4924-821c-810e0e036a5a"));
        notification2.setTargetUserId(3L);
        notification2.setCurrentUserId(2L);

        notificationPendingList.add(notification1);
        notificationPendingList.add(notification2);

        when(notificationService.pendingNotifications(3L)).thenReturn(new ArrayList<>());

        mockMvc.perform(get(EndpointsConstants.GET_PENDING_NOTIFICATIONS_ENDPOINT)
                        .param("userId", String.valueOf(3L)))
                .andExpect(status().isNoContent());

        verify(notificationService, times(1)).pendingNotifications(3L);
    }

    @Test
    void testListOfPendingNotification_IsNull() throws Exception {
        List<Notification> notificationPendingList = new ArrayList<>();

        Notification notification1 = new Notification();
        notification1.setStatus(NotificationRequestStatus.PENDING);
        notification1.setRequestId(UUID.fromString("5fc03087-d265-11e7-b8c6-83e29cd24f4c"));
        notification1.setTargetUserId(3L);
        notification1.setCurrentUserId(4L);

        Notification notification2 = new Notification();
        notification2.setStatus(NotificationRequestStatus.PENDING);
        notification2.setRequestId(UUID.fromString("078c8017-0616-4924-821c-810e0e036a5a"));
        notification2.setTargetUserId(3L);
        notification2.setCurrentUserId(2L);

        notificationPendingList.add(notification1);
        notificationPendingList.add(notification2);

        when(notificationService.pendingNotifications(3L)).thenReturn(null);

        mockMvc.perform(get(EndpointsConstants.GET_PENDING_NOTIFICATIONS_ENDPOINT)
                        .param("userId", String.valueOf(3L)))
                .andExpect(status().isNoContent());

        verify(notificationService, times(1)).pendingNotifications(3L);
    }

    @Test
    void testAcceptTheLocationRequest() throws Exception {
        UUID requestId = UUID.fromString("5fc03087-d265-11e7-b8c6-83e29cd24f4c");

        Notification notification = new Notification();
        notification.setStatus(NotificationRequestStatus.ACCEPTED);
        notification.setRequestId(requestId);
        notification.setTargetUserId(2L);
        notification.setCurrentUserId(1L);

        mockMvc.perform(post(EndpointsConstants.ACCEPT_LOCATION_REQUEST_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(notification)))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Notification accepted successfully.")));

        verify(notificationService, times(1)).accepctNotifications(requestId);
    }

    @Test
    void testAcceptTheLocationRequest_EntityNotFound() throws Exception {
        UUID requestId = UUID.fromString("5fc03087-d265-11e7-b8c6-83e29cd24f4c");

        Notification notification = new Notification();
        notification.setStatus(NotificationRequestStatus.PENDING);
        notification.setRequestId(requestId);
        notification.setTargetUserId(2L);
        notification.setCurrentUserId(1L);

        doThrow(new EntityNotFoundException("Notification not found with ID: " + requestId))
                .when(notificationService).accepctNotifications(requestId);

        mockMvc.perform(post(EndpointsConstants.ACCEPT_LOCATION_REQUEST_ENDPOINT)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(notification)))
                .andExpect(status().isNotFound())
                .andExpect(content().string(containsString("Notification not found with ID: " + requestId)));

        verify(notificationService, times(1)).accepctNotifications(requestId);
    }

    @Test
    void testAcceptTheLocationRequest_Exception() throws Exception {
        UUID requestId = UUID.fromString("5fc03087-d265-11e7-b8c6-83e29cd24f4c");

        Notification notification = new Notification();
        notification.setStatus(NotificationRequestStatus.PENDING);
        notification.setRequestId(requestId);
        notification.setTargetUserId(2L);
        notification.setCurrentUserId(1L);

        doThrow(new RuntimeException("Error while accepting notification."))
                .when(notificationService).accepctNotifications(requestId);

        mockMvc.perform(post(EndpointsConstants.ACCEPT_LOCATION_REQUEST_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(notification)))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string(containsString("Error while accepting notification.")));

        verify(notificationService, times(1)).accepctNotifications(requestId);
    }

    @Test
    void testRejectTheLocationRequest() throws Exception {
        UUID requestId = UUID.fromString("5fc03087-d265-11e7-b8c6-83e29cd24f4c");

        Notification notification = new Notification();
        notification.setStatus(NotificationRequestStatus.REJECTED);
        notification.setRequestId(requestId);
        notification.setTargetUserId(2L);
        notification.setCurrentUserId(1L);

        mockMvc.perform(post(EndpointsConstants.REJECTED_LOCATION_REQUEST_ENDPOINT)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(notification)))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Notification rejected successfully.")));

        verify(notificationService, times(1)).rejectNotifications(requestId);
    }

    @Test
    void testRejectTheLocationRequest_EntityNotFound() throws Exception {
        UUID requestId = UUID.fromString("5fc03087-d265-11e7-b8c6-83e29cd24f4c");

        Notification notification = new Notification();
        notification.setStatus(NotificationRequestStatus.REJECTED);
        notification.setRequestId(requestId);
        notification.setTargetUserId(2L);
        notification.setCurrentUserId(1L);

        doThrow(new EntityNotFoundException("Notification not found with ID: " + requestId))
                .when(notificationService).rejectNotifications(requestId);

        mockMvc.perform(post(EndpointsConstants.REJECTED_LOCATION_REQUEST_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(notification)))
                .andExpect(status().isNotFound())
                .andExpect(content().string(containsString("Notification not found with ID: " + requestId)));

        verify(notificationService, times(1)).rejectNotifications(requestId);
    }

    @Test
    void testRejectTheLocationRequest_Exception() throws Exception {
        UUID requestId = UUID.fromString("5fc03087-d265-11e7-b8c6-83e29cd24f4c");

        Notification notification = new Notification();
        notification.setStatus(NotificationRequestStatus.REJECTED);
        notification.setRequestId(requestId);
        notification.setTargetUserId(2L);
        notification.setCurrentUserId(1L);

        doThrow(new RuntimeException("Error while accepting notification."))
                .when(notificationService).rejectNotifications(requestId);

        mockMvc.perform(post(EndpointsConstants.REJECTED_LOCATION_REQUEST_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(notification)))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string(containsString("Error while accepting notification.")));

        verify(notificationService, times(1)).rejectNotifications(requestId);
    }

    @Test
    void testListOfAllUsers() throws Exception{
        List<User> userList = new ArrayList<>();

        User user1= new User();
        user1.setUserId(1L);
        user1.setFullName("user1");
        user1.seteMailAddress("user1@gmail.ccom");
        user1.setMobileNumber("1111111111");
        user1.setPassword("password@1");

        User user2= new User();
        user2.setUserId(2L);
        user2.setFullName("user2");
        user2.seteMailAddress("user2@gmail.ccom");
        user2.setMobileNumber("2222222222");
        user2.setPassword("password@2");

        userList.add(user1);
        userList.add(user2);

        when(userService.usersList()).thenReturn(userList);

        mockMvc.perform(get(EndpointsConstants.LIST_OF_ALL_USERS_ENDPOINT))
                .andExpect(status().isOk())
                //.andExpect(content().string(containsString()))
                .andExpect(jsonPath("[0].userId").value(1L))
                .andExpect(jsonPath("[1].userId").value(2L));

        verify(userService, times(1)).usersList();
    }

    @Test
    void testAcceptedRequestHistory() throws Exception{
        List<Notification> acceptedHistoryList = new ArrayList<>();
        UUID resouceId1 = UUID.fromString("002a8d8b-a6e3-4644-9d5a-d930dca1d5d1");
        UUID resouceId2 = UUID.fromString("a5048687-f218-4cf7-a38f-c306af14b457");

        Notification notification1 = new Notification();
        notification1.setTargetUserId(2L);
        notification1.setCurrentUserId(1L);
        notification1.setRequestId(resouceId1);
        notification1.setStatus(NotificationRequestStatus.ACCEPTED);

        Notification notification2 = new Notification();
        notification2.setTargetUserId(3L);
        notification2.setCurrentUserId(1L);
        notification2.setRequestId(resouceId2);
        notification2.setStatus(NotificationRequestStatus.ACCEPTED);

        acceptedHistoryList.add(notification1);
        acceptedHistoryList.add(notification2);

        when(notificationService.acceptHistory(1L)).thenReturn(acceptedHistoryList);

        mockMvc.perform(get(EndpointsConstants.ACCEPT_HISTORY_ENDPOINT)
                    .param("currentUserId", String.valueOf(1L)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("[0].requestId").value(String.valueOf(resouceId1)))
                .andExpect(jsonPath("[1].requestId").value(String.valueOf(resouceId2)));

        verify(notificationService, times(1)).acceptHistory(1L);
    }

    @Test
    void testRejectRequestHistory() throws Exception{
        List<Notification> rejectHistoryList = new ArrayList<>();
        UUID resouceId1 = UUID.fromString("002a8d8b-a6e3-4644-9d5a-d930dca1d5d1");
        UUID resouceId2 = UUID.fromString("a5048687-f218-4cf7-a38f-c306af14b457");

        Notification notification1 = new Notification();
        notification1.setTargetUserId(2L);
        notification1.setCurrentUserId(1L);
        notification1.setRequestId(resouceId1);
        notification1.setStatus(NotificationRequestStatus.REJECTED);

        Notification notification2 = new Notification();
        notification2.setTargetUserId(3L);
        notification2.setCurrentUserId(1L);
        notification2.setRequestId(resouceId2);
        notification2.setStatus(NotificationRequestStatus.REJECTED);

        rejectHistoryList.add(notification1);
        rejectHistoryList.add(notification2);

        when(notificationService.rejectHistory(1L)).thenReturn(rejectHistoryList);

        mockMvc.perform(get(EndpointsConstants.REJECT_HISTORY_ENDPOINT)
                        .param("currentUserId", String.valueOf(1L)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("[0].requestId").value(String.valueOf(resouceId1)))
                .andExpect(jsonPath("[1].requestId").value(String.valueOf(resouceId2)));

        verify(notificationService, times(1)).rejectHistory(1L);
    }

    @Test
    void testAllNotifications() throws Exception{
        Map<String, List<Notification>> allNotificationHistoryList = new HashMap<>();

        UUID resouceId1 = UUID.fromString("002a8d8b-a6e3-4644-9d5a-d930dca1d5d1");
        UUID resouceId2 = UUID.fromString("a5048687-f218-4cf7-a38f-c306af14b457");
        UUID resouceId3 = UUID.fromString("7b638ce3-27c9-4656-93b7-fdb752d6c1b8");


        Notification notification1 = new Notification();
        notification1.setTargetUserId(2L);
        notification1.setCurrentUserId(1L);
        notification1.setRequestId(resouceId1);
        notification1.setStatus(NotificationRequestStatus.ACCEPTED);

        Notification notification2 = new Notification();
        notification2.setTargetUserId(3L);
        notification2.setCurrentUserId(1L);
        notification2.setRequestId(resouceId2);
        notification2.setStatus(NotificationRequestStatus.REJECTED);

        Notification notification3 = new Notification();
        notification3.setTargetUserId(4L);
        notification3.setCurrentUserId(1L);
        notification3.setRequestId(resouceId3);
        notification3.setStatus(NotificationRequestStatus.PENDING);

        allNotificationHistoryList.put(String.valueOf(NotificationRequestStatus.ACCEPTED), List.of(notification1));
        allNotificationHistoryList.put(String.valueOf(NotificationRequestStatus.REJECTED), List.of(notification2));
        allNotificationHistoryList.put(String.valueOf(NotificationRequestStatus.PENDING), List.of(notification3));

        when(notificationService.allNotifications(1L)).thenReturn(allNotificationHistoryList);

        mockMvc.perform(get(EndpointsConstants.ALL_NOTIFICATIONS)
                        .param("userId", String.valueOf(1L)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.PENDING[0].requestId").value(resouceId3.toString()))
                .andExpect(jsonPath("$.PENDING[0].status").value("PENDING"))
                .andExpect(jsonPath("$.ACCEPTED[0].requestId").value(resouceId1.toString()))
                .andExpect(jsonPath("$.ACCEPTED[0].status").value("ACCEPTED"))
                .andExpect(jsonPath("$.REJECTED[0].requestId").value(resouceId2.toString()))
                .andExpect(jsonPath("$.REJECTED[0].status").value("REJECTED"));


        verify(notificationService, times(1)).allNotifications(1L);
    }
}
