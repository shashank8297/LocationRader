package com.location.rader.service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import com.location.rader.model.Notification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.location.rader.kafka.KafkaLocationsSentToUI;
import com.location.rader.kafka.LocationSendToKafka;
import com.location.rader.model.Coordinates;
import com.location.rader.model.User;
import com.location.rader.repository.LocationRaderRepository;
import com.location.rader.repository.UserRepositoty;

@Service
public class LocationRaderService {

    private final KafkaLocationsSentToUI kafkaLocationsSentToUI;

	@Autowired
	private LocationRaderRepository locationRaderRepository;

	@Autowired
	private UserRepositoty userRepositoty;

	@Autowired
	private LocationSendToKafka locationSendToKafka;
	
	@Autowired
	private KafkaLocationsSentToUI kafkaLocationsSentToUI2;

    LocationRaderService(KafkaLocationsSentToUI kafkaLocationsSentToUI) {
        this.kafkaLocationsSentToUI = kafkaLocationsSentToUI;
    }

	public Coordinates getLocation(Coordinates coordinates) {

		// Always create and save a new coordinate for history tracking
		Coordinates newCoordinate = new Coordinates();
		newCoordinate.setUserId(coordinates.getUserId());
		newCoordinate.setLatitude(coordinates.getLatitude());
		newCoordinate.setLongitude(coordinates.getLongitude());
		newCoordinate.setTimestamp(Instant.now());

		Coordinates savedCoordinates = locationRaderRepository.save(newCoordinate);

		locationSendToKafka.sendLocationUpdate(savedCoordinates);

		return savedCoordinates;
	}

	public List<Coordinates> getLocationHistory(Long userId) {
		Optional<User> isExist = userRepositoty.findById(userId);
		if (isExist.isPresent()) {
			List<Coordinates> hiatory = locationRaderRepository.findByUserIdOrderByTimestampDesc(userId);
			return hiatory;
		} else {
			return null;
		}
	}

	public void sendCoordinatesToUser(Long userId) {
		Optional<User> isExist = userRepositoty.findById(userId);
		List<Long> canAccess = isExist.get().getAccessibleUsers();
		
		//kafkaLocationsSentToUI.consumeLocationUpdate(canAccess);
	}

	public List<Long> listOfUserHaveAccess(Long userId){
		Optional<User> listOfUsers = userRepositoty.findById(userId);
		if(listOfUsers.isPresent()){
			List<Long> userIds = listOfUsers.get().getAccessibleUsers();
			return userIds;
		}
		return Collections.emptyList();
	}


}
