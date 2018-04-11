package com.nextcentury.savior.freerdp;

import java.awt.GraphicsEnvironment;

public class SimpleRDP implements Runnable {

	
	public static class SessionRunner extends ClientThreadRunner {
		/*
		 * It initiates the connection, and will continue to run until the session ends,
		 * processing events as they are received.
		 */
		@Override
		public boolean apply(rdp_freerdp instance) {
			// TODO
			if (!freerdpmodule.connect(instance)) {
				long error = freerdpmodule.get_last_error(instance.getContext());
				String errorMessage = freerdpmodule.get_last_error_string(error);
				System.err.println("connection error: " + errorMessage);
				return false;
			}
			
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
			// TODO clean upn
			
		}

		@Override
		public int clientStart(ClientContext context) {
			// TODO validate inputs (e.g., check that the remote hostname is set)
			ClientThreadRunner runner = new SessionRunner();
			int retval = context.createThread(runner) ? 0 : 1;
			return retval;
		}

		@Override
		public int clientStop(ClientContext context) {
			freerdpmodule.abort_connect(context.getInstance());
			return context.waitForThread();
		}

	}

	public SimpleRDP() {
		System.loadLibrary("jfreerdp");
	}
	
	public static void main(String[] args) {
		try {
			new SimpleRDP().run();
		}
		catch (Throwable t) {
			System.exit(1);
		}
		System.exit(0);
	}

	@Override
	public void run() {
		ClientEntryPoints entryPoints = new SimpleClientEntryPoints();
		ClientContext clientContext = new ClientContext(entryPoints);
		rdp_settings settings = clientContext.getSettings();
		settings.setServerHostname("ec2-52-71-214-44.compute-1.amazonaws.com");
		settings.setUsername("Administrator");
		settings.setPassword("9J;dIaPTPTyYIZ(.CqykmPHgfSWvdYv)");
		// set TLS security
		settings.setRdpSecurity(false);
		settings.setTlsSecurity(true);
		settings.setNlaSecurity(false);
		settings.setExtSecurity(false);
		
		if (clientContext.start() != 0) {
			throw new RuntimeException("Failed to start client context");
		}
		int retcode = clientContext.waitForThread();
		if (retcode != 0) {
			throw new RuntimeException("error waiting for client thread: " + retcode);
		}
	}

}
