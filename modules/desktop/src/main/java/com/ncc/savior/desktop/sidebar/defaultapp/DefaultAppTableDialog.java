package com.ncc.savior.desktop.sidebar.defaultapp;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
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
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import org.apache.commons.lang3.tuple.Pair;

import com.ncc.savior.desktop.sidebar.ColorManager;
import com.ncc.savior.desktop.virtues.IIconService;
import com.ncc.savior.util.JavaUtil;
import com.ncc.savior.virtueadmin.model.ApplicationDefinition;
import com.ncc.savior.virtueadmin.model.OS;
import com.ncc.savior.virtueadmin.model.VirtueState;
import com.ncc.savior.virtueadmin.model.desktop.DesktopVirtue;

/**
 * GUI code for default table-based application chooser for default
 * applications.
 *
 */
public class DefaultAppTableDialog extends BaseAppChooser {
	private static final int ROW_HEIGHT = 32;
	private static final int ICON_SIZE = 20;
	private IIconService iconService;
	private JTable table;
	private int cellPadding = 5;
	private ColorManager colorManager;

	public DefaultAppTableDialog(IIconService iconService, ColorManager colorManager) {
		this.iconService = iconService;
		this.colorManager = colorManager;
	}

	@Override
	public void start() {
		JDialog dialog = new JDialog();
		// dialog.setSize(600, 400);
		dialog.setTitle("Where do you want to open " + defaultApplicationType.toString());
		dialog.setAlwaysOnTop(true);

		String topLabelText = "<html>Choose a virtue and application combo to run " + defaultApplicationType.toString();
		if (JavaUtil.isNotEmpty(params)) {
			topLabelText += " with params: <br/> &nbsp;&nbsp;" + params;
		}
		topLabelText += "</html>";
		JLabel label = new JLabel(topLabelText);
		FlowLayout flow = new FlowLayout(FlowLayout.CENTER);
		flow.setHgap(50);
		JPanel buttonPane = new JPanel(flow);
		// buttonPane.setBorder(BorderFactory.createLineBorder(Color.red));
		JButton saveOpenButton = new JButton("Open always");
		JButton openButton = new JButton("Open just once");
		buttonPane.add(saveOpenButton, FlowLayout.LEFT);
		buttonPane.add(openButton, FlowLayout.LEFT);

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
		checkGbc.gridx = 1;
		checkGbc.gridy = 2;
		checkGbc.weightx = 2;
		checkGbc.insets = new Insets(5, 55, 5, 5);
		GridBagConstraints openGbc = new GridBagConstraints();
		// openGbc.fill = GridBagConstraints.BOTH;
		openGbc.gridx = 0;
		openGbc.gridy = 2;
		openGbc.weightx = 2;
		openGbc.gridwidth = 2;
		openGbc.anchor = GridBagConstraints.EAST;
		openGbc.fill = GridBagConstraints.BOTH;
		openGbc.insets = new Insets(5, 5, 5, 5);

		// list.setBackground(Color.red);
		TableModel tableModel = getTableModel(this.appList);
		this.table = new JTable(tableModel);

		JScrollPane listScroll = new JScrollPane(table);
		table.setOpaque(false);
		table.setBorder(BorderFactory.createEmptyBorder());
		table.setBorder(BorderFactory.createLineBorder(Color.BLACK));
		openButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				Pair<DesktopVirtue, ApplicationDefinition> pair = getSelectedPair();
				startAppBiConsumer.accept(pair, params);
				dialog.dispose();

			}
		});
		saveOpenButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				Pair<DesktopVirtue, ApplicationDefinition> pair = getSelectedPair();
				if (savePreferenceConsumer != null) {
					savePreferenceConsumer.accept(pair);
				}
				startAppBiConsumer.accept(pair, params);
				dialog.dispose();
			}
		});
		table.setDefaultRenderer(ApplicationDefinition.class, getAppTableCellRenderer());
		table.setDefaultRenderer(DesktopVirtue.class, getVirtueTableCellRenderer());
		table.setDefaultRenderer(VirtueState.class, getStatusTableCellRenderer());
		table.setDefaultRenderer(OS.class, getOsTableCellRenderer());
		table.setRowSelectionAllowed(true);
		table.setColumnSelectionAllowed(false);
		table.setRowHeight(ROW_HEIGHT);
		table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

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
		// dialog.add(saveOpenButton, checkGbc);
		// dialog.add(openButton, openGbc);
		dialog.add(buttonPane, openGbc);

		listScroll.setBorder(BorderFactory.createEmptyBorder());
		listScroll.setMinimumSize(new Dimension(100, 300));
		// dialog.setSize(new Dimension(600, 400));
		dialog.pack();
		Dimension size = dialog.getSize();
		size.width = 800;

		table.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				Pair<DesktopVirtue, ApplicationDefinition> pair = getSelectedPair();
				boolean enabled = (pair != null);
				openButton.setEnabled(enabled);
				saveOpenButton.setEnabled(enabled);
			}
		});
		openButton.setEnabled(false);
		saveOpenButton.setEnabled(false);
		dialog.setSize(size);
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
					icon = icon.getScaledInstance(ICON_SIZE, ICON_SIZE, Image.SCALE_SMOOTH);
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
				// label.setBackground(colorManager.getHeaderColor(virtue.getTemplateId()));
				// label.setForeground(Color.white);
				// label.setForeground(colorManager.getHeaderColor(virtue.getTemplateId()));
				label.setBackground(colorManager.getBodyColor(virtue.getTemplateId()));
				label.setOpaque(true);
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
				JLabel label = new JLabel(state.toString(), SwingConstants.CENTER);
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
				Image icon = iconService.getImageNow(name);
				if (icon != null) {
					icon = icon.getScaledInstance(ICON_SIZE, ICON_SIZE, Image.SCALE_SMOOTH);
					label = new JLabel(new ImageIcon(icon));
					label.setToolTipText(name);
				} else {
					label = new JLabel(name);
				}
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
		label.setBorder(BorderFactory.createEmptyBorder(cellPadding, cellPadding, cellPadding, cellPadding));
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
