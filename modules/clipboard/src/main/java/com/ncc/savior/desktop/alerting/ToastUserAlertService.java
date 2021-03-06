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
package com.ncc.savior.desktop.alerting;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Service that manager {@link BaseAlertMessage}s via an Android like toast
 * mechanism. This manager will cause a {@link ToastMessage} to appear on the
 * screen when an alert comes in. The toast message will persist on screen for a
 * period of time given by the toastDelay constructor variable and then
 * disappear. If the cursor is over any of the {@link ToastMessage}s, then none
 * of the alerts will disappear until the cursor exits.
 * 
 * Care should be take to prevent any possibility of the alerts from covering
 * the entire screen to prevent them from remaining around forever. The current
 * {@link ToastMessage} has a fixed width that only covers 1/3 of the screen.
 * 
 *
 */
public class ToastUserAlertService implements IUserAlertService {
	@SuppressWarnings("unused")
	private static final Logger logger = LoggerFactory.getLogger(ToastUserAlertService.class);
	private List<ToastMessage> messageQueue;
	private ScheduledExecutorService executor;
	private int startY;
	private int Ybuffer;
	protected boolean stopUpdate;
	private MouseAdapter mouseListener;
	protected boolean hover;
	private long toastDelay;

	public ToastUserAlertService(long toastDelay) {
		this.hover = false;
		this.toastDelay = toastDelay;
		this.messageQueue = Collections.synchronizedList(new LinkedList<ToastMessage>());
		this.executor = Executors.newSingleThreadScheduledExecutor();
		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
		this.startY = dim.height - 200;
		// pixels between alert messages. If not 0, users could try to hover over toasts
		// to keep them around, but change toast could cause the cursor to momentarily
		// be between alerts and then expired messages will disappear. This still may be
		// possible with 0, but much less likely.
		this.Ybuffer = 0;
		mouseListener = new MouseAdapter() {
			@Override
			public void mouseEntered(MouseEvent e) {
				hover = true;
				Component comp = e.getComponent();
				if (comp instanceof ToastMessage) {
					ToastMessage toast = (ToastMessage) comp;
					toast.highlight();
				}
			}

			@Override
			public void mouseExited(MouseEvent e) {
				hover = false;
				Component comp = e.getComponent();
				if (comp instanceof ToastMessage) {
					ToastMessage toast = (ToastMessage) comp;
					toast.unhighlight();
				}
			}
		};
	}

	@Override
	public void displayAlert(BaseAlertMessage alertMessage) {
		ToastMessage m = new ToastMessage(alertMessage.getTitle(), alertMessage.getPlainTextMessage(), mouseListener);
		addToMessages(m, toastDelay);
	}

	private void addToMessages(ToastMessage m, long timeoutMillis) {
		synchronized (messageQueue) {
			messageQueue.add(m);
		}
		executor.schedule(() -> {
			removeWhenAble(m);
		}, timeoutMillis, TimeUnit.MILLISECONDS);
		updateMessagePositions();
	}

	private void removeWhenAble(ToastMessage m) {
		if (hover) {
			executor.schedule(() -> {
				removeWhenAble(m);
			}, 50, TimeUnit.MILLISECONDS);
		} else {
			synchronized (messageQueue) {
				messageQueue.remove(m);
			}
			m.dispose();
			updateMessagePositions();
		}

	}

	private void updateMessagePositions() {
		int lastY = startY;
		synchronized (messageQueue) {
			for (ToastMessage toast : messageQueue) {
				Rectangle r = toast.setLocation(lastY - Ybuffer);
				lastY = (int) r.getY();
			}
		}
	}
}
