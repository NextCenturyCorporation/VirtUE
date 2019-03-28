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
