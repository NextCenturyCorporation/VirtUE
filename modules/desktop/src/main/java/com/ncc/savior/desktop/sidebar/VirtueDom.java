package com.ncc.savior.desktop.sidebar;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;

import com.ncc.savior.virtueadmin.model.desktop.DesktopVirtue;

/**
 * This is the domain object for virtues
 */

public class VirtueDom {

	private ChangeListener changeListener;
	private ArrayList<PropertyChangeListener> listenerList;

	private DesktopVirtue virtue;
	private ArrayList<ApplicationDom> applications;

	public VirtueDom(DesktopVirtue virtue) {
		this.virtue = virtue;
	}

	public DesktopVirtue getVirtue() {
		return virtue;
	}

	public ArrayList<ApplicationDom> getApplication() {
		return applications;
	}

	public ChangeListener getChangeListener() {
		return changeListener;
	}

	public void addListener(PropertyChangeListener listener) {
		listenerList.add(listener);
	}

	@SuppressWarnings("unused")
	private void sendChangeEvent(PropertyChangeEvent propertyChangeEvent) {
		for (PropertyChangeListener listener : listenerList) {
			listener.propertyChange(propertyChangeEvent);
		}
	}

	private class ChangeListener implements PropertyChangeListener {

		@Override
		public void propertyChange(PropertyChangeEvent evt) {
			// send an event
		}
	}
}
