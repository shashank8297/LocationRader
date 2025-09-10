package com.location.rader.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.location.rader.model.User;
import com.location.rader.repository.UserRepositoty;

@Service
public class UserService {

	@Autowired
	private UserRepositoty userRepositoty;

	@Autowired
	private PasswordEncoder passwordEncoder;

	public User createNewUser(User user) {

		Optional<User> isExist = userRepositoty.findById(user.getUserId());

		if (isExist.isPresent()) {
			return null;
		} else {
			User newUser = new User();
			newUser.setUserId((long) user.getUserId());
			newUser.setFullName(user.getFullName());
			newUser.seteMailAddress(user.geteMailAddress());
			newUser.setMobileNumber(user.getMobileNumber());
			newUser.setPassword(passwordEncoder.encode(user.getPassword()));
			newUser.setAccessibleUsers(new ArrayList<>());
			newUser.setSharedUsers(new ArrayList<>());

			return userRepositoty.save(newUser);
		}
	}

	public User userDetails(Long userId){
		Optional<User> userDetails = userRepositoty.findById(userId);
		if(userDetails.isPresent()){
            return userDetails.get();
		}else{
			throw new EntityNotFoundException("User not found with ID: " + userId);
		}
	}

	public boolean validatingCredentials(User user){
		Optional<User> userDetails = userRepositoty.findById(user.getUserId());
		if(userDetails.isPresent()){
			String storedPassword = userDetails.get().getPassword();
			String loginPassword = user.getPassword();
			return passwordEncoder.matches(loginPassword, storedPassword);
		}
		else {
			return false;
		}
	}

	public User saveWhichUserCanAccesOtherUsersLocations(User user) {
		User currentUser = userRepositoty.findById(user.getUserId())
				.orElseThrow(() -> new EntityNotFoundException("User not found with ID: " + user.getUserId()));

		// Copy accessible users from request
		List<Long> accessList = new ArrayList<>(user.getAccessibleUsers());
		currentUser.setAccessibleUsers(accessList);
		User savedUser = userRepositoty.save(currentUser);

		// Update shared users for each user in the access list
		for (Long accessibleUserId : savedUser.getAccessibleUsers()) {
			userRepositoty.findById(accessibleUserId).ifPresent(accessUser -> {
				List<Long> sharedUsers = accessUser.getSharedUsers();
				if (sharedUsers == null) sharedUsers = new ArrayList<>();

				if (!sharedUsers.contains(savedUser.getUserId())) {
					sharedUsers.add(savedUser.getUserId());
					accessUser.setSharedUsers(sharedUsers);
					userRepositoty.save(accessUser);
				}
			});
		}

		return savedUser;
	}


	public List<User> usersList(){
		List<User> users = userRepositoty.findAll();
		if(!users.isEmpty()){
			return users;
		}
		return new ArrayList<>();
	}
}
