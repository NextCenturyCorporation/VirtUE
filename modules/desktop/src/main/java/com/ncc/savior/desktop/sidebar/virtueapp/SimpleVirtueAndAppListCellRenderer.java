package com.ncc.savior.desktop.sidebar.virtueapp;

import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

import org.apache.commons.lang3.tuple.Pair;

import com.ncc.savior.virtueadmin.model.ApplicationDefinition;
import com.ncc.savior.virtueadmin.model.desktop.DesktopVirtue;

public class SimpleVirtueAndAppListCellRenderer
		implements ListCellRenderer<Pair<DesktopVirtue, ApplicationDefinition>> {

	@Override
	public Component getListCellRendererComponent(JList<? extends Pair<DesktopVirtue, ApplicationDefinition>> list,
			Pair<DesktopVirtue, ApplicationDefinition> value, int index, boolean isSelected, boolean cellHasFocus) {
		return new JLabel(value.getRight().getName() + " - " + value.getLeft().getName());
	}

}
