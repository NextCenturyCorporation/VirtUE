package com.ncc.savior.desktop.sidebar;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.io.IOException;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

public class TestClass {
	private JPanel container;
	// private static boolean applicationsOpen = true;

	public TestClass() throws IOException {
		this.container = new JPanel();

		JScrollPane sp = new JScrollPane();

		AppsList al = new AppsList();
		container.setLayout(new BorderLayout(0, 0));
		// sp.setViewportView(al.getView());

		container.add(al.getView());
		// container.add(sp);
	}

	public JPanel getContainer() {
		return container;
	}

	public static void main(String[] args) throws IOException {
		TestClass tc = new TestClass();

		JFrame frame = new JFrame("FrameDemo");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
		frame.getContentPane().add(tc.getContainer());
		frame.setSize(500, 875);
		frame.setVisible(true);
	}
}
