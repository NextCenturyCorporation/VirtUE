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
package com.nextcentury.savior.cifsproxy.services;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.FileNotFoundException;
import java.io.IOException;

import org.junit.jupiter.api.Test;

import com.nextcentury.savior.cifsproxy.model.Virtue;

class VirtueServiceTest {

	@Test
	void testCreateUsername_simple() throws FileNotFoundException, IOException {
		VirtueService virtueService = new VirtueService();
		Virtue virtue = new Virtue("simple", "simple");
		String username = virtueService.createUsername(virtue);
		assertEquals("simple", username);
	}

	@Test
	void testCreateUsername_upper() throws FileNotFoundException, IOException {
		VirtueService virtueService = new VirtueService();
		Virtue virtue = new Virtue("UpPEr", "test");
		String username = virtueService.createUsername(virtue);
		assertEquals("upper", username);
	}
	
	@Test
	void testCreateUsername_numbers() throws FileNotFoundException, IOException {
		VirtueService virtueService = new VirtueService();
		Virtue virtue = new Virtue("13_thing42", "test");
		String username = virtueService.createUsername(virtue);
		assertEquals("thing42", username);
	}
	
	@Test
	void testCreateUsername_symbols() throws FileNotFoundException, IOException {
		VirtueService virtueService = new VirtueService();
		Virtue virtue = new Virtue("8foo*bar[ish ness-", "test");
		String username = virtueService.createUsername(virtue);
		assertEquals("foo_bar_ish_ness-", username);
	}
	
	@Test
	void testCreateUsername_long() throws FileNotFoundException, IOException {
		VirtueService virtueService = new VirtueService();
		Virtue virtue = new Virtue("a much longer name than we're ever likely to actually have", "test");
		String username = virtueService.createUsername(virtue);
		assertEquals("a_much_longer_name_than_we_re_e", username);
	}

	@Test
	void testCreateUsername_collision() throws FileNotFoundException, IOException {
		VirtueService virtueService = new VirtueService();
		Virtue virtue = new Virtue("a much longer name than we're ever likely to actually have", "test");
		String username = virtueService.createUsername(virtue);

		assertEquals("a_much_longer_name_than_we_re_e", username);
		
		virtue.initUsername(username);
		virtueService.virtuesById.put("v1", virtue);
		username = virtueService.createUsername(virtue);
		assertEquals("a_much_longer_name_than_we_re_2", username);
	}
	
	@Test
	void testCreateUsername_root() throws FileNotFoundException, IOException {
		VirtueService virtueService = new VirtueService();
		Virtue virtue = new Virtue("root", "test");
		String username = virtueService.createUsername(virtue);
		assertEquals("root2", username);
	}
}
