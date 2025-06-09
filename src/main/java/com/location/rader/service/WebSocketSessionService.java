package com.location.rader.service;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.location.rader.model.User;
import com.location.rader.repository.UserRepositoty;

@Service
public class WebSocketSessionService {
	
	@Autowired
	private UserRepositoty userRepositoty;
	
	public void mappingSessionIdToUserId(Long userId, String sessionId) {
		Optional<User> isExist = userRepositoty.findById(userId);
		User user = isExist.get();
		user.setWebSocketSessionId(sessionId);
		userRepositoty.save(user);
	}

}
