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
		Callback cb = makeCallback();
		rdp_freerdp internal = cb.getInstance();
		freerdp.freerdp_connect(internal);
		Assert.assertTrue(cb.preConnectCalled);
	}

	private Callback makeCallback() {
		Callback cb = new Callback();
		rdp_context context = new rdp_context();
		cb.contextNew(context);
		return cb;
	}
	
	public static class Callback extends FreeRDPWrapper {
		boolean preConnectCalled = false;

		@Override
		public boolean preConnect() {
			preConnectCalled = true;
			return true;
		}

	}
}
