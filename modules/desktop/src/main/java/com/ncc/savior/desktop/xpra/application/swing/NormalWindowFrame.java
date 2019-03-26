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
import java.awt.event.ComponentListener;
import java.awt.event.KeyListener;
import java.awt.event.WindowListener;

import javax.swing.JFrame;

/**
 * Implementation of {@link WindowFrame} that uses a {@link JFrame}. This is for
 * most base windows.
 *
 *
 */
public class NormalWindowFrame extends WindowFrame {

	private JFrame frame;

	public NormalWindowFrame(JFrame frame) {
		super(frame);
		this.frame = frame;
	}

	@Override
	public Container getContentPane() {
		return frame.getContentPane();
	}

	@Override
	public Rectangle getMaximizedBounds() {
		return frame.getMaximizedBounds();
	}

	@Override
	public void setUndecorated(boolean b) {
		frame.setUndecorated(b);
	}

	@Override
	public void setState(int normal) {
		frame.setState(normal);
	}

	@Override
	public void setMaximizedBounds(Rectangle fullScreenBounds) {
		frame.setMaximizedBounds(fullScreenBounds);
	}

	@Override
	public int getExtendedState() {
		return frame.getExtendedState();
	}

	@Override
	public void setExtendedState(int i) {
		frame.setExtendedState(i);
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
	protected Window doSwitchToFullscreen(Window window, Window previousWindow) {
		JFrame jf;
		if (window == null) {
			jf = new JFrame();
			jf.setExtendedState(JFrame.MAXIMIZED_BOTH);
			jf.setUndecorated(true);
			copyListeners(previousWindow, jf);
			// add listeners
		} else {
			jf = (JFrame) window;
		}
		jf.setVisible(true);
		jf.setIconImage(frame.getIconImage());
		jf.setTitle(frame.getTitle());
		frame = jf;
		return jf;
	}

	@Override
	protected Window doSwitchFromFullscreen(Window window, Window previousWindow) {
		JFrame jf;
		if (window == null) {
			jf = new JFrame();
			jf.setExtendedState(JFrame.NORMAL);
			jf.setUndecorated(false);
			copyListeners(previousWindow, jf);
			// add listeners
		} else {
			jf = (JFrame) window;
		}
		jf.setVisible(true);
		jf.setIconImage(frame.getIconImage());
		jf.setTitle(frame.getTitle());
		frame = jf;
		return jf;
	}

	private void copyListeners(Window from, Window to) {
		for (KeyListener kl : from.getKeyListeners()) {
			to.addKeyListener(kl);
		}
		for (WindowListener wl : from.getWindowListeners()) {
			to.addWindowListener(wl);
		}
		for (ComponentListener cl : from.getComponentListeners()) {
			to.addComponentListener(cl);
		}
	}
}
