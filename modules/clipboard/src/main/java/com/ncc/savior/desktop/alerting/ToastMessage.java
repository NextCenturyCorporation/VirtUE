package com.ncc.savior.desktop.alerting;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.LineBorder;

/**
 * The display window for a single toast message.
 * 
 *
 */
public class ToastMessage extends JFrame {
	
	private ImageIcon closeIcon = (new ImageIcon(ToastMessage.class.getResource("/images/red-close-button.png")));

	private static final long serialVersionUID = 1L;
	private JPanel container;
	private JPanel header;
	private JLabel toastLabel;
	private JLabel closeLabel;

	public ToastMessage(String toastTitle, String toastString, MouseListener listener) {
		setUndecorated(true);
		getContentPane().setLayout(new BorderLayout(0, 0));

		closeLabel = new JLabel();
		Image closeImage = closeIcon.getImage();
		Image newCloseImage = closeImage.getScaledInstance(20, 20, java.awt.Image.SCALE_SMOOTH);
		ImageIcon closeButtonIcon = new ImageIcon(newCloseImage);
		closeLabel.setIcon(closeButtonIcon);
		closeLabel.setHorizontalAlignment(SwingConstants.RIGHT);
	
		header = new JPanel();
		header.setBackground(Color.GRAY);
		header.setLayout(new BorderLayout(0, 0));
		
		container = new JPanel();
		container.setLayout(new BorderLayout());
		// panel.setBackground(Color.GRAY);
		// panel.setBorder(new LineBorder(Color.LIGHT_GRAY, 2));
		getContentPane().add(container, BorderLayout.CENTER);

		toastLabel = new JLabel("<html><center>" + toastString + "</center></html>");
		toastLabel.setFont(new Font("Dialog", Font.BOLD, 13));
		//toastLabel.setBorder(BorderFactory.createEmptyBorder(2, 0, 0, 0));
		// toastLabel.setForeground(Color.WHITE);
		toastLabel.setHorizontalAlignment(SwingConstants.CENTER);
		toastLabel.setVerticalAlignment(SwingConstants.TOP);
		unhighlight();

		// setBounds(100, 100, toastLabel.getPreferredSize().width + 20, toastLabel.getPreferredSize().height + 20);
		setSize(600, 65);
		setFocusable(false);
		setFocusableWindowState(false);
		setAutoRequestFocus(false);
		setAlwaysOnTop(true);
		container.add(toastLabel, BorderLayout.CENTER);
		container.add(header, BorderLayout.NORTH);
		header.add(closeLabel, BorderLayout.EAST);
		
		closeLabel.addMouseListener(new MouseAdapter() {
			
			@Override
			public void mouseClicked(MouseEvent e) {
				dispose();
			}
			
			@Override
			public void mouseEntered(MouseEvent e) {
				highlight();
			}
			
		});

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
		container.setBackground(Color.WHITE);
		header.setBackground(Color.WHITE);
		container.setBorder(new LineBorder(Color.DARK_GRAY, 2));
		toastLabel.setForeground(Color.BLACK);
	}

	/**
	 * Return the alert to its non-highlighted state. This is usually done when a
	 * cursor exits the alert and we want to undo the {@link #highlight()}
	 */
	public void unhighlight() {
		container.setBackground(Color.GRAY);
		header.setBackground(Color.GRAY);
		container.setBorder(new LineBorder(Color.LIGHT_GRAY, 2));
		toastLabel.setForeground(Color.WHITE);
	}
}