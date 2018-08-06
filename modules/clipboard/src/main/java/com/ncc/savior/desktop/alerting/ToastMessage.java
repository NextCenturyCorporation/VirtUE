package com.ncc.savior.desktop.alerting;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.MouseListener;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.LineBorder;

/**
 * The display window for a single toast message.
 * 
 *
 */
public class ToastMessage extends JFrame {

	private static final long serialVersionUID = 1L;
	private JPanel panel;
	private JLabel toastLabel;

	public ToastMessage(String toastTitle, String toastString, MouseListener listener) {
		setUndecorated(true);
		getContentPane().setLayout(new BorderLayout(0, 0));

		panel = new JPanel();
		// panel.setBackground(Color.GRAY);
		// panel.setBorder(new LineBorder(Color.LIGHT_GRAY, 2));
		getContentPane().add(panel, BorderLayout.CENTER);

		toastLabel = new JLabel("");
		int width = (int) (Toolkit.getDefaultToolkit().getScreenSize().getWidth() / 3);
		toastLabel.setText("<HTML><body style='width: " + width + "px'>" + toastString);
		toastLabel.setFont(new Font("Dialog", Font.BOLD, 12));
		// toastLabel.setForeground(Color.WHITE);
		unhighlight();

		setBounds(100, 100, toastLabel.getPreferredSize().width + 20, toastLabel.getPreferredSize().height + 20);
		setFocusable(false);
		setFocusableWindowState(false);
		setAutoRequestFocus(false);
		setAlwaysOnTop(true);
		panel.add(toastLabel);

		addMouseListener(listener);
	}

	/**
	 * Sets the location of the alert by specifying where the bottom should be. The
	 * alert will appear based on that location and the rest will be computed. This
	 * is useful because we want to layer later alerts on top of existing alerts so
	 * the input is usually the top of the last alert.
	 * 
	 * @param bottom
	 * @return
	 */
	public Rectangle setLocation(int bottom) {
		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
		// int y = dim.height / 2 - getSize().height / 2;
		// int half = y / 2;
		Dimension size = getSize();
		int width = size.width;
		int height = size.height;
		int y = bottom - height;
		int x = dim.width / 2 - width / 2;
		setLocation(x, y);
		setVisible(true);
		Rectangle r = new Rectangle(x, y, width, height);
		return r;
	}

	/**
	 * The alert should be highlighted. In this function we want to alter the look
	 * to be highlighted. This is usually due to the cursor being over the alert.
	 * This can be undone with {@link #unhighlight()}
	 */
	public void highlight() {
		panel.setBackground(Color.WHITE);
		panel.setBorder(new LineBorder(Color.DARK_GRAY, 2));
		toastLabel.setForeground(Color.BLACK);
	}

	/**
	 * Return the alert to its non-highlighted state. This is usually done when a
	 * cursor exits the alert and we want to undo the {@link #highlight()}
	 */
	public void unhighlight() {
		panel.setBackground(Color.GRAY);
		panel.setBorder(new LineBorder(Color.LIGHT_GRAY, 2));
		toastLabel.setForeground(Color.WHITE);
	}
}