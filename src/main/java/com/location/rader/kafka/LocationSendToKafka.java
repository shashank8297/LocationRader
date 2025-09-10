package com.location.rader.kafka;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.location.rader.model.Coordinates;
import com.location.rader.utils.LocationRaderConstants;

@Service
public class LocationSendToKafka {

	@Autowired
	private KafkaTemplate<String, String> kafkaTemplate;

	@Autowired
	private ObjectMapper objectMapper;

	public void sendLocationUpdate(Coordinates coordinates) {
		try {
			String jsonMessage = objectMapper.writeValueAsString(coordinates);
			kafkaTemplate.send(LocationRaderConstants.KAFKA_TOPIC_LOCATION_UPDATES, jsonMessage);
		} catch (JsonProcessingException e) {
			e.printStackTrace(); // Use proper logging in production
		}
	}

}
