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

import java.util.Comparator;

import org.apache.commons.lang3.tuple.Pair;

import com.ncc.savior.virtueadmin.model.ApplicationDefinition;
import com.ncc.savior.virtueadmin.model.desktop.DesktopVirtue;

/**
 * {@link Comparator} for a Pair of {@link DesktopVirtue} and
 * {@link ApplicationDefinition} where the primary sort is status.
 *
 *
 */
public class StatusFirstVirtueAppComparator implements Comparator<Pair<DesktopVirtue, ApplicationDefinition>> {

	@Override
	public int compare(Pair<DesktopVirtue, ApplicationDefinition> o1, Pair<DesktopVirtue, ApplicationDefinition> o2) {
		DesktopVirtue v1 = o1.getLeft();
		DesktopVirtue v2 = o2.getLeft();
		ApplicationDefinition a1 = o1.getRight();
		ApplicationDefinition a2 = o2.getRight();
		int valComp = compareByStatus(v1, v2);
		if (valComp != 0) {
			return valComp;
		}
		valComp = compareByVirtueTemplate(v1, v2);
		if (valComp != 0) {
			return valComp;
		}
		valComp = compareByAppName(a1, a2);
		return valComp;

	}

	private int compareByAppName(ApplicationDefinition a1, ApplicationDefinition a2) {
		String n1 = a1.getName();
		String n2 = a2.getName();
		return String.CASE_INSENSITIVE_ORDER.compare(n1, n2);
	}

	private int compareByVirtueTemplate(DesktopVirtue v1, DesktopVirtue v2) {
		String t1 = v1.getTemplateId();
		String t2 = v2.getTemplateId();
		return String.CASE_INSENSITIVE_ORDER.compare(t1, t2);
	}

	private int compareByStatus(DesktopVirtue v1, DesktopVirtue v2) {
		Integer va1State = v1.getVirtueState().getValue();
		Integer va2State = v2.getVirtueState().getValue();
		int valComp = va1State.compareTo(va2State);
		return valComp;
	}

	// @Override
	// public int compare(Pair<DesktopVirtue, ApplicationDefinition> o1,
	// Pair<DesktopVirtue, ApplicationDefinition> o2) {
	// DesktopVirtue v1 = o1.getLeft();
	// DesktopVirtue v2 = o2.getLeft();
	// }

}
