package com.ncc.savior.desktop.alerting;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Toolkit;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.LineBorder;

import javafx.scene.shape.Rectangle;

public class ToastMessage extends JFrame {

	private static final long serialVersionUID = 1L;

	public ToastMessage(String toastTitle, String toastString) {
		setUndecorated(true);
		getContentPane().setLayout(new BorderLayout(0, 0));

		JPanel panel = new JPanel();
		panel.setBackground(Color.GRAY);
		panel.setBorder(new LineBorder(Color.LIGHT_GRAY, 2));
		getContentPane().add(panel, BorderLayout.CENTER);

		JLabel toastLabel = new JLabel("");
		toastLabel.setText(toastString);
		toastLabel.setFont(new Font("Dialog", Font.BOLD, 12));
		toastLabel.setForeground(Color.WHITE);

		setBounds(100, 100, toastLabel.getPreferredSize().width + 20, 31);
		setFocusable(false);
		setFocusableWindowState(false);
		setAutoRequestFocus(false);
		setAlwaysOnTop(true);
		panel.add(toastLabel);
	}

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
}