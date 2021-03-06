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

import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

import com.ncc.savior.virtueadmin.model.ApplicationDefinition;
import com.ncc.savior.virtueadmin.model.desktop.DesktopVirtue;

/**
 *
 * This class should be correctly implemented at some point to provide a second
 * view for virtues
 *
 */

public class VirtueList extends AbstractVirtueView {

	private ConcurrentHashMap<String, VirtueListContainer> virtues;

	public VirtueList(JScrollPane sp) throws IOException {
		super(sp);
		this.virtues = new ConcurrentHashMap<String, VirtueListContainer>();
	}

	public void removeVirtue(DesktopVirtue virtue) {
		virtues.remove(virtue.getTemplateId());

		triggerRemoveVirtueListener();
	}

	public void updateApp(ApplicationDefinition ad, DesktopVirtue virtue) {
		VirtueListContainer vlc = virtues.get(virtue.getTemplateId());
		if (vlc != null) {
			VirtueApplicationItem va = vlc.tiles.get(ad.getId() + virtue.getTemplateId());
			if (va != null) {
				va.update(virtue);
			}
		}
	}

	public void addVirtueToRow(DesktopVirtue virtue, VirtueListContainer vlc, int row) {
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(0, 0, 0, 0);
		gbc.weightx = 1.0;
		gbc.weighty = 0.0;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.gridx = 0;
		gbc.gridy = row;
		gbc.anchor = GridBagConstraints.PAGE_START;
		container.remove(footer);
		container.add(vlc.getContainer(), gbc);
		moveFooter(row + 1);

		virtues.put(virtue.getTemplateId(), vlc);
	}

	public void search(String keyword, Comparator<VirtueListContainer> vlcComp,
			Comparator<VirtueApplicationItem> vaiComp) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				row = 0;
				container.removeAll();
				Collection<VirtueListContainer> vlcs = virtues.values();
				List<VirtueListContainer> matchedVlcs;

				if (keyword != null) {
					matchedVlcs = vlcs.stream().filter(vlc -> vlc.containsKeyword(keyword))
							.collect(Collectors.toList());
				} else {
					matchedVlcs = vlcs.stream().collect(Collectors.toList());
				}

				if (vlcComp != null) {
					Collections.sort(matchedVlcs, vlcComp);
				} else {
					Collections.sort(matchedVlcs);
				}

				for (VirtueListContainer vlc : matchedVlcs) {
					if (keyword != null) {
						if (vaiComp != null) {
							vlc.search(vaiComp,
									va -> va.getApplicationName().toLowerCase().contains(keyword.toLowerCase()));
						} else {
							vlc.search(null,
									va -> va.getApplicationName().toLowerCase().contains(keyword.toLowerCase()));
						}
					} else {
						vlc.search(null, null);
					}
					vlc.setRow(row);
					addVirtueToRow(vlc.getVirtue(), vlc, row);
					row++;
					container.validate();
					container.repaint();
				}
				sp.setViewportView(sp.getViewport().getView());
			}
		});
	}

	public JScrollPane getScroll() {
		return sp;
	}

	@Override
	public JPanel getContainer() {
		return container;
	}
}
