package com.ncc.savior.virtueadmin.model;

import java.util.ArrayList;
import java.util.Date;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.session.SessionInformation;
import org.springframework.security.core.userdetails.User;

/**
 * Model object that represents a session.
 */
public class VirtueSession {

	private String sessionId;
	private String username;
	private ArrayList<GrantedAuthority> authorities;
	private Date lastRequest;
	private boolean expired;

	public VirtueSession(String sessionId, String username, ArrayList<GrantedAuthority> auths, Date lastRequest,
			boolean expired) {
		this.sessionId = sessionId;
		this.username = username;
		this.authorities = auths;
		this.lastRequest = lastRequest;
		this.expired = expired;
	}

	public static VirtueSession fromSessionInformation(SessionInformation session) {
		Date lastRequest = session.getLastRequest();
		// User principal = (User) session.getPrincipal();
		String sessionId = session.getSessionId();
		// String username = principal.getUsername();
		String username = (String) session.getPrincipal();
		// ArrayList<GrantedAuthority> auths = new ArrayList<GrantedAuthority>(principal.getAuthorities());
		ArrayList<GrantedAuthority> auths = new ArrayList<GrantedAuthority>();
		VirtueSession vs = new VirtueSession(sessionId, username, auths, lastRequest, session.isExpired());
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

	public boolean isExpired() {
		return expired;
	}

	@Override
	public String toString() {
		return "VirtueSession [sessionId=" + sessionId + ", username=" + username + ", authorities=" + authorities
				+ ", lastRequest=" + lastRequest + ", expired=" + expired + "]";
	}
}
