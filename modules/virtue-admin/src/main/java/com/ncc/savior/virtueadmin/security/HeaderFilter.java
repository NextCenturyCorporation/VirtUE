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
package com.ncc.savior.virtueadmin.security;

import java.io.IOException;
import java.util.ArrayList;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import com.ncc.savior.virtueadmin.model.VirtueUser;

/**
 * Pulls username and some role information from header. Not for production use!
 *
 * The username is given ROLE_USER by default. If the X-admin header is present,
 * the user will be given ROLE_ADMIN. If the X-noroles header is present, the
 * user will get no roles.
 */
public class HeaderFilter extends OncePerRequestFilter {

	@Override
	public void destroy() {
		// do nothing
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		String name = request.getHeader("X-Authorization");
		if (name != null) {
			ArrayList<GrantedAuthority> authorities = new ArrayList<GrantedAuthority>();
			authorities.add(new SimpleGrantedAuthority(VirtueUser.ROLE_USER));
			if (request.getHeader("X-admin") != null) {
				authorities.add(new SimpleGrantedAuthority(VirtueUser.ROLE_ADMIN));
			}
			if (request.getHeader("X-noroles") != null) {
				authorities.clear();
			}

			Authentication authentication = new AbstractAuthenticationToken(authorities) {
				private static final long serialVersionUID = 1L;

				@Override
				public Object getPrincipal() {
					return name;
				}

				@Override
				public Object getCredentials() {
					return name;
				}
			};
			SecurityContextHolder.getContext().setAuthentication(authentication);
		}
		filterChain.doFilter(request, response);
	}

}
