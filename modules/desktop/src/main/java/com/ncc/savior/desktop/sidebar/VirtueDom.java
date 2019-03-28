/*
 * Copyright (C) 2019 Next Century Corporation
 * 
 * This file may be redistributed and/or modified under either the GPL
 * 2.0 or 3-Clause BSD license. In addition, the U.S. Government is
 * granted government purpose rights. For details, see the COPYRIGHT.TXT
 * file at the root of this project.
 * 
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
 * 02110-1301, USA.
 * 
 * SPDX-License-Identifier: (GPL-2.0-only OR BSD-3-Clause)
 */
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
