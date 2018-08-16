package com.ncc.savior.desktop.clipboard.guard;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.LineBorder;

public class CopyPasteDialog implements IDataGuardDialog {

	private JDialog dialog;
	private JLabel prompt;
	private JButton yesButton;
	private JButton noButton;

	private static ImageIcon saviorIcon = new ImageIcon(CopyPasteDialog.class.getResource("/images/saviorLogo.png"));

	private IDialogListener listener;

	public CopyPasteDialog() {
		this.dialog = new JDialog();

		JPanel dialogContainer = new JPanel();
		dialogContainer.setBorder(new LineBorder(Color.DARK_GRAY, 2));
		dialogContainer.setLayout(new BorderLayout());
		dialogContainer.setBackground(Color.WHITE);

		this.prompt = new JLabel("");
		prompt.setBackground(Color.WHITE);
		prompt.setHorizontalAlignment(SwingConstants.CENTER);
		dialogContainer.add(prompt, BorderLayout.CENTER);

		JPanel bottomContainer = new JPanel();
		bottomContainer.setBackground(Color.WHITE);
		bottomContainer.setLayout(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.weightx = 1.0;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.gridx = 0;
		gbc.gridy = 0;
		dialogContainer.add(bottomContainer, BorderLayout.SOUTH);

		this.yesButton = new JButton("Yes");
		yesButton.setSize(new Dimension(50, 30));
		bottomContainer.add(yesButton, gbc);

		gbc.gridx = 1;
		this.noButton = new JButton("No");
		noButton.setSize(new Dimension(50, 30));
		bottomContainer.add(noButton, gbc);

		dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		dialog.add(dialogContainer);

		dialog.pack();
		dialog.setModal(true);
		dialog.setSize(new Dimension(650, 125));
		dialog.setLocationRelativeTo(null);
		dialog.setIconImage(saviorIcon.getImage());

		yesButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				triggerYesListener();
				dialog.dispose();
			}

		});

		noButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				triggerNoListener();
				dialog.dispose();
			}

		});
	}

	@Override
	public void show(String source, String destination) {
		prompt.setText(
				"Would you like to allow copy/pasting for 15 minutes from '" + source + "' to '" + destination + "'?");
		dialog.setVisible(true);
	}

	@Override
	public void setDialogListener(IDialogListener listener) {
		this.listener = listener;
	}

	protected void triggerYesListener() {
		listener.onYes();
	}

	protected void triggerNoListener() {
		listener.onNo();
	}

	public static interface IDialogListener {
		public void onYes();

		public void onNo();
	}
}
