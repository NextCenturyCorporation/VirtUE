package com.ncc.savior.desktop.sidebar;

import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ncc.savior.desktop.virtues.VirtueService;
import com.ncc.savior.virtueadmin.model.ApplicationDefinition;
import com.ncc.savior.virtueadmin.model.VirtueState;
import com.ncc.savior.virtueadmin.model.desktop.DesktopVirtue;

public abstract class AbstractVirtueContainer {

	private static Logger logger = LoggerFactory.getLogger(AbstractVirtueContainer.class);

	protected static ImageIcon optionsIcon = new ImageIcon(
			AbstractVirtueContainer.class.getResource("/images/options.png"));
	protected static Image optionsImage = optionsIcon.getImage(); // transform it
	protected static Image scaledOptionsImage = optionsImage.getScaledInstance(24, 24, java.awt.Image.SCALE_SMOOTH);

	protected DesktopVirtue virtue;
	protected VirtueService virtueService;
	protected VirtueState status;
	protected JLabel statusLabel;
	protected JPanel header;

	protected JLabel optionsLabel;

	protected boolean dropDown = false;
	protected JPanel container;

	protected static int numRows = 0;
	protected int row;

	protected HashMap<String, VirtueApplicationItem> tiles;
	protected String headerTitle;

	protected GhostText ghostText;

	protected JScrollPane sp;
	protected JTextField textField;

	private static Set<IUpdateListener> updateListeners = new HashSet<IUpdateListener>();

	public AbstractVirtueContainer(DesktopVirtue virtue, VirtueService virtueService, JScrollPane sp,
			JTextField textField, GhostText ghostText) {
		this.virtue = virtue;
		this.virtueService = virtueService;
		this.sp = sp;
		this.tiles = new HashMap<String, VirtueApplicationItem>();
		this.headerTitle = virtue.getName();
		this.status = virtue.getVirtueState();
		this.textField = textField;
		this.ghostText = ghostText;

		resetRows();
	}

	public int getRow() {
		return row;
	}

	public void setRow(int row) {
		this.row = row;
	}

	public static void resetRows() {
		numRows = 0;
	}

	public JPanel getContainer() {
		return container;
	}

	public DesktopVirtue getVirtue() {
		return virtue;
	}

	public String getName() {
		return headerTitle;
	}

	public void addOptionsListener() {
		optionsLabel.setToolTipText("Click to start or stop a virtue");

		optionsLabel.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent event) {
				JPopupMenu pm = new JPopupMenu();
				JMenuItem mi1 = new JMenuItem("Stop");
				JMenuItem mi2 = new JMenuItem("Start");
				JMenuItem mi3 = new JMenuItem("Terminate");
				mi3.setToolTipText("Terminates the virtue state. (Cannot be undone)");
				JMenuItem mi4 = new JMenuItem("Reconnect");
				mi4.setToolTipText("Establishes a new connection for the virtue");

				mi1.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent evt) {
						try {
							virtueService.stopVirtue(virtue);
							updateVirtue(virtue);
						} catch (IOException e) {
							String msg = "Error attempting to stop virtue=" + virtue;
							logger.error(msg, e);
						}
					}
				});

				mi2.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent evt) {
						try {
							virtueService.startVirtue(virtue);
							updateVirtue(virtue);
						} catch (IOException e) {
							String msg = "Error attempting to start virtue=" + virtue;
							logger.error(msg, e);
						}
					}
				});

				mi3.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent evt) {
						try {
							virtueService.terminateVirtue(virtue);
							updateVirtue(virtue);
						} catch (IOException e) {
							String msg = "Error attempting to terminate virtue=" + virtue;
							logger.error(msg, e);
						}
					}
				});

				mi4.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent evt) {
						logger.debug("Reconnect not implemented yet");
					}
				});

				pm.setPopupSize(78, 97);
				pm.add(mi1);
				pm.addSeparator();
				pm.add(mi2);
				pm.addSeparator();
				pm.add(mi3);
				pm.addSeparator();
				pm.add(mi4);
				pm.show(optionsLabel, -44, 26);
			}
		});
	}

	public boolean containsKeyword(String keyword) {
		for (ApplicationDefinition ad : virtue.getApps().values()) {
			if (ad.getName().toLowerCase().contains(keyword.toLowerCase())) {
				return true;
			}
		}
		return false;
	}

	public abstract void updateVirtue(DesktopVirtue virtue);

	protected void triggerUpdateListener() {
		for (IUpdateListener listener : updateListeners) {
			listener.onUpdate();
		}
	}

	public static void addUpdateListener(IUpdateListener listener) {
		updateListeners.add(listener);
	}

	public static void removeUpdateListener(IUpdateListener listener) {
		updateListeners.remove(listener);
	}

	public static interface IUpdateListener {

		public void onUpdate();

	}

}
