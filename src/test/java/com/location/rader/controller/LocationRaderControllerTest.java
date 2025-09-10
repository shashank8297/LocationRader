package com.location.rader.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.location.rader.model.Coordinates;
import com.location.rader.model.User;
import com.location.rader.service.LocationRaderService;
import com.location.rader.service.UserService;
import com.location.rader.utils.EndpointsConstants;
import org.jose4j.jwk.Use;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class LocationRaderControllerTest {

    private MockMvc mockMvc;

    @Mock
    private UserService userService;

    @Mock
    private LocationRaderService locationRaderService;

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

}
