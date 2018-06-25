package com.ncc.savior.desktop.sidebar;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.SystemColor;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.ToolTipManager;
import javax.swing.border.LineBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ncc.savior.desktop.authorization.AuthorizationService;
import com.ncc.savior.desktop.authorization.DesktopUser;
import com.ncc.savior.desktop.sidebar.LoginPage.ILoginEventListener;
import com.ncc.savior.desktop.sidebar.SidebarController.VirtueChangeHandler;
import com.ncc.savior.desktop.virtues.IIconService;
import com.ncc.savior.desktop.virtues.VirtueService;
import com.ncc.savior.virtueadmin.model.ApplicationDefinition;
import com.ncc.savior.virtueadmin.model.desktop.DesktopVirtue;

/**
 * Class to start the utility sidebar of the Savior application
 *
 *
 */
public class Sidebar implements VirtueChangeHandler {
	private static final Logger logger = LoggerFactory.getLogger(Sidebar.class);

	private VirtueService virtueService;
	private Map<String, VirtueContainer> virtueIdToVc;
	private AuthorizationService authService;
	private IIconService iconService;

	private Iterator<Color> colorItr;
	private ArrayList<Color> colorList;
	private JFrame frame;
	private LoginPage lp;

	private JTextField textField;

	private JPanel desktopContainer;
	private static boolean applicationsOpen = true;
	private static boolean tileViewOpen = true;
	private static boolean favoritesViewOpen = false;
	private static boolean listViewOpen = false;
	private static boolean searchMode = false;
	private JScrollPane sp;
	private AppsTile at;
	private AppsList al;
	private VirtueTile vt;
	private VirtueList vl;
	private FavoritesView fv;

	public Sidebar(VirtueService virtueService, AuthorizationService authService, IIconService iconService,
			boolean useColors, String style) {
		this.authService = authService;
		this.virtueIdToVc = new HashMap<String, VirtueContainer>();
		this.virtueService = virtueService;
		this.iconService = iconService;
		this.textField = new JTextField();

		colorList = loadColors();
		colorItr = colorList.iterator();
	}

	private ArrayList<Color> loadColors() {
		ArrayList<Color> colors = new ArrayList<Color>();
		colors.add(new Color(4, 0, 252));
		colors.add(new Color(0, 135, 255));
		colors.add(new Color(0, 153, 0));
		colors.add(new Color(0, 204, 0));
		colors.add(new Color(165, 0, 0));
		colors.add(new Color(255, 33, 0));
		colors.add(new Color(209, 195, 0));
		colors.add(new Color(255, 246, 0));
		colors.add(new Color(204, 119, 0));
		colors.add(new Color(255, 140, 0));
		colors.add(new Color(92, 0, 173));
		colors.add(new Color(144, 0, 255));
		colors.add(new Color(191, 0, 156));
		colors.add(new Color(255, 0, 208));

		return colors;
	}

	public void start(JFrame frame, List<DesktopVirtue> initialVirtues)
			throws Exception {
		frame.setTitle("SAVIOR");
		this.frame = frame;
		this.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.frame.setSize(491, 620);

		startLogin();

		// DesktopUser user = authService.getUser();

		// String reqDomain = authService.getRequiredDomain();
		// if (user != null && user.getImage() != null) {
		// userImageView.setImage(user.getImage());
		// }
		// if (user == null || (reqDomain != null &&
		// !reqDomain.equals(user.getDomain()))) {
		// initiateLoginScreen();
		// }
	}

	public void startLogin() throws IOException {
		this.lp = new LoginPage(authService);
		frame.getContentPane().removeAll();
		frame.getContentPane().validate();
		frame.getContentPane().repaint();
		this.frame.getContentPane().add(lp.getContainer());
		frame.getContentPane().validate();
		frame.getContentPane().repaint();
		this.frame.setVisible(true);
		initiateLoginScreen(lp);
	}

	private void initiateLoginScreen(LoginPage lp) throws IOException {
		lp.addLoginEventListener(new ILoginEventListener() {
			@Override
			public void onLoginSuccess(DesktopUser user) throws IOException {
				frame.getContentPane().removeAll();
				frame.validate();
				frame.repaint();
				setup(user);
				frame.getContentPane().add(desktopContainer);
				frame.setSize(491, 620);
				setInitialViewPort();
				frame.setVisible(true);
			}

			@Override
			public void onLoginFailure(String username, String domain, RuntimeException e) {
				logger.warn("Login failure for domain=" + domain + " username=" + username, e);
			}

			@Override
			public void onCancel() {
				// do nothing, handled elsewhere
			}
		});
	}

