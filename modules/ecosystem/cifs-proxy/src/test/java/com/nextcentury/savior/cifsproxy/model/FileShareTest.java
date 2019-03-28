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
package com.nextcentury.savior.cifsproxy.model;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Collections;

import org.junit.jupiter.api.Test;

class FileShareTest {

	@Test
	void testInitExportedName_reinit() {
		SambaService share = new FileShare("aname", "ID", "server", "/path",
				Collections.singleton(FileShare.SharePermissions.READ), FileShare.ShareType.CIFS);
		share.initExportedName(Collections.emptySet());
		assertThrows(IllegalStateException.class, () -> share.initExportedName(Collections.emptySet()));
	}

	@Test
	void testInitExportedName_simple() throws Exception {
		String simpleName = "MyPrinter";
		SambaService share = new FileShare(simpleName, "ID", "server", "/path",
				Collections.singleton(FileShare.SharePermissions.READ), FileShare.ShareType.CIFS);
		share.initExportedName(Collections.emptySet());
		assertEquals(simpleName, share.getExportedName());
	}

	@Test
	void testInitExportedName_duplicate() throws Exception {
		String simpleName = "MyPrinter";
		FileShare share = new FileShare(simpleName, "ID", "server", "/path",
				Collections.singleton(FileShare.SharePermissions.READ), FileShare.ShareType.CIFS);
		share.initExportedName(Collections.emptySet());
		SambaService share2 = new FileShare(simpleName, "ID", "server", "/path",
				Collections.singleton(FileShare.SharePermissions.READ), FileShare.ShareType.CIFS);
		share2.initExportedName(Collections.singleton(share.getExportedName()));
		assertEquals(simpleName + "2", share2.getExportedName());
	}

	@Test
	void testInitExportedName_tooLong() throws Exception {
		String longName = "APrinterNameThatIsWayTooLong9012345678901234567890123456789012345678901234567890PastTheEnd";
		assertThat(longName.length(), greaterThan(SambaService.MAX_SHARE_NAME_LENGTH));
		SambaService share = new FileShare(longName, "ID", "server", "/path",
				Collections.singleton(FileShare.SharePermissions.READ), FileShare.ShareType.CIFS);
		share.initExportedName(Collections.emptySet());
		assertEquals(longName.substring(0, SambaService.MAX_SHARE_NAME_LENGTH), share.getExportedName());
	}
}
