package com.location.rader.utils;

import org.springframework.stereotype.Component;

@Component
public final class LocationRaderConstants {

	private LocationRaderConstants() {
		// Prevent instantiation
		throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
	}
	public static final String LOCATION_RADER_KAFKA_BOOTSTRAP_SERVER = "192.168.7.57:9092";
	
	public static final String KAFKA_TOPIC_LOCATION_UPDATES = "location_updates";
}