	@Override
	public void changeVirtue(DesktopVirtue virtue) {
		VirtueContainer vmi = virtueIdToVc.get(virtue.getTemplateId());
		// if (vmi == null) {
		// vmi = virtueIdToVc.get(virtue.getTemplateId());
//			if (virtue.getId() != null) {
//				virtueIdToVc.remove(virtue.getTemplateId());
//				virtueIdToVc.put(virtue.getId(), vmi);
//			}
		// }
		vmi.updateVirtue(virtue);
	}

	// ***Updating Virtues***
	@Override
	public void addVirtue(DesktopVirtue virtue) throws IOException {
		VirtueContainer vc = new VirtueContainer(virtue, virtueService, getNextColor(),
				getNextColor());
		// String id = virtue.getId() == null ? virtue.getTemplateId() : virtue.getId();
		virtueIdToVc.put(virtue.getTemplateId(), vc);
		vt.addVirtueToRow(virtue, vc, vc.getRow());

		for (ApplicationDefinition ad : virtue.getApps().values()) {
			ApplicationDom dom = new ApplicationDom(ad);
			Image appImage = iconService.getImage(ad.getIconKey());

			VirtueApplicationItem appsTileVa = new VirtueApplicationItem(ad, virtueService, sp, vc, virtue, fv,
					dom.getChangeListener(), appImage);
			appsTileVa.tileSetup();
			appsTileVa.registerListener(dom.getChangeListener());

			VirtueApplicationItem vcAppsTileVa = new VirtueApplicationItem(ad, virtueService, sp, vc, virtue, fv,
					dom.getChangeListener(), appImage);
			vcAppsTileVa.tileSetup();
			vcAppsTileVa.registerListener(dom.getChangeListener());

			VirtueApplicationItem appsListVa = new VirtueApplicationItem(ad, virtueService, sp, vc, virtue, fv,
					dom.getChangeListener(), appImage);
			appsListVa.listSetup();
			appsListVa.registerListener(dom.getChangeListener());

			al.addApplication(ad, appsListVa);
			at.addApplication(ad, appsTileVa);
			vc.addApplication(ad, vcAppsTileVa);

			dom.addListener(appsTileVa.getChangeListener());
			dom.addListener(appsListVa.getChangeListener());
			dom.addListener(vcAppsTileVa.getChangeListener());
		}

		String keyword = textField.getText();
		at.search(keyword, null, va -> va.getApplicationName().toLowerCase().contains(keyword.toLowerCase()));
		al.search(keyword, null, va -> va.getApplicationName().toLowerCase().contains(keyword.toLowerCase()));
		fv.search(keyword, null, va -> va.getApplicationName().toLowerCase().contains(keyword.toLowerCase()));
		vt.search(keyword, null, null);

		sp.getViewport().validate();
	}

	@Override
	public void removeVirtue(DesktopVirtue virtue) {
		VirtueContainer vmi = virtueIdToVc.remove(virtue.getTemplateId());
		// if (vmi == null) {
		// vmi = virtueIdToVc.remove(virtue.getTemplateId());
		// }
		if (vmi != null) {
			for (ApplicationDefinition ad : virtue.getApps().values()) {
				at.removeApplication(ad, virtue);
				al.removeApplication(ad, virtue);
			}

			vt.removeVirtue(virtue);
		}
	}

	private Color getNextColor() {
		if (!colorItr.hasNext()) {
			colorItr = colorList.iterator();
		}
		return colorItr.next();
	}

