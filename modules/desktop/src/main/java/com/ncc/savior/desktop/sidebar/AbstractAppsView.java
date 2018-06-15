package com.ncc.savior.desktop.sidebar;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.HashMap;

import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ncc.savior.desktop.virtues.VirtueService;
import com.ncc.savior.virtueadmin.model.ApplicationDefinition;
import com.ncc.savior.virtueadmin.model.VirtueState;
import com.ncc.savior.virtueadmin.model.desktop.DesktopVirtue;

/**
 *
 * This is a superclass for appsList, appsTile, and favoritesView and it
 * includes the basic common components of each different view
 *
 */

public abstract class AbstractAppsView {

	private static final Logger logger = LoggerFactory.getLogger(AbstractAppsView.class);

	protected JPanel container;
	protected VirtueService virtueService;
	protected JScrollPane sp;
	protected HashMap<String, JPanel> tiles;

	public AbstractAppsView(VirtueService vs, JScrollPane sp) {
		this.container = new JPanel();
		this.virtueService = vs;
		this.sp = sp;
		this.tiles = new HashMap<String, JPanel>();
	}

	public void addListener(JPanel tile, VirtueContainer vc, FavoritesView fv, ApplicationDefinition ad,
			DesktopVirtue virtue) {
		tile.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent event) {
				if (SwingUtilities.isLeftMouseButton(event)) {
					JPopupMenu pm = new JPopupMenu();
					JMenuItem mi1 = new JMenuItem("Yes");
					JMenuItem mi2 = new JMenuItem("No");
					pm.add(new JLabel("Would you like to start a " + ad.getName() + " application?"));

					mi1.addActionListener(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent evt) {
							try {
								virtueService.startApplication(virtue, ad, new RgbColor(0, 0, 0, 0));
								virtue.setVirtueState(VirtueState.LAUNCHING);
								vc.updateVirtue(virtue);
							} catch (IOException e) {
								String msg = "Error attempting to start a " + ad.getName() + " application";
								logger.error(msg, e);
							}
						}
					});

					pm.setPopupSize(375, 75);
					pm.add(mi1);
					pm.add(mi2);
					pm.show(sp, 50, 150);
				}

				if (SwingUtilities.isRightMouseButton(event)) {
					JPopupMenu pm = new JPopupMenu();
					JMenuItem mi1 = new JMenuItem("Yes");
					JMenuItem mi2 = new JMenuItem("No");
					pm.add(new JLabel("Would you like to favorite the " + ad.getName() + " application?"));

					mi1.addActionListener(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent evt) {
							try {
								fv.addFavorite(ad, virtue, vc);
							} catch (IOException e) {
								String msg = "Error attempting to favorite a " + ad.getName() + " application";
								logger.error(msg, e);
							}
						}
					});

					pm.setPopupSize(375, 75);
					pm.add(mi1);
					pm.add(mi2);
					pm.show(sp, 50, 150);
				}
			}
		});
	}

	public void removeVirtue(DesktopVirtue virtue) {
		container.remove(tiles.get(virtue.getName()));
		container.validate();
		container.repaint();
		tiles.remove(virtue.getName());
		container.validate();
		container.repaint();
	}

}
