package com.ncc.savior.desktop.clipboard.hub;

import java.util.List;

import com.ncc.savior.desktop.clipboard.messages.DefaultApplicationMessage.DefaultApplicationType;

public interface IDefaultApplicationListener {

	void activateDefaultApp(DefaultApplicationType defaultApplicationType, List<String> arguments);

}
