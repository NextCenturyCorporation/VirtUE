package com.ncc.savior.desktop.sidebar;

import java.awt.BorderLayout;
import java.io.IOException;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;


public class TestClass {
	private JPanel container;
	// private static boolean applicationsOpen = true;

	public TestClass() throws IOException {
		this.container = new JPanel();

		JScrollPane sp = new JScrollPane();
		sp.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

		AppsList al = new AppsList();
		container.setLayout(new ModifiedFlowLayout());
		sp.setViewportView(al.getView());

		// container.add(al.getView());
		container.add(sp);
	}

	public JPanel getContainer() {
		return container;
	}

	public static void main(String[] args) throws IOException {
		TestClass tc = new TestClass();

		JFrame frame = new JFrame("FrameDemo");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(new BorderLayout(0, 0));

		frame.getContentPane().add(tc.getContainer());
		frame.setSize(500, 875);
		frame.setVisible(true);
	}
}
