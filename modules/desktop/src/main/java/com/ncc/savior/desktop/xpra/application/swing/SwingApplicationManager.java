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
package com.ncc.savior.desktop.xpra.application.swing;

import java.awt.Color;

import javax.swing.SwingUtilities;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ncc.savior.desktop.xpra.XpraClient;
import com.ncc.savior.desktop.xpra.application.XpraApplication;
import com.ncc.savior.desktop.xpra.application.XpraApplicationManager;
import com.ncc.savior.desktop.xpra.protocol.keyboard.SwingKeyboard;
import com.ncc.savior.desktop.xpra.protocol.packet.dto.NewWindowPacket;
import com.ncc.savior.desktop.xpra.protocol.packet.dto.RaiseWindowPacket;

/**
 * Swing Application manager keeps track of and controls
 * {@link SwingApplication}s. An Application is defined as window that has its
 * own taskbar item. For example, each firefox window is considered an
 * application.
 *
 *
 */
public class SwingApplicationManager extends XpraApplicationManager {
	private static final Logger logger = LoggerFactory.getLogger(SwingApplicationManager.class);
	private Color color;

	public SwingApplicationManager(XpraClient client, SwingKeyboard keyboard) {
		super(client);
		client.setKeyboard(keyboard);
	}

	@Override
	protected synchronized XpraApplication createXpraApplication(NewWindowPacket packet) {
		return createNewSwingXpraApplication(packet, null);
	}

	private XpraApplication createNewSwingXpraApplication(NewWindowPacket packet, SwingApplication parent) {
		SwingApplication app = new SwingApplication(client, packet, parent, color);
		return app;
	}

	@Override
	protected void onRaiseWindow(RaiseWindowPacket packet) {
		int id = packet.getWindowId();
		SwingApplication app = (SwingApplication) windowIdsToApplications.get(id);
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				logger.debug("focusing window" + this.toString());
				app.getWindowFrame().requestFocus();
			}
		});
	}

	@Override
	protected XpraApplication createXpraApplication(NewWindowPacket packet, XpraApplication parent) {
		SwingApplication p = null;
		if (parent instanceof SwingApplication) {
			p = (SwingApplication) parent;
		}
		return createNewSwingXpraApplication(packet, p);
	}

	public void setColor(Color color) {
		this.color = color;
	}
}
