package com.ncc.savior.desktop.clipboard.hub;

import java.util.List;

import com.ncc.savior.desktop.clipboard.messages.DefaultApplicationMessage.DefaultApplicationType;

/**
 * Listeners to handle when a default application is called. Typically one of
 * the listeners will launch the application or an ApplicationChooser.
 * 
 *
 */
public interface IDefaultApplicationListener {

	/**
	 * Called when a default application is intended to be called.
	 * 
	 * @param defaultApplicationType
	 * @param arguments
	 * @param string 
	 */
	void activateDefaultApp(DefaultApplicationType defaultApplicationType, List<String> arguments, String sourceId);

}
