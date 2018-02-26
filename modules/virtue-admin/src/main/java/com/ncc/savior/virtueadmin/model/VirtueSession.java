package com.ncc.savior.virtueadmin.model;

import java.util.ArrayList;
import java.util.Date;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.session.SessionInformation;
import org.springframework.security.core.userdetails.User;

public class VirtueSession {

	private String sessionId;
	private String username;
	private ArrayList<GrantedAuthority> authorities;
	private Date lastRequest;

	public VirtueSession(String sessionId, String username, ArrayList<GrantedAuthority> auths, Date lastRequest) {
		this.sessionId = sessionId;
		this.username = username;
		this.authorities = auths;
		this.lastRequest = lastRequest;
	}

	public static VirtueSession fromSessionInformation(SessionInformation session) {
		Date lastRequest = session.getLastRequest();
		User principal = (User) session.getPrincipal();
		String sessionId = session.getSessionId();
		String username = principal.getUsername();
		ArrayList<GrantedAuthority> auths = new ArrayList<GrantedAuthority>(principal.getAuthorities());
		VirtueSession vs = new VirtueSession(sessionId, username, auths, lastRequest);
		return vs;
	}

	public String getSessionId() {
		return sessionId;
	}

	public String getUsername() {
		return username;
	}

	public ArrayList<GrantedAuthority> getAuthorities() {
		return authorities;
	}

	public Date getLastRequest() {
		return lastRequest;
	}

	@Override
	public String toString() {
		return "VirtueSession [sessionId=" + sessionId + ", username=" + username + ", authorities=" + authorities
				+ ", lastRequest=" + lastRequest + "]";
	}
}
