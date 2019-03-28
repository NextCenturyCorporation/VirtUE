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
package com.ncc.savior.desktop.xpra.application;

import java.io.IOException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ncc.savior.desktop.xpra.protocol.IPacketSender;
import com.ncc.savior.desktop.xpra.protocol.keyboard.IKeyboard;
import com.ncc.savior.desktop.xpra.protocol.packet.dto.DamageSequencePacket;
import com.ncc.savior.desktop.xpra.protocol.packet.dto.DrawPacket;
import com.ncc.savior.desktop.xpra.protocol.packet.dto.FocusPacket;
import com.ncc.savior.desktop.xpra.protocol.packet.dto.MouseButtonActionPacket;
import com.ncc.savior.desktop.xpra.protocol.packet.dto.MousePointerPositionPacket;
import com.ncc.savior.desktop.xpra.protocol.packet.dto.NewWindowPacket;
import com.ncc.savior.desktop.xpra.protocol.packet.dto.Packet;

/**
 * Handles windows from the Xpra protocol. A window is defined by the Xpra
 * protocol and is considered any panel or screen that appears overtop another
 * view. For example, tooltips, history, or settings panels are often their own
 * window inside a single application.
 *
 *
 */
public abstract class XpraWindow implements IXpraWindow {
	private static final Logger logger = LoggerFactory.getLogger(XpraWindow.class);

	protected int id;
	protected boolean debugOutput;
	protected IPacketSender packetSender;
	protected IFocusNotifier focusNotifier;
	protected boolean graphicsSet;
	protected IKeyboard keyboard;

	public XpraWindow(NewWindowPacket packet, IPacketSender packetSender, IKeyboard keyboard,
			IFocusNotifier focusNotifier) {
		this.id = packet.getWindowId();
		this.packetSender = packetSender;
		this.keyboard = keyboard;
		this.focusNotifier = focusNotifier;
		this.graphicsSet = false;
	}

	@Override
	public void setDebugOutput(boolean debugOn) {
		this.debugOutput = debugOn;
	}

	protected void sendDamageSequence(DrawPacket packet) {
		DamageSequencePacket sendPacket = new DamageSequencePacket(packet);
		sendPacket(sendPacket, "damage packet");
	}

	protected void onMouseMove(int x, int y, List<String> modifiers) {
		MousePointerPositionPacket sendPacket = new MousePointerPositionPacket(id, x, y, modifiers);
		sendPacket(sendPacket, "pointer position packet");
	}

	protected void onMousePress(int button, int x, int y, List<String> modifiers) {
		MouseButtonActionPacket sendPacket = new MouseButtonActionPacket(id, button, true, x, y, modifiers);
		sendPacket(sendPacket, "mouse button action packet (pressed)");
	}

	protected void onMouseRelease(int button, int x, int y, List<String> modifiers) {
		MouseButtonActionPacket sendPacket = new MouseButtonActionPacket(id, button, false, x, y, modifiers);
		sendPacket(sendPacket, "mouse button action packet (released)");
	}

	protected void onMouseScroll(int button, int distance, int x, int y) {
		int delta = distance / 20;
		// logger.debug("Scrolling delta=" + delta + " _ " + id + " " + x + " " + y);
		if (delta > 0) {
			button = 4;
		} else {
			button = 5;
			delta = -delta;
		}
		// if (x < 0 || y < 0) {
		// throw new IllegalArgumentException("Minus coordinates are not allowed: " + x
		// + ", " + y);
		// }
		// new WheelMotionPacket(id, button, distance, x, y);
		for (int i = 0; i < delta; i++) {
			sendPacket(new MouseButtonActionPacket(id, button, true, x, y), "mouse wheel packet");
			sendPacket(new MouseButtonActionPacket(id, button, false, x, y), "mouse wheel packet");
		}
	}

	protected void onWindowFocus() {
		this.focusNotifier.notifyFocusedWindow(id);
		FocusPacket sendPacket = new FocusPacket(id);
		sendPacket(sendPacket, "focus packet");
	}

	protected void sendPacket(Packet sendPacket, String packetDescription) {
		try {
			packetSender.sendPacket(sendPacket);
			if (logger.isDebugEnabled()) {
				// logger.debug("Sending Packet=" + sendPacket.toString());
			}
		} catch (IOException e) {
			logger.error("Error attempting to send damage packet=" + sendPacket, e);
		}
	}

	public abstract void doClose();

	@Override
	public void close() throws IOException {
		doClose();
	}

}
