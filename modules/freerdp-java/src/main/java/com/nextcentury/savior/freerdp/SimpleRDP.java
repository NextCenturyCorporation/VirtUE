package com.nextcentury.savior.freerdp;

import java.awt.GraphicsEnvironment;

import javax.swing.SwingUtilities;

public class SimpleRDP implements Runnable {

	
	public static class SessionRunner extends ClientThreadRunner {
		/*
		 * It initiates the connection, and will continue to run until the session ends,
		 * processing events as they are received.
		 */
		@Override
		public boolean apply(rdp_freerdp instance) {
			// TODO
			return true;
		}

	}

	public static class SimpleClientEntryPoints extends ClientEntryPoints {

		@Override
		public boolean globalInit() {
			return true;
		}

		@Override
		public void globalUninit() {
		}

		@Override
		public boolean clientNew(rdp_freerdp instance, ClientContext context) {
			if (GraphicsEnvironment.isHeadless()) {
				System.err.println("cannot run w/o a display");
				return false;
			}
			// TODO set up instance callbacks, make sure we can talk to the local display
			
			return true;
		}

		@Override
		public void clientFree(rdp_freerdp instance, ClientContext context) {
			// TODO clean up
		}

		@Override
		public int clientStart(ClientContext context) {
			ClientThreadRunner runner = new SessionRunner();
			int retval = context.createThread(runner) ? 0 : 1;
			return retval;
		}

		@Override
		public int clientStop(ClientContext context) {
			freerdpmodule.freerdp_abort_connect(context.getInstance());
			return context.waitForThread();
		}

	}

	public SimpleRDP() {
		
	}
	
	public static void main(String[] args) {
		new SimpleRDP().run();
	}

	@Override
	public void run() {
		ClientEntryPoints entryPoints = new SimpleClientEntryPoints();
		ClientContext clientContext = new ClientContext(entryPoints);
		if (clientContext.start() != 0) {
			throw new RuntimeException("Failed to start client context");
		}
		int retcode = clientContext.waitForThread();
		if (retcode != 0) {
			throw new RuntimeException("error waiting for client thread: " + retcode);
		}
	}

}
