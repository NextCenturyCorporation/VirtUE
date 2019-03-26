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

import java.awt.Container;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.Window;

import javax.swing.JDialog;
import javax.swing.JFrame;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link JDialog} based implementation of {@link WindowFrame}. This is used for
 * Dialogs and Modals.
 *
 *
 */
public class DialogWindowFrame extends WindowFrame {
	private static Logger logger = LoggerFactory.getLogger(DialogWindowFrame.class);

	private JDialog frame;

	public DialogWindowFrame(JDialog jDialog) {
		super(jDialog);
		this.frame = jDialog;
	}

	@Override
	public Container getContentPane() {
		return frame.getContentPane();
	}

	@Override
	public Rectangle getMaximizedBounds() {
		return null;
	}

	@Override
	public void setUndecorated(boolean b) {
		frame.setUndecorated(b);
	}

	@Override
	public void setState(int normal) {

	}

	@Override
	public void setMaximizedBounds(Rectangle fullScreenBounds) {

	}

	@Override
	public int getExtendedState() {
		return JFrame.NORMAL;
	}

	@Override
	public void setExtendedState(int i) {

	}

	@Override
	public void setTitle(String title) {
		frame.setTitle(title);
	}

	@Override
	public void setIconImage(Image icon) {
		frame.setIconImage(icon);
	}

	@Override
	protected Window doSwitchToFullscreen(Window window, Window previous) {
		logger.warn("Cannot DialogWindow to fullscreen");
		return window;
	}

	@Override
	protected Window doSwitchFromFullscreen(Window window, Window previous) {
		logger.warn("Cannot DialogWindow to fullscreen");
		return window;
	}
}
