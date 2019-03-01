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
		FileShare share = new FileShare("aname", "ID", "server", "/path",
				Collections.singleton(FileShare.SharePermissions.READ), FileShare.ShareType.CIFS);
		share.initExportedName(Collections.emptySet());
		assertThrows(IllegalStateException.class, () -> share.initExportedName(Collections.emptySet()));
	}

	@Test
	void testInitExportedName_simple() throws Exception {
		String simpleName = "MyPrinter";
		FileShare share = new FileShare(simpleName, "ID", "server", "/path",
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
		FileShare share2 = new FileShare(simpleName, "ID", "server", "/path",
				Collections.singleton(FileShare.SharePermissions.READ), FileShare.ShareType.CIFS);
		share2.initExportedName(Collections.singleton(share));
		assertEquals(simpleName + "2", share2.getExportedName());
	}

	@Test
	void testInitExportedName_tooLong() throws Exception {
		String longName = "APrinterNameThatIsWayTooLong9012345678901234567890123456789012345678901234567890PastTheEnd";
		assertThat(longName.length(), greaterThan(FileShare.MAX_SHARE_NAME_LENGTH));
		FileShare share = new FileShare(longName, "ID", "server", "/path",
				Collections.singleton(FileShare.SharePermissions.READ), FileShare.ShareType.CIFS);
		share.initExportedName(Collections.emptySet());
		assertEquals(longName.substring(0, FileShare.MAX_SHARE_NAME_LENGTH), share.getExportedName());
	}
}
