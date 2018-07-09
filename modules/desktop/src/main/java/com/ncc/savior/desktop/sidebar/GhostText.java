package com.ncc.savior.desktop.sidebar;

import java.awt.Color;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

public class GhostText implements FocusListener, DocumentListener, PropertyChangeListener {
	private final JTextField textfield;
	private boolean isEmpty;
	private boolean isVisible = true;
	private Color ghostColor;
	private Color foregroundColor;
	private final String ghostText;
	private JLabel searchLabel;
	private ImageIcon search;

	protected GhostText(final JTextField textfield, String ghostText, JLabel searchLabel, ImageIcon search) {
		super();
		this.search = search;
		this.searchLabel = searchLabel;
		this.textfield = textfield;
		this.ghostText = ghostText;
		this.ghostColor = Color.LIGHT_GRAY;
		this.foregroundColor = textfield.getForeground();
		this.textfield.setForeground(ghostColor);
		textfield.addFocusListener(this);
		registerListeners();
		updateState();
	}

	private void registerListeners() {
		textfield.getDocument().addDocumentListener(this);
		textfield.addPropertyChangeListener("foreground", this);
	}

	private void updateState() {
		isEmpty = textfield.getText().length() == 0;
	}

	public boolean getIsVisible() {
		return isVisible;
	}

	@Override
	public void focusGained(FocusEvent e) {
		if (isVisible) {
			isVisible = false;
			textfield.setText("");
			textfield.setForeground(foregroundColor);
		}

	}

	@Override
	public void focusLost(FocusEvent e) {
		if (isEmpty) {
			isVisible = true;
			textfield.setText(ghostText);
			textfield.setForeground(ghostColor);
			searchLabel.setIcon(search);
		}
	}

	public void reset() {
		isVisible = true;
		textfield.setText(ghostText);
		textfield.setForeground(ghostColor);
		searchLabel.setIcon(search);
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		updateState();
	}

	@Override
	public void changedUpdate(DocumentEvent e) {
		updateState();
	}

	@Override
	public void insertUpdate(DocumentEvent e) {
		updateState();
	}

	@Override
	public void removeUpdate(DocumentEvent e) {
		updateState();
	}
}
