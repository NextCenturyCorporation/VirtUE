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

import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ncc.savior.desktop.virtues.VirtueService;
import com.ncc.savior.virtueadmin.model.ApplicationDefinition;
import com.ncc.savior.virtueadmin.model.VirtueState;
import com.ncc.savior.virtueadmin.model.desktop.DesktopVirtue;

public abstract class AbstractVirtueContainer {

	private static Logger logger = LoggerFactory.getLogger(AbstractVirtueContainer.class);

	protected static ImageIcon optionsIcon = new ImageIcon(
			AbstractVirtueContainer.class.getResource("/images/options.png"));
	protected static Image optionsImage = optionsIcon.getImage(); // transform it
	protected static Image scaledOptionsImage = optionsImage.getScaledInstance(24, 24, java.awt.Image.SCALE_SMOOTH);

	protected DesktopVirtue virtue;
	protected VirtueService virtueService;
	protected VirtueState status;
	protected JLabel statusLabel;
	protected JPanel header;

	protected JLabel optionsLabel;

	protected boolean dropDown = false;
	protected JPanel container;

	protected static int numRows = 0;
	protected int row;

	protected HashMap<String, VirtueApplicationItem> tiles;
	protected String headerTitle;

	protected GhostText ghostText;

	protected JScrollPane sp;
	protected JTextField textField;

	private static Set<IUpdateListener> updateListeners = new HashSet<IUpdateListener>();

	public AbstractVirtueContainer(DesktopVirtue virtue, VirtueService virtueService, JScrollPane sp,
			JTextField textField, GhostText ghostText) {
		this.virtue = virtue;
		this.virtueService = virtueService;
		this.sp = sp;
		this.tiles = new HashMap<String, VirtueApplicationItem>();
		this.headerTitle = virtue.getName();
		this.status = virtue.getVirtueState();
		this.textField = textField;
		this.ghostText = ghostText;

		resetRows();
	}

	public int getRow() {
		return row;
	}

	public void setRow(int row) {
		this.row = row;
	}

	public static void resetRows() {
		numRows = 0;
	}

	public JPanel getContainer() {
		return container;
	}

	public DesktopVirtue getVirtue() {
		return virtue;
	}

	public String getName() {
		return headerTitle;
	}

	public void addOptionsListener() {
		optionsLabel.setToolTipText("Click to start or stop a virtue");

		optionsLabel.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent event) {
				JPopupMenu menu = new JPopupMenu();
				JMenuItem stopMenuItem = new JMenuItem("Stop");
				JMenuItem startMenuItem = new JMenuItem("Start");
				JMenuItem terminateMenuItem = new JMenuItem("Terminate");
				terminateMenuItem.setToolTipText("Terminates the virtue state. (Cannot be undone)");
				JMenuItem reconnectMenuItem = new JMenuItem("Reconnect");
				reconnectMenuItem.setToolTipText(
						"Attempts to re-establishe a connection for the virtue if necessary.  Applications running on the virtue when the connect was broken should resume.");

				stopMenuItem.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent evt) {
						try {
							virtueService.stopVirtue(virtue);
							updateVirtue(virtue);
						} catch (IOException e) {
							String msg = "Error attempting to stop virtue=" + virtue;
							logger.error(msg, e);
						}
					}
				});

				startMenuItem.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent evt) {
						try {
							virtueService.startVirtue(virtue);
							updateVirtue(virtue);
						} catch (IOException e) {
							String msg = "Error attempting to start virtue=" + virtue;
							logger.error(msg, e);
						}
					}
				});

				terminateMenuItem.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent evt) {
						try {
							virtueService.terminateVirtue(virtue);
							updateVirtue(virtue);
						} catch (IOException e) {
							String msg = "Error attempting to terminate virtue=" + virtue;
							logger.error(msg, e);
						}
					}
				});

				reconnectMenuItem.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent evt) {
						virtueService.ensureConnectionForVirtue(virtue);
					}
				});

				menu.addPopupMenuListener(new PopupMenuListener() {
					@Override
					public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
						logger.debug("state of '" + virtue.getName() + "' = " + virtue.getVirtueState());
						stopMenuItem.setEnabled(virtueService.isStopable(virtue));
						startMenuItem.setEnabled(virtueService.isStartable(virtue));
						terminateMenuItem.setEnabled(virtueService.isTerminatable(virtue));
					}

					@Override
					public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
					}

					@Override
					public void popupMenuCanceled(PopupMenuEvent e) {
					}
				});
				menu.setPopupSize(78, 97);
				menu.add(stopMenuItem);
				menu.addSeparator();
				menu.add(startMenuItem);
				menu.addSeparator();
				menu.add(terminateMenuItem);
				menu.addSeparator();
				menu.add(reconnectMenuItem);
				menu.show(optionsLabel, -44, 26);
			}
		});
	}

	public boolean containsKeyword(String keyword) {
		for (ApplicationDefinition ad : virtue.getApps().values()) {
			if (ad.getName().toLowerCase().contains(keyword.toLowerCase())) {
				return true;
			}
		}
		return false;
	}

	public abstract void updateVirtue(DesktopVirtue virtue);

	protected void triggerUpdateListener() {
		for (IUpdateListener listener : updateListeners) {
			listener.onUpdate();
		}
	}

	public static void addUpdateListener(IUpdateListener listener) {
		updateListeners.add(listener);
	}

	public static void removeUpdateListener(IUpdateListener listener) {
		updateListeners.remove(listener);
	}

	public static interface IUpdateListener {

		public void onUpdate();

	}

}
