package com.location.rader.controller;

import java.util.List;
import java.util.Map;

import com.location.rader.model.Notification;
import com.location.rader.service.NotificationService;
import com.location.rader.utils.EndpointsConstants;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.location.rader.model.Coordinates;
import com.location.rader.model.User;
import com.location.rader.service.LocationRaderService;
import com.location.rader.service.UserService;

@RestController
@CrossOrigin(origins = "*")
public class LocationRaderController {

	@Autowired
	private LocationRaderService locationRaderService;

	@Autowired
	private UserService userService;

	@Autowired
	private NotificationService notificationService;

	@Autowired
	private ObjectMapper objectMapper;

	/*public ResponseEntity<String> responseEntity;*/

	@PostMapping(EndpointsConstants.NEW_USER_REGISTER_ENDPOINT)
	public ResponseEntity<User> registerNewUser(@RequestBody User user) {
		User createdUser = userService.createNewUser(user);
		if (createdUser == null) {
			return ResponseEntity.status(409).build(); // or return a custom error DTO
		}
		return ResponseEntity.ok(createdUser);
	}


	@PostMapping(EndpointsConstants.LOGIN_ENDPOINT)
	public ResponseEntity<String> userLogin(@RequestBody User user) {
		boolean validate = userService.validatingCredentials(user);
		if (validate) {
			return ResponseEntity.ok("Login successful for userId: " + user.getUserId());
		}
		return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
				.body("Invalid credentials. Please check userId or password.");
	}


	@GetMapping(EndpointsConstants.USER_DETAILS_ENDPOINT)
	public ResponseEntity<User> userDetails(@RequestParam("userId") Long userId){
		User user = userService.userDetails(userId);
		if(user == null){
			return ResponseEntity.notFound().build();
		}
		return ResponseEntity.ok(user);
	}

	@PostMapping(EndpointsConstants.LOCATION_UPDATE_ENDPOINT)
	public ResponseEntity<String> location(@RequestBody Coordinates coordinates) {
		locationRaderService.saveLocation(coordinates);
		return ResponseEntity.ok("Location saved to history.");
	}

	@GetMapping(EndpointsConstants.USER_HISTORY_ENDPOINT)
	public ResponseEntity<List<Coordinates>> history(@RequestParam("userId") Long userId) {
		List<Coordinates> coordinatesHistory = locationRaderService.getLocationHistory(userId);
		if (coordinatesHistory == null) {
			return ResponseEntity.notFound().build();
		}
		return ResponseEntity.ok(coordinatesHistory);
	}


	@PostMapping(EndpointsConstants.REQUEST_FOR_LOCATION_ACCESS_ENDPOINT)
	public ResponseEntity<User> accessLocationOfOtherUsers(@RequestBody User user) throws JsonProcessingException {
		User updatedUser = userService.saveWhichUserCanAccesOtherUsersLocations(user);
		if (updatedUser != null) {
			return ResponseEntity.ok(updatedUser);
		} else {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
		}
	}

	/*@GetMapping(EndpointsConstants.COORDINATES_OF_OTHER_USER_ENDPOINT)
	public ResponseEntity<String> getCoordinatesOfOtherUser(@RequestParam("userId") Long userId) {

		locationRaderService.sendCoordinatesToUser(userId);

		return responseEntity;

	}*/

	@GetMapping(EndpointsConstants.USER_HAVE_ACCESS_TO_ENDPOINT)
	public ResponseEntity<List<Long>> userHaveAccessTo(@RequestParam("userId") Long userId) {
		List<Long> listOfUserIds = locationRaderService.listOfUserHaveAccess(userId);

		if (listOfUserIds == null || listOfUserIds.isEmpty()) {
			return ResponseEntity.notFound().build();
		}

		return ResponseEntity.ok(listOfUserIds);
	}

	@PostMapping(EndpointsConstants.NEW_NOTIFICATION_ENDPOINT)
	public ResponseEntity<Notification> newNotification(@RequestBody Notification notification) {
		Notification createdNotification = notificationService.savingNotificationRequestes(notification);

		if (createdNotification != null) {
			return ResponseEntity.ok(createdNotification);
		} else {
			return ResponseEntity.status(HttpStatus.CONFLICT).build();
		}
	}

	@GetMapping(EndpointsConstants.GET_PENDING_NOTIFICATIONS_ENDPOINT)
	public ResponseEntity<List<Notification>> listOfPendingNotification(@RequestParam("userId") Long userId) {
		List<Notification> listOfNotifications = notificationService.pendingNotifications(userId);

		if (listOfNotifications == null || listOfNotifications.isEmpty()) {
			return ResponseEntity.noContent().build(); // 204, no body
		}

		return ResponseEntity.ok(listOfNotifications);
	}


	@PostMapping(EndpointsConstants.ACCEPT_LOCATION_REQUEST_ENDPOINT)
	public ResponseEntity<String> acceptTheLocationRequest(@RequestBody Notification notification){
		try {
			notificationService.accepctNotifications(notification.getRequestId());
			return ResponseEntity.ok("Notification accepted successfully.");
		} catch (EntityNotFoundException e) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Notification not found: " + e.getMessage());
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error while accepting notification.");
		}
	}

	@PostMapping(EndpointsConstants.REJECTED_LOCATION_REQUEST_ENDPOINT)
	public ResponseEntity<String> rejectTheLocationRequest(@RequestBody Notification notification){
		try{
			notificationService.rejectNotifications(notification.getRequestId());
			return ResponseEntity.ok("Notification rejected successfully.");
		}catch (EntityNotFoundException e) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Notification not found: " + e.getMessage());
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error while accepting notification.");
		}
	}

	@GetMapping(EndpointsConstants.LIST_OF_ALL_USERS_ENDPOINT)
	public ResponseEntity<List<User>> listOfAllUsers(){
		return  ResponseEntity.ok(userService.usersList());
	}

	@GetMapping(EndpointsConstants.ACCEPT_HISTORY_ENDPOINT)
	public ResponseEntity<List<Notification>> acceptedRequestHistory(@RequestParam("currentUserId") Long currentUserId){
		List<Notification> acceptHistory = notificationService.acceptHistory(currentUserId);
		return ResponseEntity.ok(acceptHistory);
	}

	@GetMapping(EndpointsConstants.REJECT_HISTORY_ENDPOINT)
	public ResponseEntity<List<Notification>> rejectRequestHistory(@RequestParam("currentUserId") Long currentUserId){
		List<Notification> rejectHistory = notificationService.rejectHistory(currentUserId);
		return ResponseEntity.ok(rejectHistory);
	}

	@GetMapping(EndpointsConstants.ALL_NOTIFICATIONS)
	public Map<String, List<Notification>>  getAllNotifications(@RequestParam("userId") Long userId){
		return notificationService.allNotifications(userId);
	}

}
