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

	public Boolean validatingCredentials(User user){
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
		Optional<User> userDeatails = userRepositoty.findById(user.getUserId());

		List<Long> accessList = new ArrayList<>();
		for (int i = 0; i < user.getAccessibleUsers().size(); i++) {
			accessList.add(user.getAccessibleUsers().get(i));
		}
		userDeatails.get().setAccessibleUsers(accessList);
		User saved = userRepositoty.save(userDeatails.get());

		for (int j = 0; j < saved.getAccessibleUsers().size(); j++) {
			Optional<User> userDetailsOfAccessList = userRepositoty.findById(saved.getAccessibleUsers().get(j));
			if (userDetailsOfAccessList.isPresent()) {
				List<Long> getsAccessList = userDetailsOfAccessList.get().getSharedUsers();
				if (getsAccessList == null) {
					getsAccessList = new ArrayList<>();
				}
				// Avoid duplicates
				if (!getsAccessList.contains(saved.getUserId())) {
					getsAccessList.add(saved.getUserId());
					userDetailsOfAccessList.get().setSharedUsers(getsAccessList);
					userRepositoty.save(userDetailsOfAccessList.get());
				}
			}
		}
		return saved;
	}

	public List<User> usersList(){
		List<User> users = userRepositoty.findAll();
		if(!users.isEmpty()){
			return users;
		}
		return new ArrayList<>();
	}
}
