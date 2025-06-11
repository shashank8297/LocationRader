package com.location.rader.kafka;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.location.rader.model.User;
import com.location.rader.repository.UserRepositoty;

@Service
public class KafkaLocationsSentToUI {

	@Autowired
	private UserRepositoty userRepositoty;

	@Autowired
	private SimpMessagingTemplate messagingTemplate;

	@KafkaListener(topics = "location_updates", groupId = "location-ui-group")
	public void consumeLocationUpdate(String message) {
		try {
			ObjectMapper mapper = new ObjectMapper();
			JsonNode node = mapper.readTree(message);
			Long sendingUserId = node.get("userId").asLong();

			Optional<User> sendingUser = userRepositoty.findById(sendingUserId);
			if (sendingUser.isPresent()) {
				List<Long> reciversUserIds = sendingUser.get().getSharedUsers();
				for (Long reciversUserId : reciversUserIds) {
					String destination = "/topic/coordinates/" + reciversUserId;
					messagingTemplate.convertAndSend(destination, message);
				}
			}

		} catch (Exception e) {
			System.err.println("Error processing message: " + message);
		    e.printStackTrace();
		}
	}
}
