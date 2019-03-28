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
package com.nextcentury.savior.cifsproxy.controllers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpSession;
import javax.xml.ws.WebServiceException;

import org.slf4j.ext.XLogger;
import org.slf4j.ext.XLoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.server.WebServerException;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.nextcentury.savior.cifsproxy.model.FileShare;
import com.nextcentury.savior.cifsproxy.model.SambaService;
import com.nextcentury.savior.cifsproxy.services.ShareService;

/**
 * Implements the CIFS Proxy REST API as defined in the SAVIOR CIFS Proxy
 * documentation.
 * 
 * @author clong
 *
 */
@RestController
public class ShareController {

	private static final XLogger LOGGER = XLoggerFactory.getXLogger(ShareController.class);

	@Autowired
	private ShareService service;

	@GetMapping("/share")
	List<FileShare> getShares() {
		LOGGER.entry();
		List<FileShare> shares = new ArrayList<>(service.getShares());
		LOGGER.exit(shares);
		return shares;
	}

	@GetMapping("/share/{name}")
	SambaService getShare(@PathVariable String name) {
		LOGGER.entry(name);
		FileShare share = service.getShare(name);
		LOGGER.exit(share);
		return share;
	}

	@PostMapping("/share")
	SambaService newShare(HttpSession session, @RequestBody FileShare share) {
		LOGGER.entry(session, share);
		try {
			service.newShare(session, share);
		} catch (IllegalArgumentException | IOException e) {
			WebServerException wse = new WebServerException("exception mounting a share", e);
			LOGGER.throwing(wse);
			throw wse;
		}
		LOGGER.exit(share);
		return share;
	}

	@DeleteMapping("/share/{name}")
	void removeShare(@PathVariable String name) {
		LOGGER.entry();
		try {
			service.removeShare(name);
		} catch (IllegalArgumentException | IOException e) {
			WebServerException wse = new WebServerException("exception unmounting a share", e);
			LOGGER.throwing(wse);
			throw wse;
		}
		LOGGER.exit();
	}

	@GetMapping("/scan")
	String scan() {
		LOGGER.entry();
		try {
			service.scan();
		} catch (IOException e) {
			WebServiceException wse = new WebServiceException(e);
			LOGGER.throwing(wse);
			throw wse;
		}
		LOGGER.exit("");
		return "";
	}
}
