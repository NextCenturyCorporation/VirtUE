package com.ncc.savior.desktop.sidebar.defaultapp;

import java.util.Vector;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import org.apache.commons.lang3.tuple.Pair;

import com.ncc.savior.desktop.clipboard.messages.DefaultApplicationMessage.DefaultApplicationType;
import com.ncc.savior.virtueadmin.model.ApplicationDefinition;
import com.ncc.savior.virtueadmin.model.desktop.DesktopVirtue;

/**
 * Interface for application chooser GUI code for default applications. The
 * chooser is not intended to be reused since it stores type and parameters for
 * applications.
 *
 *
 */
public interface IAppChooser {
	/**
	 * sets method ( {@link Consumer} ) to save a preference.
	 *
	 * @param savePreferenceConsumer
	 */
	void setSavePreferenceAction(Consumer<Pair<DesktopVirtue, ApplicationDefinition>> savePreferenceConsumer);

	/**
	 * Starts the chooser.
	 */
	void start();

	/**
	 * Sets the choices for the applications that are valid for this chooser.
	 *
	 * @param appList
	 */
	void setVirtueAppChoices(Vector<Pair<DesktopVirtue, ApplicationDefinition>> appList);

	/**
	 * Sets the applications parameters to be passed to the application.
	 *
	 * @param params
	 */
	void setParameters(String params);

	/**
	 * Sets the application type for the chooser.
	 *
	 * @param defaultApplicationType
	 */
	void setAppType(DefaultApplicationType defaultApplicationType);

	/**
	 * Sets the code that should be called ( {@link BiConsumer} ) when an
	 * application should actually be started. The string parameter is the string of
	 * all the parameters to be passed to the application.
	 *
	 * @param startAppBiConsumer
	 */
	void setStartAppBiConsumer(BiConsumer<Pair<DesktopVirtue, ApplicationDefinition>, String> startAppBiConsumer);

}
