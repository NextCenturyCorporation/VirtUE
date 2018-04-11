package com.nextcentury.savior.freerdp;

import org.junit.Assert;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class FreeRDPWrapperTest {

	@BeforeAll
	static void setUpBeforeClass() throws Exception {
		System.loadLibrary("jfreerdp");
	}

	@Test
	void testContextNew() {
		FreeRDPWrapper rdp = new FreeRDPWrapper();
		rdp_context context = new rdp_context();
		Assert.assertTrue(rdp.contextNew(context));
	}

	@Test
	void testContextFree() {
		FreeRDPWrapper rdp = new FreeRDPWrapper();
		rdp_context context = new rdp_context();
		Assert.assertTrue(rdp.contextNew(context));
		rdp.contextFree(context);
	}

	@Test
	void testPreConnect() {
		FreeRDPWrapper rdp = new FreeRDPWrapper();
		rdp_context context = new rdp_context();
		rdp.contextNew(context);
		
		Callback cb = new Callback();
		rdp.registerPreConnect(cb);
		rdp_freerdp internal = rdp.getInstance();
		
		freerdpmodule.connect(internal);
		Assert.assertTrue(cb.preConnectCalled);
	}

	public static class Callback extends BoolCallback {
		boolean preConnectCalled = false;

		@Override
		public boolean apply(rdp_freerdp instance) {
			preConnectCalled = true;
			return true;
		}
	}
}
