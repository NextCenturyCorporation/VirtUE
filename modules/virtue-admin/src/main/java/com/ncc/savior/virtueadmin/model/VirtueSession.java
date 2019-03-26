/*
 * Copyright (C) 2019 Next Century Corporation
 * 
 * This file may be redistributed and/or modified under either the GPL
 * 2.0 or 3-Clause BSD license. In addition, the U.S. Government is
 * granted government purpose rights. For details, see the COPYRIGHT.TXT
 * file at the root of this project.
 * 
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
 * 02110-1301, USA.
 * 
 * SPDX-License-Identifier: (GPL-2.0-only OR BSD-3-Clause)
 */
package com.ncc.savior.virtueadmin.model;

import java.util.ArrayList;
import java.util.Date;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.session.SessionInformation;
import org.springframework.security.core.userdetails.User;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Model object that represents a session.
 */
@Schema(description = "Describes a session in virtue for a user.")
public class VirtueSession {
	@Schema(description = "ID for the session. Also the token for this session.")
	private String sessionId;
	@Schema(description = "User who owns this session.")
	private String username;
	@Schema(description = "Authorities or groups that the user is assigned to.  These authorities ties to access to the savior system and not access to certain virtues.")
	private ArrayList<GrantedAuthority> authorities;
	@Schema(description = "Date of the last request made on the session.")
	private Date lastRequest;
	@Schema(description = "Boolean as whether or not the session has expired.")
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
		Object principal = session.getPrincipal();
		String username;
		ArrayList<GrantedAuthority> auths = null;
		if (principal instanceof User) {
			username = ((User) principal).getUsername();
			auths = new ArrayList<GrantedAuthority>(((User) principal).getAuthorities());
		} else {
			username = (String) principal;
		}
		String sessionId = session.getSessionId();
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
