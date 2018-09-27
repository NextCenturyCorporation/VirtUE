package com.ncc.savior.desktop.sidebar.virtueapp;

import java.util.Vector;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import org.apache.commons.lang3.tuple.Pair;

import com.ncc.savior.desktop.clipboard.messages.DefaultApplicationMessage.DefaultApplicationType;
import com.ncc.savior.virtueadmin.model.ApplicationDefinition;
import com.ncc.savior.virtueadmin.model.desktop.DesktopVirtue;

public abstract class BaseAppChooser implements IAppChooser {
	protected Vector<Pair<DesktopVirtue, ApplicationDefinition>> appList;
	protected String params;
	protected DefaultApplicationType defaultApplicationType;
	protected Consumer<Pair<DesktopVirtue, ApplicationDefinition>> savePreferenceConsumer;
	protected BiConsumer<Pair<DesktopVirtue, ApplicationDefinition>, String> startAppBiConsumer;

	@Override
	public void setVirtueAppChoices(Vector<Pair<DesktopVirtue, ApplicationDefinition>> appList) {
		this.appList = appList;
	}

	@Override
	public void setParameters(String params) {
		this.params = params;
	}

	@Override
	public void setAppType(DefaultApplicationType defaultApplicationType) {
		this.defaultApplicationType = defaultApplicationType;
	}

	@Override
	public void setSavePreferenceAction(Consumer<Pair<DesktopVirtue, ApplicationDefinition>> savePreferenceConsumer) {
		this.savePreferenceConsumer = savePreferenceConsumer;
	}

	public BiConsumer<Pair<DesktopVirtue, ApplicationDefinition>, String> getStartAppBiConsumer() {
		return startAppBiConsumer;
	}

	@Override
	public void setStartAppBiConsumer(
			BiConsumer<Pair<DesktopVirtue, ApplicationDefinition>, String> startAppBiConsumer) {
		this.startAppBiConsumer = startAppBiConsumer;
	}
}
