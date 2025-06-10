package com.location.rader.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
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
	private ObjectMapper objectMapper;

	public ResponseEntity<String> responseEntity;

	@PostMapping("/register")
	public ResponseEntity<String> registerNewUser(@RequestBody User user) throws JsonProcessingException {
		User createdUser = userService.createNewUser(user);
		if (createdUser == null) {
			return ResponseEntity.status(409).body("User already exists... \ntry with another userId.");
		}
		return ResponseEntity.ok("new user is added...\n"
				+ objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(createdUser));
	}

	@PostMapping("/login")
	public  ResponseEntity<?> userLogin(@RequestBody User user){
		Boolean validate = userService.validatingCredentials(user);
		if(validate){
			return ResponseEntity.ok("Login successful for userId: " + user.getUserId());
		}
		return ResponseEntity.status(401).body("Invalid credentials. Please check userId or password.");
	}

	@PostMapping("/location")
	public ResponseEntity<String> location(@RequestBody Coordinates coordinates) {
		System.out.println("Get mapping location");
		System.out.println("Latitude: " + coordinates.getLatitude());
		System.out.println("Longitude " + coordinates.getLongitude());
		Coordinates savedCoordinates = locationRaderService.getLocation(coordinates);
		return ResponseEntity.ok("Location saved to history.");
	}

	@GetMapping("/history")
	public ResponseEntity<?> history(@RequestParam("userId") Long userId) {
		List<Coordinates> coordinatesHistory = locationRaderService.getLocationHistory(userId);
		if (coordinatesHistory == null) {
			return ResponseEntity.status(404).body("User '" + userId + "' don't exist.");
		}
		return ResponseEntity.ok(coordinatesHistory);
	}

	@PostMapping("/locationAccess")
	public ResponseEntity<String> accessLocationOfOtherUsers(@RequestBody User user) throws JsonProcessingException {
		User listOfUserCanAccessOtherUsers = userService.saveWhichUserCanAccesOtherLocations(user);
		return ResponseEntity.ok("Access Granted for User: " + user.getUserId() + "following users "
				+ objectMapper.writerWithDefaultPrettyPrinter()
						.writeValueAsString(listOfUserCanAccessOtherUsers.getUsersLocationsCanAccess()));
	}

	@GetMapping("/CoordinatedOfOtherUser")
	public ResponseEntity<String> getCoordinatedOfOtherUser(@RequestParam("userId") Long userId) {

		locationRaderService.sendCoordinatesToUser(userId);

		return responseEntity;

	}

	@GetMapping("/userHaveAccessTo")
	public ResponseEntity<?> userHaveAccessTo(@RequestParam("userId") Long userId){
		List<Long> listOfUserIds =  locationRaderService.listOfUserHaveAccess(userId);
		if (listOfUserIds == null) {
			return ResponseEntity.status(404).body("Empty...");
		}
		return ResponseEntity.ok(listOfUserIds);
	}

}
