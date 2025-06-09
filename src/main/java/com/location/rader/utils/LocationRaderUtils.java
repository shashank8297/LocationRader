package com.location.rader.utils;

import org.springframework.stereotype.Component;

import com.location.rader.model.Coordinates;

@Component
public class LocationRaderUtils {

	public boolean isSameLocation(Coordinates coordinates) {
		double epsilon = 0.000001;
		return Math.abs(coordinates.getLatitude() - coordinates.getLatitude()) < epsilon
				&& Math.abs(coordinates.getLongitude() - coordinates.getLongitude()) < epsilon;
	}
}
