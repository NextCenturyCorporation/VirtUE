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

import com.nextcentury.savior.cifsproxy.model.Virtue;
import com.nextcentury.savior.cifsproxy.services.VirtueService;

@RestController
public class VirtueController {
	private static final XLogger LOGGER = XLoggerFactory.getXLogger(VirtueController.class);

	@Autowired
	private VirtueService service;

	@PostMapping("/virtue")
	Virtue newVirtue(@RequestBody Virtue virtue) {
		LOGGER.entry(virtue);
		try {
			service.newVirtue(virtue);
		} catch (IllegalArgumentException | IOException e) {
			WebServiceException wse = new WebServiceException("exception while creating a new Virtue", e);
			LOGGER.throwing(wse);
			throw wse;
		}
		LOGGER.exit(virtue);
		return virtue;
	}
	
	@GetMapping("/virtue")
	List<Virtue> getVirtues() {
		LOGGER.entry();
		ArrayList<Virtue> virtues = new ArrayList<>(service.getVirtues());
		LOGGER.exit(virtues);
		return virtues;
	}

	@GetMapping("/virtue/{id}")
	Virtue getVirtue(@PathVariable String id) {
		LOGGER.entry(id);
		Virtue virtue = service.getVirtue(id);
		LOGGER.exit(virtue);
		return virtue;
	}

	@DeleteMapping("/virtue/{id}")
	void removeVirtue(@PathVariable String id) {
		LOGGER.entry(id);
		try {
			service.removeVirtue(id);
		} catch (IllegalArgumentException | IOException e) {
			WebServerException wse = new WebServerException("exception removing a Virtue", e);
			LOGGER.throwing(wse);
			throw wse;
		}
		LOGGER.exit();
	}
}
