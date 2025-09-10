package com.location.rader.service;

import java.util.Optional;

import com.location.rader.controller.WebSocketController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.location.rader.model.User;
import com.location.rader.repository.UserRepositoty;

@Service
public class WebSocketSessionService {
	
	@Autowired
	private UserRepositoty userRepositoty;

	private static final Logger log = LoggerFactory.getLogger(WebSocketSessionService.class);


	public void mappingSessionIdToUserId(Long userId, String sessionId) {
		try {
			Optional<User> isExist = userRepositoty.findById(userId);
			if (isExist.isPresent()) {
				User user = isExist.get();
				user.setWebSocketSessionId(sessionId);
				userRepositoty.save(user);
			} else {
				log.error("User not found with ID: {}", userId);
			}
		} catch (Exception e) {
			log.error("Error while mapping session to user", e);
		}
	}
}
