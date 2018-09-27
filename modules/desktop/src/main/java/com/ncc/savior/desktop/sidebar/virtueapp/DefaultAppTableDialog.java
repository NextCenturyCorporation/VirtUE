package com.ncc.savior.desktop.sidebar.virtueapp;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Comparator;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.DefaultListSelectionModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import org.apache.commons.lang3.tuple.Pair;

import com.ncc.savior.desktop.virtues.IIconService;
import com.ncc.savior.util.JavaUtil;
import com.ncc.savior.virtueadmin.model.ApplicationDefinition;
import com.ncc.savior.virtueadmin.model.OS;
import com.ncc.savior.virtueadmin.model.VirtueState;
import com.ncc.savior.virtueadmin.model.desktop.DesktopVirtue;

public class DefaultAppTableDialog extends BaseAppChooser {
	private IIconService iconService;
	private JTable table;

	public DefaultAppTableDialog(IIconService iconService) {
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
		JCheckBox saveCheckbox = new JCheckBox("Save a preference");
		JButton openButton = new JButton("Open");

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

		// list.setBackground(Color.red);
		TableModel tableModel = getTableModel(this.appList);
		this.table = new JTable(tableModel);

		JScrollPane listScroll = new JScrollPane(table);
		table.setOpaque(false);
		table.setBorder(BorderFactory.createEmptyBorder());
		openButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				Pair<DesktopVirtue, ApplicationDefinition> pair = getSelectedPair();
				if (saveCheckbox.isSelected() && savePreferenceConsumer != null) {
					savePreferenceConsumer.accept(pair);
				}
				startAppBiConsumer.accept(pair, params);
				// startAppWithParam(pair, params);
				dialog.dispose();
			}
		});
		table.setDefaultRenderer(ApplicationDefinition.class, getAppTableCellRenderer());
		table.setDefaultRenderer(DesktopVirtue.class, getVirtueTableCellRenderer());
		table.setDefaultRenderer(VirtueState.class, getStatusTableCellRenderer());
		table.setDefaultRenderer(OS.class, getOsTableCellRenderer());
		table.setRowSelectionAllowed(true);
		table.setColumnSelectionAllowed(false);
		table.setRowHeight(28);
		// table.setAutoCreateRowSorter(true);
		TableRowSorter<TableModel> sorter = new TableRowSorter<TableModel>(tableModel);
		sorter.setComparator(0, new Comparator<ApplicationDefinition>() {
			@Override
			public int compare(ApplicationDefinition o1, ApplicationDefinition o2) {
				return o1.getName().compareTo(o2.getName());
			}
		});
		sorter.setComparator(1, new Comparator<OS>() {
			@Override
			public int compare(OS o1, OS o2) {
				return o1.compareTo(o2);
			}
		});
		sorter.setComparator(2, new Comparator<DesktopVirtue>() {
			@Override
			public int compare(DesktopVirtue o1, DesktopVirtue o2) {
				return o1.getName().compareTo(o2.getName());
			}
		});
		sorter.setComparator(3, new Comparator<VirtueState>() {
			@Override
			public int compare(VirtueState o1, VirtueState o2) {
				return o1.compareTo(o2);
			}
		});
		table.setRowSorter(sorter);
		// table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		ListSelectionModel newModel = new DefaultListSelectionModel() {
			private static final long serialVersionUID = 1L;

			@Override
			public void clearSelection() {
			}

			@Override
			public void removeSelectionInterval(int index0, int index1) {
			}
		};
		newModel.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		table.setSelectionModel(newModel);

		listScroll.setBorder(BorderFactory.createEmptyBorder());
		listScroll.setMinimumSize(new Dimension(100, 300));
		dialog.add(label, labelGbc);
		dialog.add(listScroll, scrollGbc);
		dialog.add(saveCheckbox, checkGbc);
		dialog.add(openButton, openGbc);

		listScroll.setBorder(BorderFactory.createEmptyBorder());
		listScroll.setMinimumSize(new Dimension(100, 300));
		dialog.setSize(new Dimension(400, 250));
		dialog.setVisible(true);
		dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

	}

	private TableCellRenderer getAppTableCellRenderer() {
		TableCellRenderer tcr = new TableCellRenderer() {

			@Override
			public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
					boolean hasFocus, int row, int column) {
				ApplicationDefinition app = (ApplicationDefinition) value;
				Image icon = iconService.getImageNow(app.getIconKey());
				JLabel label;
				if (icon != null) {
					icon = icon.getScaledInstance(24, 24, Image.SCALE_SMOOTH);
					ImageIcon image = new ImageIcon(icon);
					label = new JLabel(app.getName(), image, SwingConstants.LEADING);
				} else {
					label = new JLabel(app.getName());
				}
				alterBySelection(label, isSelected, row);
				return label;
			}
		};
		return tcr;
	}

	private TableCellRenderer getVirtueTableCellRenderer() {
		TableCellRenderer tcr = new TableCellRenderer() {

			@Override
			public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
					boolean hasFocus, int row, int column) {
				DesktopVirtue virtue = (DesktopVirtue) value;
				JLabel label = new JLabel(virtue.getName());
				alterBySelection(label, isSelected, row);
				return label;
			}
		};
		return tcr;
	}

	private TableCellRenderer getStatusTableCellRenderer() {
		TableCellRenderer tcr = new TableCellRenderer() {

			@Override
			public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
					boolean hasFocus, int row, int column) {
				VirtueState state = (VirtueState) value;
				JLabel label = new JLabel(state.toString());
				Color color = StatusColor.getColor(state, isSelected, hasFocus);
				label.setForeground(color);
				alterBySelection(label, isSelected, row);
				return label;
			}
		};
		return tcr;
	}

	private TableCellRenderer getOsTableCellRenderer() {
		TableCellRenderer tcr = new TableCellRenderer() {

			@Override
			public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
					boolean hasFocus, int row, int column) {
				OS os = (OS) value;
				JLabel label;
				String name = os.name();
				label = new JLabel(name);
				alterBySelection(label, isSelected, row);
				return label;
			}
		};
		return tcr;
	}

	protected void alterBySelection(JLabel label, boolean isSelected, int row) {
		if (isSelected) {
			label.setBackground(label.getBackground().brighter());
			label.setOpaque(true);
		}
	}

	protected Pair<DesktopVirtue, ApplicationDefinition> getSelectedPair() {
		int index = table.getSelectedRow();
		int modelIndex = table.convertRowIndexToModel(index);
		if (modelIndex > -1 && modelIndex < appList.size()) {
			return appList.get(modelIndex);
		}
		return null;
	}

	private TableModel getTableModel(Vector<Pair<DesktopVirtue, ApplicationDefinition>> appList) {
		AbstractTableModel atm = new AbstractTableModel() {
			private static final long serialVersionUID = 1L;
			private String[] columnNames = { "Application", "OS", "Virtue", "Status" };

			@Override
			public String getColumnName(int column) {
				return columnNames[column];
			}

			@Override
			public Object getValueAt(int rowIndex, int columnIndex) {
				Pair<DesktopVirtue, ApplicationDefinition> pair = appList.get(rowIndex);
				switch (columnIndex) {
				case 0:
					return pair.getRight();
				case 1:
					return pair.getRight().getOs();
				case 2:
					return pair.getLeft();
				case 3:
					return pair.getLeft().getVirtueState();
				default:
					return "";
				}
			}

			@Override
			public Class<?> getColumnClass(int columnIndex) {
				switch (columnIndex) {
				case 0:
					return ApplicationDefinition.class;
				case 1:
					return OS.class;
				case 2:
					return DesktopVirtue.class;
				case 3:
					return VirtueState.class;
				default:
					return Object.class;
				}
			}

			@Override
			public int getRowCount() {
				return appList.size();
			}

			@Override
			public int getColumnCount() {
				return 4;
			}
		};
		return atm;
	}
}
