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

	private List<Long> accessibleUsers;

	private List<Long> sharedUsers;

	private String webSocketSessionId;

	private String password;

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

	public List<Long> getAccessibleUsers() {
		return accessibleUsers;
	}

	public void setAccessibleUsers(List<Long> accessibleUsers) {
		this.accessibleUsers = accessibleUsers;
	}

	public List<Long> getSharedUsers() {
		return sharedUsers;
	}

	public void setSharedUsers(List<Long> sharedUsers) {
		this.sharedUsers = sharedUsers;
	}

	public String getWebSocketSessionId() {
		return webSocketSessionId;
	}

	public void setWebSocketSessionId(String webSocketSessionId) {
		this.webSocketSessionId = webSocketSessionId;
	}


	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}
}
