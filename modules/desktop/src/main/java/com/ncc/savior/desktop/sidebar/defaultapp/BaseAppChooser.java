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
package com.ncc.savior.desktop.sidebar.defaultapp;

import java.util.Vector;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import org.apache.commons.lang3.tuple.Pair;

import com.ncc.savior.desktop.clipboard.messages.DefaultApplicationMessage.DefaultApplicationType;
import com.ncc.savior.virtueadmin.model.ApplicationDefinition;
import com.ncc.savior.virtueadmin.model.desktop.DesktopVirtue;

/**
 * Base class for an application chooser which determines which application
 * should be opened for a given type and arguments.
 *
 *
 */
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
