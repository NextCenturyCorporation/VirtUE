package com.ncc.savior.desktop.sidebar.virtueapp;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JScrollPane;

import org.apache.commons.lang3.tuple.Pair;

import com.ncc.savior.desktop.virtues.IIconService;
import com.ncc.savior.util.JavaUtil;
import com.ncc.savior.virtueadmin.model.ApplicationDefinition;
import com.ncc.savior.virtueadmin.model.desktop.DesktopVirtue;

public class DefaultAppListDialog extends BaseAppChooser {
	// private static final Logger logger =
	// LoggerFactory.getLogger(DefaultAppListDialog.class);
	private IIconService iconService;

	public DefaultAppListDialog(IIconService iconService) {
		this.iconService = iconService;
	}

	@Override
	public void start() {
		JDialog dialog = new JDialog();
		dialog.setTitle("Where do you want to open " + defaultApplicationType.toString());
		dialog.setAlwaysOnTop(true);

		String topLabelText = "<html>Choose a virtue and application combo to run " + defaultApplicationType.toString();
		if (JavaUtil.isNotEmpty(params)) {
			topLabelText += " with params: <br/> &nbsp;&nbsp;" + params;
		}
		topLabelText += "</html>";
		JLabel label = new JLabel(topLabelText);
		JList<Pair<DesktopVirtue, ApplicationDefinition>> list = new JList<Pair<DesktopVirtue, ApplicationDefinition>>(
				appList);

		list.setCellRenderer(new VirtueAndAppListCellRenderer(iconService));
		// combo.setRenderer(new SimpleVirtueAndAppListCellRenderer());

		JCheckBox saveCheckbox = new JCheckBox("Save a preference");
		JButton openButton = new JButton("Open");
		openButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				Pair<DesktopVirtue, ApplicationDefinition> pair = list.getSelectedValue();
				if (saveCheckbox.isSelected() && savePreferenceConsumer != null) {
					savePreferenceConsumer.accept(pair);
				}
				startAppBiConsumer.accept(pair, params);
				// startAppWithParam(pair, params);
				dialog.dispose();
			}
		});
		int itemSize = appList.size();
		int showItemSize = itemSize < 10 ? itemSize : 10;
		list.setVisibleRowCount(showItemSize);
		GridBagLayout gbl = new GridBagLayout();
		dialog.setLayout(gbl);
		GridBagConstraints labelGbc = new GridBagConstraints();
		labelGbc.fill = GridBagConstraints.BOTH;
		labelGbc.gridx = 0;
		labelGbc.gridy = 0;
		labelGbc.gridheight = 1;
		labelGbc.gridwidth = 2;
		labelGbc.ipadx = 2;
		labelGbc.ipady = 2;
		// labelGbc.weightx=2;
		// labelGbc.weighty = 200;
		// scrollGbc.weightx = 1;
		// scrollGbc.weighty = 1;

		GridBagConstraints scrollGbc = new GridBagConstraints();
		scrollGbc.fill = GridBagConstraints.BOTH;
		scrollGbc.gridx = 0;
		scrollGbc.gridy = 1;
		scrollGbc.gridheight = 1;
		scrollGbc.gridwidth = 2;
		scrollGbc.weightx = 1;
		scrollGbc.weighty = 1;
		// scrollGbc.ipadx = 2;
		// scrollGbc.ipady = 2;
		scrollGbc.insets = new Insets(5, 5, 5, 5);
		GridBagConstraints checkGbc = new GridBagConstraints();
		// checkGbc.fill = GridBagConstraints.BOTH;
		checkGbc.gridx = 0;
		checkGbc.gridy = 2;
		GridBagConstraints openGbc = new GridBagConstraints();
		// openGbc.fill = GridBagConstraints.BOTH;
		openGbc.gridx = 1;
		openGbc.gridy = 2;

		JScrollPane listScroll = new JScrollPane(list);
		// list.setBackground(Color.red);
		list.setOpaque(false);
		list.setBorder(BorderFactory.createEmptyBorder());
		listScroll.setBorder(BorderFactory.createEmptyBorder());
		listScroll.setMinimumSize(new Dimension(100, 300));
		dialog.add(label, labelGbc);
		dialog.add(listScroll, scrollGbc);
		dialog.add(saveCheckbox, checkGbc);
		dialog.add(openButton, openGbc);
		// sp.setViewportView(container);
		// dialog.add(sp);
		dialog.setSize(new Dimension(400, 250));
		dialog.setVisible(true);
		dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

	}
}
