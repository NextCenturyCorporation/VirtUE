package com.nextcentury.savior.freerdp;

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.lang.management.ManagementFactory;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class ClientContextTest {

	static class DefaultClientEntryPoints extends ClientEntryPoints {
		
		public boolean globalInitCalled;
		public boolean globalUninitCalled;
		public boolean clientStartCalled;
		public ClientContext startContext;
		public boolean clientStopCalled;
		public ClientContext stopContext;

		@Override
		public boolean globalInit() {
			System.out.println("*** globalInit");
			globalInitCalled = true;
			return true;
		}

		@Override
		public void globalUninit() {
			globalUninitCalled = true;
		}

		@Override
		public boolean clientNew(rdp_freerdp instance, ClientContext context) {
			return true;
		}

		@Override
		public void clientFree(rdp_freerdp instance, ClientContext context) {
		}

		@Override
		public int clientStart(ClientContext context) {
			System.out.println("*** clientStart: " + context);
			clientStartCalled = true;
			startContext = context;
			return 0;
		}

		@Override
		public int clientStop(ClientContext context) {
			System.out.println("*** clientStop: " + context);
			clientStopCalled = true;
			stopContext = context;
			return 0;
		}		
	}
	
	@BeforeAll
	static void setUpBeforeClass() throws Exception {
		System.loadLibrary("jfreerdp");
		System.out.println(ManagementFactory.getRuntimeMXBean().getName());
	}

	@Test
	void testClientContextClientEntryPoints() {
		System.out.println(">>>testClientContextClientEntryPoints");
		ClientEntryPoints entryPoints = null;
		new ClientContext(entryPoints);
		System.out.println("<<<testClientContextClientEntryPoints");
	}

	@Test
	void testGetSettings() {
		ClientContext clientContext = new ClientContext(null);
		rdp_settings settings = clientContext.getSettings();
		assertNotNull(settings, "getSettings returned null");
	}

	@Test
	void testStart() {
		DefaultClientEntryPoints entryPoints = new DefaultClientEntryPoints();
		ClientContext clientContext = new ClientContext(entryPoints);
		int retval = clientContext.start();
		assertEquals(0, retval);
		assertTrue("clientStart was not called", entryPoints.clientStartCalled);
		assertEquals(clientContext, entryPoints.startContext);
	}

	@Test
	void testStop() {
		DefaultClientEntryPoints entryPoints = new DefaultClientEntryPoints();
		ClientContext clientContext = new ClientContext(entryPoints);
		clientContext.start();

		int stopRetval = clientContext.stop();
		assertEquals(0, stopRetval);
		assertTrue("clientStop was not called", entryPoints.clientStopCalled);
		assertEquals(clientContext, entryPoints.stopContext);
	}

}
