package com.location.rader.model;

import java.util.List;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "user_details")
public class User {

	@Id
	private Long userId;

	private String fullName;

	private String eMailAddress;

	private String mobileNumber;

	private List<Long> usersLocationsCanAccess;

	private List<Long> usersLocationGetsAccess;

	private String webSocketSessionId;

	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	public String getFullName() {
		return fullName;
	}

	public void setFullName(String fullName) {
		this.fullName = fullName;
	}

	public String geteMailAddress() {
		return eMailAddress;
	}

	public void seteMailAddress(String eMailAddress) {
		this.eMailAddress = eMailAddress;
	}

	public String getMobileNumber() {
		return mobileNumber;
	}

	public void setMobileNumber(String mobileNumber) {
		this.mobileNumber = mobileNumber;
	}

	public List<Long> getUsersLocationsCanAccess() {
		return usersLocationsCanAccess;
	}

	public void setUsersLocationsCanAccess(List<Long> usersLocationsCanAccess) {
		this.usersLocationsCanAccess = usersLocationsCanAccess;
	}

	public List<Long> getUsersLocationGetsAccess() {
		return usersLocationGetsAccess;
	}

	public void setUsersLocationGetsAccess(List<Long> usersLocationGetsAccess) {
		this.usersLocationGetsAccess = usersLocationGetsAccess;
	}

	public String getWebSocketSessionId() {
		return webSocketSessionId;
	}

	public void setWebSocketSessionId(String webSocketSessionId) {
		this.webSocketSessionId = webSocketSessionId;
	}

}
