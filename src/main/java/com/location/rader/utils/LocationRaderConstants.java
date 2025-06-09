package com.location.rader.utils;

import org.springframework.stereotype.Component;

@Component
public class LocationRaderConstants {
	
	public static final String LOCATION_RADER_KAFKA_BOOTSTRAP_SERVER = "192.168.7.57:9092";
	
	public static final String KAFKA_TOPIC_LOCATION_UPDATES = "location_updates";
}
