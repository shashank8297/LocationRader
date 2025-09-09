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

	public ResponseEntity<String> responseEntity;

	@PostMapping(EndpointsConstants.NEW_USER_REGISTER_ENDPOINT)
	public ResponseEntity<String> registerNewUser(@RequestBody User user) throws JsonProcessingException {
		User createdUser = userService.createNewUser(user);
		if (createdUser == null) {
			return ResponseEntity.status(409).body("User already exists... \ntry with another userId.");
		}
		return ResponseEntity.ok("new user is added...\n"
				+ objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(createdUser));
	}

	@PostMapping(EndpointsConstants.LOGIN_ENDPOINT)
	public  ResponseEntity<?> userLogin(@RequestBody User user){
		Boolean validate = userService.validatingCredentials(user);
		if(validate){
			return ResponseEntity.ok("Login successful for userId: " + user.getUserId());
		}
		return ResponseEntity.status(401).body("Invalid credentials. Please check userId or password.");
	}

	@GetMapping(EndpointsConstants.USER_DETAILS_ENDPOINT)
	public ResponseEntity<User> userDetails(@RequestParam("userId") Long userId){
		User user =userService.userDetails(userId);
		return ResponseEntity.ok(user);
	}

	@PostMapping(EndpointsConstants.LOCATION_UPDATE_ENDPOINT)
	public ResponseEntity<String> location(@RequestBody Coordinates coordinates) {
		System.out.println("Get mapping location");
		System.out.println("Latitude: " + coordinates.getLatitude());
		System.out.println("Longitude " + coordinates.getLongitude());
		Coordinates savedCoordinates = locationRaderService.getLocation(coordinates);
		return ResponseEntity.ok("Location saved to history.");
	}

	@GetMapping(EndpointsConstants.USER_HISTORY_ENDPOINT)
	public ResponseEntity<?> history(@RequestParam("userId") Long userId) {
		List<Coordinates> coordinatesHistory = locationRaderService.getLocationHistory(userId);
		if (coordinatesHistory == null) {
			return ResponseEntity.status(404).body("User '" + userId + "' don't exist.");
		}
		return ResponseEntity.ok(coordinatesHistory);
	}

	@PostMapping(EndpointsConstants.REQUEST_FOR_LOCATION_ACCESS_ENDPOINT)
	public ResponseEntity<String> accessLocationOfOtherUsers(@RequestBody User user) throws JsonProcessingException {
		User listOfUserCanAccessOtherUsers = userService.saveWhichUserCanAccesOtherUsersLocations(user);
		return ResponseEntity.ok("Access Granted for User: " + user.getUserId() + "following users "
				+ objectMapper.writerWithDefaultPrettyPrinter()
						.writeValueAsString(listOfUserCanAccessOtherUsers.getAccessibleUsers()));
	}

	@GetMapping(EndpointsConstants.COORDINATES_OF_OTHER_USER_ENDPOINT)
	public ResponseEntity<String> getCoordinatesOfOtherUser(@RequestParam("userId") Long userId) {

		locationRaderService.sendCoordinatesToUser(userId);

		return responseEntity;

	}

	@GetMapping(EndpointsConstants.USER_HAVE_ACCESS_TO_ENDPOINT)
	public ResponseEntity<?> userHaveAccessTo(@RequestParam("userId") Long userId){
		List<Long> listOfUserIds =  locationRaderService.listOfUserHaveAccess(userId);
		if (listOfUserIds == null) {
			return ResponseEntity.status(404).body("Empty...");
		}
		return ResponseEntity.ok(listOfUserIds);
	}

	@PostMapping(EndpointsConstants.NEW_NOTIFICATION_ENDPOINT)
	public ResponseEntity<?> newtNotification(@RequestBody Notification notification){
		Notification createdNotification = notificationService.savingNotificationRequestes(notification);

		if (createdNotification != null) {
			return ResponseEntity.ok(createdNotification);
		} else {
			String errorMessage = "A request has already been sent to user ID: "
					+ notification.getTargetUserId()
					+ ". Please wait until they respond.";
			return ResponseEntity.status(HttpStatus.CONFLICT).body(errorMessage);
		}
	}

	@GetMapping(EndpointsConstants.GET_PENDING_NOTIFICATIONS_ENDPOINT)
	public ResponseEntity<?> listOfPendingNotification(@RequestParam("userId") Long userId){
		List<Notification> listOfNotifications = notificationService.pendingNotifications(userId);
		if (listOfNotifications != null && !listOfNotifications.isEmpty()) {
			return ResponseEntity.ok(listOfNotifications);
		} else {
			return ResponseEntity.status(204).body("No pending notifications found for userId: " + userId);
		}
	}

	@PostMapping(EndpointsConstants.ACCEPT_LOCATION_REQUEST_ENDPOINT)
	public ResponseEntity<?> acceptTheLocationRequest(@RequestBody Notification notification){
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
	public ResponseEntity<?> rejectTheLocationRequest(@RequestBody Notification notification){
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
