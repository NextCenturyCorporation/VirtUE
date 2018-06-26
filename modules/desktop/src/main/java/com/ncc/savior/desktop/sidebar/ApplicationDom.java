package com.ncc.savior.desktop.sidebar;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;

import com.ncc.savior.virtueadmin.model.ApplicationDefinition;

/**
 * This is the domain object for applications
 */

public class ApplicationDom {

	private ChangeListener changeListener;
	private ArrayList<PropertyChangeListener> listenerList;

	private ApplicationDefinition ad;
	private boolean isFavorited;

	public ApplicationDom(ApplicationDefinition ad, boolean isFavorited) {
		this.ad = ad;
		this.isFavorited = isFavorited;
		this.changeListener = new ChangeListener();
		listenerList = new ArrayList<PropertyChangeListener>();
	}

	public ChangeListener getChangeListener() {
		return changeListener;
	}

	public ApplicationDefinition getApplication() {
		return ad;
	}

	public void setIsFavorited(boolean favorited) {
		this.isFavorited = favorited;
	}

	public boolean getIsFavorited() {
		return isFavorited;
	}

	public void addListener(PropertyChangeListener listener) {
		listenerList.add(listener);
	}

	private void sendChangeEvent(PropertyChangeEvent propertyChangeEvent) {
		for (PropertyChangeListener listener : listenerList) {
			listener.propertyChange(propertyChangeEvent);
		}
	}

	private class ChangeListener implements PropertyChangeListener {

		@Override
		public void propertyChange(PropertyChangeEvent evt) {
			if (isFavorited) {
				isFavorited = false;
				sendChangeEvent(new PropertyChangeEvent("", "isFavorited", true, false));
			} else {
				isFavorited = true;
				sendChangeEvent(new PropertyChangeEvent("", "isFavorited", false, true));
			}
		}
	}
}
