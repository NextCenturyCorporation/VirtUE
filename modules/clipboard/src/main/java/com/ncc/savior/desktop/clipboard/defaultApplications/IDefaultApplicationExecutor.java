package com.ncc.savior.desktop.clipboard.defaultApplications;

import java.util.List;

import com.ncc.savior.desktop.clipboard.messages.DefaultApplicationMessage.DefaultApplicationType;

public interface IDefaultApplicationExecutor {

	public void runWithDefaultApplication(DefaultApplicationType type, List<String> arguments);

}
