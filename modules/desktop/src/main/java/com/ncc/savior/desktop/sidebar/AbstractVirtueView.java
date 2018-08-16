package com.ncc.savior.desktop.sidebar;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JPanel;
import javax.swing.JScrollPane;

public abstract class AbstractVirtueView {
	protected JPanel container;

	protected static int row = 0;
	protected JPanel footer = new JPanel();

	protected JScrollPane sp;

	private static Set<IRemoveVirtueListener> removeVirtueListeners = new HashSet<IRemoveVirtueListener>();

	public AbstractVirtueView(JScrollPane sp) {
		this.sp = sp;
		this.container = new JPanel();

		GridBagLayout gbl = new GridBagLayout();
		// gbl.columnWidths = new int[] { 455, 0 };
		gbl.columnWeights = new double[] { 0.0, Double.MIN_VALUE };
		gbl.rowWeights = new double[] { 0.0, 0.0, Double.MIN_VALUE };
		container.setLayout(gbl);
	}

	public void moveFooter(int row) {
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(0, 0, 0, 0);
		gbc.weightx = 1.0;
		gbc.weighty = 1.0;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.gridx = 0;
		gbc.gridy = row;
		gbc.anchor = GridBagConstraints.PAGE_START;
		container.add(footer, gbc);
	}

	public JPanel getContainer() {
		return container;
	}

	protected void triggerRemoveVirtueListener() {
		for (IRemoveVirtueListener listener : removeVirtueListeners) {
			listener.onRemove();
		}
	}

	public static void addRemoveVirtueListener(IRemoveVirtueListener listener) {
		removeVirtueListeners.add(listener);
	}

	public static void deleteRemoveVirtueListener(IRemoveVirtueListener listener) {
		removeVirtueListeners.remove(listener);
	}

	public static interface IRemoveVirtueListener {

		public void onRemove();

	}

}
