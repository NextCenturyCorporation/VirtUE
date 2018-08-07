package com.ncc.savior.desktop.sidebar;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.Image;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

public class ToastTest {
	private static ImageIcon closeIcon = (new ImageIcon(Sidebar.class.getResource("/images/red-close-button.png")));

	public static void main(String[] args) {

		JFrame frame = new JFrame();

		frame.setUndecorated(true);
		frame.getContentPane().setLayout(new BorderLayout(0, 0));

		JLabel closeLabel = new JLabel();
		Image closeImage = closeIcon.getImage();
		Image newCloseImage = closeImage.getScaledInstance(20, 20, java.awt.Image.SCALE_SMOOTH);
		ImageIcon closeButtonIcon = new ImageIcon(newCloseImage);
		closeLabel.setIcon(closeButtonIcon);
		closeLabel.setHorizontalAlignment(SwingConstants.RIGHT);

		JPanel header = new JPanel();
		header.setBackground(Color.GRAY);
		JPanel footer = new JPanel();
		footer.setBackground(Color.GRAY);

		JPanel container = new JPanel();
		container.setLayout(new BorderLayout());
		// panel.setBackground(Color.GRAY);
		// panel.setBorder(new LineBorder(Color.LIGHT_GRAY, 2));
		frame.getContentPane().add(container, BorderLayout.CENTER);

		JLabel toastLabel = new JLabel("wow");
		toastLabel.setFont(new Font("Dialog", Font.BOLD, 17));
		toastLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 6, 0));
		// toastLabel.setForeground(Color.WHITE);
		toastLabel.setHorizontalAlignment(SwingConstants.CENTER);
		//toastLabel.setVerticalAlignment(SwingConstants.TOP);

		// setBounds(100, 100, toastLabel.getPreferredSize().width + 20, toastLabel.getPreferredSize().height + 20);
		frame.setSize(600, 50);
		frame.setFocusable(false);
		frame.setFocusableWindowState(false);
		frame.setAutoRequestFocus(false);
		frame.setAlwaysOnTop(true);
		container.add(toastLabel, BorderLayout.CENTER);
		container.add(header, BorderLayout.NORTH);
		header.setLayout(new BorderLayout(0, 0));
		header.add(closeLabel, BorderLayout.EAST);
		container.add(footer, BorderLayout.SOUTH);


		frame.setVisible(true);
	}
}
