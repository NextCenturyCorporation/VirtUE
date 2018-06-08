package com.ncc.savior.desktop.sidebar;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.SwingConstants;

public class AppsTile {
	private JPanel view;

	public AppsTile() throws IOException {
		this.view = new JPanel();

		for (Integer i = 0; i < 4; i++) {
			addTile(i.toString());
			view.validate();
			view.repaint();
		}

		GridLayout gl_view = new GridLayout(0, 3);
		gl_view.setVgap(30);
		gl_view.setHgap(30);
		view.setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));
		view.setLayout(gl_view);
		view.validate();
		view.repaint();
	}

	public void addTile(String name) throws IOException {
		JPanel tile = new JPanel();
		tile.setBackground(Color.WHITE);
		JLabel lblNewLabel_1 = new JLabel(name);
		lblNewLabel_1.setHorizontalAlignment(SwingConstants.CENTER);
		tile.setLayout(new BorderLayout(0, 0));
		BufferedImage myPicture = ImageIO.read(new File("Test.png"));
		JLabel picLabel = new JLabel(new ImageIcon(myPicture));
		tile.add(picLabel);
		tile.add(lblNewLabel_1, BorderLayout.SOUTH);
		tile.setSize(0, 40);

		tile.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				JPopupMenu pm = new JPopupMenu();
				JMenuItem mi1 = new JMenuItem("Yes");
				JMenuItem mi2 = new JMenuItem("No");
				JLabel prompt = new JLabel("Would you like to start a virtue?");
				pm.add(new JLabel("Would you like to start a virtue?"));
				prompt.setHorizontalAlignment(SwingConstants.CENTER);

				pm.setPopupSize(225, 75);
				pm.add(mi1);
				pm.add(mi2);
				pm.show(view, 125, 200);
			}
		});

		view.add(tile);
	}

	public static void main(String[] args) throws IOException {
		AppsTile at = new AppsTile();

		JFrame frame = new JFrame("FrameDemo");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().add(at.getView());
		frame.setSize(600, 875);
		frame.setVisible(true);
	}

	public JPanel getView() {
		return view;
	}

}
