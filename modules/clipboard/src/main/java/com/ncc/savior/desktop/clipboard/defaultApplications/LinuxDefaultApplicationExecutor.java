package com.ncc.savior.desktop.clipboard.defaultApplications;

import java.io.IOException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ncc.savior.desktop.clipboard.messages.DefaultApplicationMessage.DefaultApplicationType;

public class LinuxDefaultApplicationExecutor implements IDefaultApplicationExecutor {
	private static final Logger logger = LoggerFactory.getLogger(LinuxDefaultApplicationExecutor.class);
	@Override
	public void runWithDefaultApplication(DefaultApplicationType type, List<String> arguments) {

		String command;
		switch (type) {
		case BROWSER:
			command = "xdg-open " + arguments.get(0);
			try {
				Runtime.getRuntime().exec(command);
			} catch (IOException e) {
				logger.error("Failed to start default browser");
			}
			break;
		default:
			break;
		}

	}

}