	// This will setup the main display after login
	public void setup(DesktopUser user) throws IOException {
		ToolTipManager.sharedInstance().setReshowDelay(1);
		ToolTipManager.sharedInstance().setInitialDelay(1250);

		colorItr = colorList.iterator();
		this.desktopContainer = new JPanel();
		this.sp = new JScrollPane();
		this.at = new AppsTile(virtueService, sp);
		this.al = new AppsList(virtueService, sp);
		this.vt = new VirtueTile(sp);
		this.vl = new VirtueList();
		this.fv = new FavoritesView(virtueService, sp);
		desktopContainer.setLayout(new BorderLayout(0, 0));

		applicationsOpen = true;
		tileViewOpen = true;
		favoritesViewOpen = false;
		listViewOpen = false;

		JPanel topBorder = new JPanel();
		FlowLayout flowLayout = (FlowLayout) topBorder.getLayout();
		flowLayout.setAlignment(FlowLayout.LEFT);
		topBorder.setBackground(Color.DARK_GRAY);
		topBorder.setSize(20, 100);
		desktopContainer.add(topBorder, BorderLayout.NORTH);

		JLabel name = new JLabel(user.getUsername());
		name.setIcon(null);
		name.setForeground(Color.WHITE);
		name.setFont(new Font("Tahoma", Font.PLAIN, 17));
		topBorder.add(name);

		JPanel bottomBorder = new JPanel();
		FlowLayout flowLayout_1 = (FlowLayout) bottomBorder.getLayout();
		flowLayout_1.setVgap(0);
		bottomBorder.setBackground(Color.DARK_GRAY);
		desktopContainer.add(bottomBorder, BorderLayout.SOUTH);

		JLabel logoutLabel = new JLabel();

		ImageIcon imageIcon = new ImageIcon(Sidebar.class.getResource("/images/u73.png"));
		Image image = imageIcon.getImage(); // transform it
		Image newimg = image.getScaledInstance(27, 30, java.awt.Image.SCALE_SMOOTH); // scale it the smooth way
		imageIcon = new ImageIcon(newimg);  // transform it back
		logoutLabel.setIcon(imageIcon);

		bottomBorder.add(logoutLabel);

		JLabel logout = new JLabel("Logout");
		logout.setFont(new Font("Tahoma", Font.PLAIN, 19));
		logout.setForeground(Color.WHITE);
		bottomBorder.add(logout);

		JPanel center = new JPanel();
		desktopContainer.add(center, BorderLayout.CENTER);
		center.setLayout(new GridBagLayout());

		GridBagConstraints c = new GridBagConstraints();

		c.fill = GridBagConstraints.HORIZONTAL;

		JPanel applications = new JPanel();
		applications.setBorder(new LineBorder(SystemColor.windowBorder));
		applications.setBackground(SystemColor.scrollbar);
		c.weightx = 0.5;

		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 0;
		c.gridy = 0;
		center.add(applications, c);
		applications.setLayout(new BorderLayout(0, 4));

		JLabel applicationsLabel = new JLabel("Applications");
		applicationsLabel.setVerticalAlignment(SwingConstants.TOP);
		applicationsLabel.setHorizontalAlignment(SwingConstants.CENTER);
		applications.add(applicationsLabel);

		JPanel applicationsSelected = new JPanel();
		FlowLayout flowLayout_2 = (FlowLayout) applicationsSelected.getLayout();
		flowLayout_2.setVgap(1);
		applicationsSelected.setBackground(new Color(148, 0, 211));
		applications.add(applicationsSelected, BorderLayout.SOUTH);

		JPanel applicationsHeader = new JPanel();
		applicationsHeader.setBackground(SystemColor.scrollbar);
		applications.add(applicationsHeader, BorderLayout.NORTH);

		JPanel virtues = new JPanel();
		virtues.setBorder(new LineBorder(SystemColor.windowBorder));
		virtues.setBackground(SystemColor.scrollbar);
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 0.5;
		c.gridx = 1;
		c.gridy = 0;
		center.add(virtues, c);
		virtues.setLayout(new BorderLayout(0, 4));

		JLabel virtuesLabel = new JLabel("Virtues");
		virtuesLabel.setHorizontalAlignment(SwingConstants.CENTER);
		virtues.add(virtuesLabel);

		JPanel virtuesHeader = new JPanel();
		virtuesHeader.setBackground(SystemColor.scrollbar);
		virtues.add(virtuesHeader, BorderLayout.NORTH);

		JPanel virtuesSelected = new JPanel();
		FlowLayout flowLayout_3 = (FlowLayout) virtuesSelected.getLayout();
		flowLayout_3.setVgap(1);
		virtuesSelected.setBackground(SystemColor.scrollbar);
		virtues.add(virtuesSelected, BorderLayout.SOUTH);

		JPanel search = new JPanel();
		search.setBorder(new LineBorder(SystemColor.windowBorder));
		search.setBackground(SystemColor.scrollbar);
		search.setLayout(new GridBagLayout());
		c.fill = GridBagConstraints.BOTH;
		c.weightx = 0.5;
		c.gridx = 2;
		c.gridy = 0;
		center.add(search, c);

		c.gridx = 0;
		c.gridy = 0;
		c.weightx = 1.0;
		c.fill = GridBagConstraints.BOTH;

		JLabel searchLabel = new JLabel();
		searchLabel.setBackground(SystemColor.scrollbar);
		ImageIcon initialSearchIcon = new ImageIcon(AppsTile.class.getResource("/images/search.png"));
		Image searchImage = initialSearchIcon.getImage();
		Image newSearchImage = searchImage.getScaledInstance(24, 24, java.awt.Image.SCALE_SMOOTH);
		ImageIcon searchIcon = new ImageIcon(newSearchImage);

		ImageIcon closeIcon = (new ImageIcon(Sidebar.class.getResource("/images/close.png")));
		searchLabel.setIcon(searchIcon);

		textField.setColumns(6);
		textField.setFont(new Font("Tahoma", Font.PLAIN, 13));

		search.add(textField, c);

		c.weightx = 0.0;
		c.gridx = 1;
		search.add(searchLabel, c);

		JPanel icons = new JPanel();
		icons.setBorder(new LineBorder(SystemColor.windowBorder));
		icons.setBackground(new Color(248, 248, 255));
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 0.0;
		c.gridwidth = 3;
		c.gridx = 0;
		c.gridy = 1;
		center.add(icons, c);
		icons.setLayout(new FlowLayout(FlowLayout.RIGHT, 0, 0));

		ImageIcon inactiveListIcon = (new ImageIcon(Sidebar.class.getResource("/images/list-inactive2.png")));
		ImageIcon activeListIcon = (new ImageIcon(Sidebar.class.getResource("/images/list-active2.png")));
		JLabel listLabel = new JLabel(inactiveListIcon);
		listLabel.setBackground(new Color(248, 248, 255));

		ImageIcon inactiveTileIcon = (new ImageIcon(Sidebar.class.getResource("/images/tile-inactive2.png")));
		ImageIcon activeTileIcon = (new ImageIcon(Sidebar.class.getResource("/images/tile-active2.png")));
		JLabel tileLabel = new JLabel(activeTileIcon);
		tileLabel.setBackground(new Color(248, 248, 255));

		ImageIcon inactiveFavoriteIcon = (new ImageIcon(
				Sidebar.class.getResource("/images/favorite-inactive.png")));
		ImageIcon activeFavoriteIcon = (new ImageIcon(
				Sidebar.class.getResource("/images/favorite-active.png")));
		JLabel favoritesLabel = new JLabel(inactiveFavoriteIcon);
		favoritesLabel.setBackground(new Color(248, 248, 255));

		JPanel favoritesView = new JPanel();
		favoritesView.setBackground(new Color(248, 248, 255));
		favoritesView.add(favoritesLabel);
		icons.add(favoritesView);
		favoritesView.setToolTipText("Favorites view");

		JPanel listView = new JPanel();
		listView.setBackground(new Color(248, 248, 255));
		listView.add(listLabel);
		icons.add(listView);
		listView.setToolTipText("List view");

		JPanel tileView = new JPanel();
		tileView.setBackground(new Color(248, 248, 255));
		tileView.add(tileLabel);
		icons.add(tileView);
		tileView.setToolTipText("Tile view");

		sp.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		sp.setSize(300, 800);
		sp.setPreferredSize(new Dimension(0, 800));
		sp.getVerticalScrollBar().setUnitIncrement(16);
		c.fill = GridBagConstraints.BOTH;
		c.ipady = 0;
		c.weighty = 1.0; // request any extra vertical space
		c.anchor = GridBagConstraints.PAGE_END; // bottom of space
		c.gridx = 0;
		c.gridwidth = 3; // 3 columns wide
		c.gridy = 2; // third row
		center.add(sp, c);

		sp.getViewport().revalidate();
		sp.validate();
		sp.repaint();

		tileView.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent event) {
				if (!tileViewOpen) {
					tileViewOpen = true;
					favoritesViewOpen = false;
					listViewOpen = false;
					tileLabel.setIcon(activeTileIcon);
					listLabel.setIcon(inactiveListIcon);
					favoritesLabel.setIcon(inactiveFavoriteIcon);
				}
				if (applicationsOpen) {
					sp.setViewportView(at.getContainer());
				} else {
					sp.setViewportView(vt.getContainer());
				}
			}
		});

		listView.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent event) {
				if (!listViewOpen) {
					listViewOpen = true;
					tileViewOpen = false;
					favoritesViewOpen = false;
					listLabel.setIcon(activeListIcon);
					tileLabel.setIcon(inactiveTileIcon);
					favoritesLabel.setIcon(inactiveFavoriteIcon);
				}
				if (applicationsOpen) {
					sp.setViewportView(al.getContainer());
				} else {
					sp.setViewportView(vl.getContainer());
				}
			}
		});

		favoritesView.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent event) {
				if (!favoritesViewOpen) {
					favoritesViewOpen = true;
					tileViewOpen = false;
					listViewOpen = false;
					favoritesLabel.setIcon(activeFavoriteIcon);
					tileLabel.setIcon(inactiveTileIcon);
					listLabel.setIcon(inactiveListIcon);
				}
				if (applicationsOpen) {
					sp.setViewportView(fv.getContainer());
				}
			}
		});

		applications.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent event) {
				if (!applicationsOpen) {
					tileViewOpen = true;
					favoritesViewOpen = false;
					listViewOpen = false;
					applicationsOpen = true;
					listView.setVisible(true);
					favoritesView.setVisible(true);
					favoritesLabel.setIcon(inactiveFavoriteIcon);
					virtuesSelected.setBackground(SystemColor.scrollbar);
					applicationsSelected.setBackground(new Color(148, 0, 211));
					sp.setViewportView(at.getContainer());
				}
			}
		});

		virtues.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent event) {
				if (applicationsOpen) {
					applicationsOpen = false;
					tileLabel.setIcon(activeTileIcon);
					listLabel.setIcon(inactiveListIcon);
					listView.setVisible(false);
					favoritesView.setVisible(false);
					applicationsSelected.setBackground(SystemColor.scrollbar);
					virtuesSelected.setBackground(new Color(148, 0, 211));
					sp.setViewportView(vt.getContainer());
				}
			}
		});

		bottomBorder.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent event) {
				authService.logout();
				try {
					startLogin();
				} catch (IOException e) {
					String msg = "Error attempting to logout";
					logger.error(msg, e);
				}
			}
		});

		searchLabel.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent event) {
				if (searchMode) {
					searchMode = false;
					searchLabel.setIcon(searchIcon);
					textField.setText("");
					al.search(null, null, null);
					at.search(null, null, null);
					fv.search(null, null, null);
					vt.search(null, null, null);
					vt.search(null, null, null);
					sp.setViewportView(sp.getViewport().getView());
				}
			}
		});

		textField.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void insertUpdate(DocumentEvent e) {
				searchMode = true;
				String keyword = textField.getText();
				at.search(keyword, null, va -> va.getApplicationName().toLowerCase().contains(keyword.toLowerCase()));
				al.search(keyword, null, va -> va.getApplicationName().toLowerCase().contains(keyword.toLowerCase()));
				fv.search(keyword, null, va -> va.getApplicationName().toLowerCase().contains(keyword.toLowerCase()));
				vt.search(keyword, null, null);
				sp.setViewportView(sp.getViewport().getView());
				searchLabel.setIcon(closeIcon);
			}

			@Override
			public void changedUpdate(DocumentEvent e) {
			}

			@Override
			public void removeUpdate(DocumentEvent e) {
			}

		});

		textField.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent event) {
				if (event.getKeyCode() == KeyEvent.VK_ENTER) {
					if (textField.getText().equals("")) {
						searchMode = false;
						searchLabel.setIcon(searchIcon);
						textField.setText("");
						al.search(null, null, null);
						at.search(null, null, null);
						fv.search(null, null, null);
						vt.search(null, null, null);
						sp.setViewportView(sp.getViewport().getView());
					} else {
						searchMode = true;
						String keyword = textField.getText();
						at.search(keyword, null,
								va -> va.getApplicationName().toLowerCase().contains(keyword.toLowerCase()));
						al.search(keyword, null,
								va -> va.getApplicationName().toLowerCase().contains(keyword.toLowerCase()));
						fv.search(keyword, null,
								va -> va.getApplicationName().toLowerCase().contains(keyword.toLowerCase()));
						vt.search(keyword, null, null);
						sp.setViewportView(sp.getViewport().getView());
						searchLabel.setIcon(closeIcon);
					}
				}
			}
		});
	}

	public void setInitialViewPort() {
		sp.setViewportView(at.getContainer());
	}

	public JPanel getContainer() {
		return desktopContainer;
	}
}
