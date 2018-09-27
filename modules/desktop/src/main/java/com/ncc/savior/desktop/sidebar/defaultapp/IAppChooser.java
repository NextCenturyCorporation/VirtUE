package com.ncc.savior.desktop.sidebar.defaultapp;

import java.util.Vector;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import org.apache.commons.lang3.tuple.Pair;

import com.ncc.savior.desktop.clipboard.messages.DefaultApplicationMessage.DefaultApplicationType;
import com.ncc.savior.virtueadmin.model.ApplicationDefinition;
import com.ncc.savior.virtueadmin.model.desktop.DesktopVirtue;

public interface IAppChooser {

	void setSavePreferenceAction(Consumer<Pair<DesktopVirtue, ApplicationDefinition>> savePreferenceConsumer);

	void start();

	void setVirtueAppChoices(Vector<Pair<DesktopVirtue, ApplicationDefinition>> appList);

	void setParameters(String params);

	void setAppType(DefaultApplicationType defaultApplicationType);

	void setStartAppBiConsumer(BiConsumer<Pair<DesktopVirtue, ApplicationDefinition>, String> startAppBiConsumer);

}
