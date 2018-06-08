package com.ncc.savior.desktop.sidebar;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.io.IOException;

import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.LineBorder;

public class AppsList {
	private JPanel view;

	public AppsList() throws IOException {
		this.view = new JPanel();

		for (Integer i = 0; i < 1; i++) {
			addTile(i.toString());
			view.validate();
			view.repaint();
		}

		JPanel tile = new JPanel();
		tile.setBorder(new LineBorder(new Color(128, 128, 128), 1));
		tile.setBackground(Color.WHITE);
		JLabel lblNewLabel_1 = new JLabel("Firefox");
		lblNewLabel_1.setIcon(new ImageIcon(AppsList.class.getResource("/images/play.png")));
		lblNewLabel_1.setHorizontalAlignment(SwingConstants.LEFT);
		tile.setLayout(new BoxLayout(tile, BoxLayout.X_AXIS));
		tile.add(lblNewLabel_1);
		view.add(tile);

		GridLayout gl_view = new GridLayout(0, 1);
		view.setLayout(gl_view);
		view.validate();
		view.repaint();
	}

	public void addTile(String name) throws IOException {
		JPanel tile = new JPanel();
		tile.setBorder(new LineBorder(new Color(128, 128, 128), 1));
		tile.setBackground(Color.WHITE);
		JLabel lblNewLabel_1 = new JLabel("Firefox");
		lblNewLabel_1.setIcon(new ImageIcon(AppsList.class.getResource("/images/play.png")));
		lblNewLabel_1.setHorizontalAlignment(SwingConstants.LEFT);
		tile.setLayout(new BoxLayout(tile, BoxLayout.X_AXIS));
		tile.add(lblNewLabel_1);
		tile.setSize(new Dimension(0, 60));
		tile.setMinimumSize(new Dimension(0, 60));
		tile.setMaximumSize(new Dimension(0, 60));
		tile.setPreferredSize(new Dimension(0, 60));
		view.add(tile);
	}

	public static void main(String[] args) throws IOException {
		AppsList at = new AppsList();

		JFrame frame = new JFrame("FrameDemo");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().add(at.getView(), BorderLayout.NORTH);
		frame.setSize(600, 875);
		frame.setVisible(true);
	}

	public JPanel getView() {
		return view;
	}

}
