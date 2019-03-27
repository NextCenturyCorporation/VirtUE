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
package com.ncc.savior.desktop.clipboard.messages;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Used to pass information from one virtue to another to run a default
 * application. The original driver for this is clicking on a url to open a
 * browser in another virtue.
 */
public class DefaultApplicationMessage extends BaseClipboardMessage {
	private static final long serialVersionUID = 1L;
	private List<String> arguments;
	private DefaultApplicationType defaultApplicationType;

	// For Jackson (de)serialization
	protected DefaultApplicationMessage() {
		this(null, null, (List<String>) null);
	}

	public DefaultApplicationMessage(String sourceId, DefaultApplicationType defaultApplicationType, String argument) {
		this(sourceId, defaultApplicationType, Collections.singletonList(argument));
	}

	public DefaultApplicationMessage(String sourceId, DefaultApplicationType defaultApplicationType,
			List<String> arguments) {
		super(sourceId);
		this.defaultApplicationType = defaultApplicationType;
		this.arguments = arguments;
	}

	public DefaultApplicationMessage(String sourceId, DefaultApplicationType defaultApplicationType,
			String[] arguments) {
		this(sourceId, defaultApplicationType, Arrays.asList(arguments));
	}

	public List<String> getArguments() {
		return arguments;
	}

	public DefaultApplicationType getDefaultApplicationType() {
		return defaultApplicationType;
	}

	@Override
	public String toString() {
		return "DefaultApplicationMessage [arguments=" + arguments + ", defaultApplicationType="
				+ defaultApplicationType + "]";
	}

	public enum DefaultApplicationType {
		BROWSER
	}
}
