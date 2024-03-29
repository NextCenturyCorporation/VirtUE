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

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

import com.ncc.savior.desktop.sidebar.AbstractVirtueView.IRemoveVirtueListener;
import com.ncc.savior.desktop.virtues.VirtueService;
import com.ncc.savior.virtueadmin.model.ApplicationDefinition;
import com.ncc.savior.virtueadmin.model.desktop.DesktopVirtue;

/**
 *
 * This is a superclass for appsList, appsTile, and favoritesView and it
 * includes the basic common components of each different view
 *
 */

public abstract class AbstractAppsView {

	protected VirtueService virtueService;
	protected JPanel container;
	protected ConcurrentHashMap<String, VirtueApplicationItem> tiles;
	protected JScrollPane sp;

	private static Set<IRemoveVirtueListener> removeVirtueListeners = new HashSet<IRemoveVirtueListener>();

	public AbstractAppsView(VirtueService virtueService, JScrollPane sp) {
		this.virtueService = virtueService;
		this.sp = sp;
		this.container = new JPanel();
		this.tiles = new ConcurrentHashMap<String, VirtueApplicationItem>();
	}

	public void addApplication(ApplicationDefinition ad, VirtueApplicationItem va) throws IOException {
		tiles.put(ad.getId() + va.getVirtue().getTemplateId(), va);
		container.add(va.getContainer());

		container.validate();
		container.repaint();
	}

	public void removeVirtue(DesktopVirtue virtue) {
		for (ApplicationDefinition ad : virtue.getApps().values()) {
			tiles.remove(ad.getId() + virtue.getTemplateId());
		}

		triggerRemoveVirtueListener();
	}

	public void search(String keyword, Comparator<VirtueApplicationItem> comp,
			Predicate<VirtueApplicationItem> predicate) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				container.removeAll();
				Collection<VirtueApplicationItem> vas = tiles.values();
				List<VirtueApplicationItem> matchedVas;

				if (predicate != null) {
					matchedVas = vas.stream()
							.filter(predicate)
							.collect(Collectors.toList());
				} else {
					matchedVas = vas.stream().collect(Collectors.toList());
				}

				if (comp != null) {
					Collections.sort(matchedVas, comp);
				} else {
					Collections.sort(matchedVas);
				}

				for (VirtueApplicationItem va : matchedVas) {
					addTile(va);
				}

				container.validate();
				container.repaint();
				sp.setViewportView(sp.getViewport().getView());
			}
		});
	}

	public JPanel getContainer() {
		return container;
	}

	public void updateApp(ApplicationDefinition ad, DesktopVirtue virtue) {
		VirtueApplicationItem va = tiles.get(ad.getId() + virtue.getTemplateId());
		if (va != null) {
			va.update(virtue);
		}
	}

	protected void triggerRemoveVirtueListener() {
		for (IRemoveVirtueListener listener : removeVirtueListeners) {
			listener.onRemove();
		}
	}

	public static void addRemoveVirtueListener(IRemoveVirtueListener listener) {
		removeVirtueListeners.add(listener);
	}

	public static void deleteRemoveVirtueListener(IRemoveVirtueListener listener) {
		removeVirtueListeners.remove(listener);
	}

	public abstract void addTile(VirtueApplicationItem va);

}
