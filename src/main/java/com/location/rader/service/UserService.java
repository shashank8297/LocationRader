package com.location.rader.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.location.rader.model.User;
import com.location.rader.repository.UserRepositoty;

@Service
public class UserService {

	@Autowired
	private UserRepositoty userRepositoty;

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

			return userRepositoty.save(newUser);
		}
	}

	public User saveWhichUserCanAccesOtherLocations(User user) {
		Optional<User> userDeatails = userRepositoty.findById(user.getUserId());

		List<Long> accessList = new ArrayList<>();
		for (int i = 0; i < user.getUsersLocationsCanAccess().size(); i++) {
			accessList.add(user.getUsersLocationsCanAccess().get(i));
		}
		userDeatails.get().setUsersLocationsCanAccess(accessList);
		User saved = userRepositoty.save(userDeatails.get());

		for (int j = 0; j < saved.getUsersLocationsCanAccess().size(); j++) {
			Optional<User> userDetailsOfAccessList = userRepositoty.findById(saved.getUsersLocationsCanAccess().get(j));
			if (userDetailsOfAccessList.isPresent()) {
				List<Long> getsAccessList = userDetailsOfAccessList.get().getUsersLocationGetsAccess();
				if (getsAccessList == null) {
					getsAccessList = new ArrayList<>();
				}
				// Avoid duplicates
				if (!getsAccessList.contains(saved.getUserId())) {
					getsAccessList.add(saved.getUserId());
					userDetailsOfAccessList.get().setUsersLocationGetsAccess(getsAccessList);
					userRepositoty.save(userDetailsOfAccessList.get());
				}
			}
		}
		return saved;
	}
}
