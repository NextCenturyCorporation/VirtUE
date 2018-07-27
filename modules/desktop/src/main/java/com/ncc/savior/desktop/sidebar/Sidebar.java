package com.ncc.savior.desktop.sidebar;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.SystemColor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;
import java.util.prefs.Preferences;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.DefaultListCellRenderer;
import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
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

	private ImageIcon inactiveFavoriteIcon = (new ImageIcon(
			Sidebar.class.getResource("/images/favorite-inactive.png")));
	private ImageIcon activeFavoriteIcon = (new ImageIcon(Sidebar.class.getResource("/images/favorite-active.png")));

	private ImageIcon inactiveTileIcon = (new ImageIcon(Sidebar.class.getResource("/images/tile-inactive2.png")));
	private ImageIcon activeTileIcon = (new ImageIcon(Sidebar.class.getResource("/images/tile-active2.png")));

	private ImageIcon inactiveListIcon = (new ImageIcon(Sidebar.class.getResource("/images/list-inactive2.png")));
	private ImageIcon activeListIcon = (new ImageIcon(Sidebar.class.getResource("/images/list-active2.png")));

	private static ImageIcon saviorIcon = new ImageIcon(AppsTile.class.getResource("/images/saviorLogo.png"));

	private static Image defaultImage = saviorIcon.getImage();
	private static Image saviorTile = defaultImage.getScaledInstance(47, 50, java.awt.Image.SCALE_SMOOTH);
	private static Image saviorList = defaultImage.getScaledInstance(30, 30, java.awt.Image.SCALE_SMOOTH);

	private static ImageIcon loadingIcon = new ImageIcon(AppsTile.class.getResource("/images/loading.gif"));

	private ImageIcon searchIcon;
	private ImageIcon closeIcon = new ImageIcon(Sidebar.class.getResource("/images/close-button.png"));

	private VirtueService virtueService;
	private Map<String, VirtueTileContainer> virtueIdToVtc;
	private Map<String, VirtueListContainer> virtueIdToVlc;
	private AuthorizationService authService;
	private IIconService iconService;

	private Iterator<Color> colorItr;
	private ArrayList<Color> colorList;
	private JFrame frame;
	private LoginPage lp;

	private GhostText ghostText;

	private JTextField textField;
	private JLabel favoritesLabel;
	private JLabel tileLabel;
	private JLabel listLabel;

	private JLabel searchLabel;
	private JLabel about;

	private JPanel virtues;
	private JPanel applications;

	private JPanel listView;
	private JPanel favoritesView;
	private JPanel tileView;

	private JPanel virtuesSelected;
	private JPanel applicationsSelected;

	private JPanel bottomBorder;

	private JComboBox<String> cb;

	private JPanel desktopContainer;
	private boolean applicationsOpen = true;

	private DesktopView desktopView;

	private boolean searchMode = false;

	private JScrollPane sp;
	private AppsTile at;
	private AppsList al;
	private VirtueTile vt;
	private VirtueList vl;
	private FavoritesView fv;

	private Preferences favorites;
	private Preferences lastView;
	private Preferences lastSort;

	public static boolean askAgain = true;
	private boolean loading = true;

	private Comparator<VirtueApplicationItem> sortAppsByStatus;
	private Comparator<VirtueTileContainer> sortVtByStatus;
	private Comparator<VirtueListContainer> sortVlByStatus;

	private JPanel loadingContainer;

	public Sidebar(VirtueService virtueService, AuthorizationService authService, IIconService iconService,
			boolean useColors, String style) {
		this.authService = authService;
		this.virtueIdToVtc = new HashMap<String, VirtueTileContainer>();
		this.virtueIdToVlc = new HashMap<String, VirtueListContainer>();
		this.virtueService = virtueService;
		this.iconService = iconService;

		this.textField = new JTextField();
		this.searchLabel = new JLabel();
		textField.setColumns(6);
		textField.setForeground(Color.BLACK);
		textField.setFont(new Font("Tahoma", Font.PLAIN, 13));

		ImageIcon initialSearchIcon = new ImageIcon(AppsTile.class.getResource("/images/search.png"));
		Image searchImage = initialSearchIcon.getImage();
		Image newSearchImage = searchImage.getScaledInstance(22, 22, java.awt.Image.SCALE_SMOOTH);
		this.searchIcon = new ImageIcon(newSearchImage);

		this.ghostText = new GhostText(textField, "search", searchLabel, searchIcon);

		colorList = loadColors();
		colorItr = colorList.iterator();
		setupComparators();
		setupLoadingGif();
	}

	private void setupLoadingGif() {
		this.loadingContainer = new JPanel();
		loadingContainer.setLayout(new BorderLayout());
		JLabel gifLabel = new JLabel();
		gifLabel.setIcon(loadingIcon);
		gifLabel.setVerticalAlignment(SwingConstants.CENTER);
		gifLabel.setHorizontalAlignment(SwingConstants.CENTER);
		loadingContainer.add(gifLabel, BorderLayout.CENTER);
	}

	private ArrayList<Color> loadColors() {
		ArrayList<Color> colors = new ArrayList<Color>();
		colors.add(new Color(189, 0, 38));
		colors.add(new Color(227, 26, 28));

		colors.add(new Color(34, 94, 168));
		colors.add(new Color(29, 145, 192));

		colors.add(new Color(35, 132, 67));
		colors.add(new Color(65, 171, 93));

		colors.add(new Color(204, 76, 2));
		colors.add(new Color(236, 112, 20));


		colors.add(new Color(136, 65, 157));
		colors.add(new Color(140, 107, 177));

		colors.add(new Color(206, 18, 86));
		colors.add(new Color(231, 41, 138));

		colors.add(new Color(106, 81, 163));
		colors.add(new Color(128, 125, 186));

		colors.add(new Color(203, 24, 29));
		colors.add(new Color(239, 59, 44));

		colors.add(new Color(191, 129, 45));
		colors.add(new Color(223, 194, 125));

		colors.add(new Color(53, 151, 143));
		colors.add(new Color(128, 205, 193));

		colors.add(new Color(127, 188, 65));
		colors.add(new Color(184, 225, 134));

		return colors;
	}

	public void start(JFrame frame, List<DesktopVirtue> initialVirtues) throws Exception {
		frame.setTitle("SAVIOR");
		frame.setIconImage(saviorIcon.getImage());
		this.frame = frame;
		this.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.frame.setSize(491, 600);
		this.frame.setMinimumSize(new Dimension(380, 200));

		DesktopUser user = authService.getUser();
		if (user != null) {
			onLogin(user);
		} else {
			startLogin();
		}

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
				onLogin(user);
				ghostText.reset();
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

	protected void onLogin(DesktopUser user) throws IOException {
		favorites = Preferences.userRoot().node("VirtUE/Desktop/" + user.getUsername() + "/favorites");
		lastView = Preferences.userRoot().node("VirtUE/Desktop/" + user.getUsername() + "/lastView");
		lastSort = Preferences.userRoot().node("VirtUE/Desktop/" + user.getUsername() + "/lastSort");

		frame.getContentPane().removeAll();
		frame.validate();
		frame.repaint();
		setup(user);
		frame.getContentPane().add(desktopContainer);
		frame.setSize(491, 600);
		setInitialViewPort();
		sp.setViewportView(loadingContainer);
		frame.setVisible(true);
	}

	@Override
	public void changeVirtue(DesktopVirtue virtue) {
		VirtueTileContainer vtc = virtueIdToVtc.get(virtue.getTemplateId());
		VirtueListContainer vlc = virtueIdToVlc.get(virtue.getTemplateId());
		// if (vmi == null) {
		// vmi = virtueIdToVc.get(virtue.getTemplateId());
		// if (virtue.getId() != null) {
		// virtueIdToVc.remove(virtue.getTemplateId());
		// virtueIdToVc.put(virtue.getId(), vmi);
		// }
		// }
		vtc.updateVirtue(virtue);
		vlc.updateVirtue(virtue);

		for (ApplicationDefinition ad : virtue.getApps().values()) {
			al.updateApp(ad, virtue);
			at.updateApp(ad, virtue);
			fv.updateApp(ad, virtue);
			vl.updateApp(ad, virtue);
			vt.updateApp(ad, virtue);
		}
	}

	// ***Updating Virtues***
	@Override
	public void addVirtues(List<DesktopVirtue> virtues) throws IOException, InterruptedException, ExecutionException {
		if (loading) {
			loading = false;
			setInitialViewPort();
		}

		for (DesktopVirtue virtue : virtues) {
			Color headerColor = getNextColor();
			VirtueTileContainer vtc = new VirtueTileContainer(virtue, virtueService, headerColor, getNextColor(), sp,
					textField, ghostText, vt);
			vt.addVirtueToRow(virtue, vtc, vtc.getRow());

			VirtueListContainer vlc = new VirtueListContainer(virtue, virtueService, headerColor, sp, textField,
					ghostText, vl);
			vl.addVirtueToRow(virtue, vlc, vlc.getRow());

			virtueIdToVtc.put(virtue.getTemplateId(), vtc);
			virtueIdToVlc.put(virtue.getTemplateId(), vlc);

			for (ApplicationDefinition ad : virtue.getApps().values()) {

				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						try {
							boolean isFavorited = favorites.getBoolean(ad.getId() + virtue.getTemplateId(), false);
							ApplicationDom dom = new ApplicationDom(ad, isFavorited);

							VirtueApplicationItem appsTileVa = new VirtueApplicationItem(ad, virtueService, sp, vtc,
									virtue, fv, dom.getChangeListener(), saviorTile, isFavorited, frame, textField, cb,
									sortAppsByStatus, ghostText);
							appsTileVa.tileSetup();
							appsTileVa.registerListener(dom.getChangeListener());
							at.addApplication(ad, appsTileVa);

							VirtueApplicationItem vtcAppsTileVa = new VirtueApplicationItem(ad, virtueService, sp, vtc,
									virtue, fv, dom.getChangeListener(), saviorTile, isFavorited, frame, textField, cb,
									sortAppsByStatus, ghostText);
							vtcAppsTileVa.tileSetup();
							vtcAppsTileVa.registerListener(dom.getChangeListener());

							VirtueApplicationItem vlcAppsListVa = new VirtueApplicationItem(ad, virtueService, sp, vtc,
									virtue, fv, dom.getChangeListener(), saviorList, isFavorited, frame, textField, cb,
									sortAppsByStatus, ghostText);
							vlcAppsListVa.listSetup();
							vlcAppsListVa.registerListener(dom.getChangeListener());

							VirtueApplicationItem appsListVa = new VirtueApplicationItem(ad, virtueService, sp, vtc, virtue,
									fv, dom.getChangeListener(), saviorList, isFavorited, frame, textField, cb,
									sortAppsByStatus, ghostText);
							appsListVa.listSetup();
							appsListVa.registerListener(dom.getChangeListener());

							al.addApplication(ad, appsListVa);
							vtc.addApplication(ad, vtcAppsTileVa);
							vlc.addApplication(ad, vlcAppsListVa);

							Consumer<Image> consumer = i -> {
								appsTileVa.setTileImage(i);
								vtcAppsTileVa.setTileImage(i);
								vlcAppsListVa.setListImage(i);
								appsListVa.setListImage(i);
								fv.setTileImage(ad, virtue, i);
							};

							iconService.getImage(ad.getIconKey(), consumer);

							dom.addListener(appsTileVa.getChangeListener());
							dom.addListener(appsListVa.getChangeListener());
							dom.addListener(vtcAppsTileVa.getChangeListener());
							dom.addListener(vlcAppsListVa.getChangeListener());

							if (isFavorited) {
								String selected = (String) cb.getSelectedItem();
								VirtueApplicationItem favoritedVa;
								switch (selected) {
								case "Alphabetical":
									favoritedVa = new VirtueApplicationItem(ad, virtueService, sp, vtc, virtue, fv,
											dom.getChangeListener(), saviorTile, true, frame, textField, cb, null,
											ghostText);
									favoritedVa.tileSetup();
									fv.addFavorite(ad, virtue, favoritedVa, textField, null, ghostText);
									break;
								case "Status":
									favoritedVa = new VirtueApplicationItem(ad, virtueService, sp, vtc, virtue, fv,
											dom.getChangeListener(), saviorTile, true, frame, textField, cb, null,
											ghostText);
									favoritedVa.tileSetup();
									fv.addFavorite(ad, virtue, favoritedVa, textField, sortAppsByStatus, ghostText);
									break;
								}
							}
						} catch (Exception e) {
							logger.debug("Error with adding virtues");
						}
					}
				});

			}
		}

		String keyword = textField.getText();
		if (ghostText.getIsVisible()) {
			keyword = "";
		}
		sortByOption(keyword);

		sp.getViewport().validate();
	}

	@Override
	public void removeVirtue(DesktopVirtue virtue) {
		VirtueTileContainer vtc = virtueIdToVtc.remove(virtue.getTemplateId());
		virtueIdToVlc.remove(virtue.getTemplateId());
		// if (vmi == null) {
		// vmi = virtueIdToVc.remove(virtue.getTemplateId());
		// }
		if (vtc != null) {
			for (ApplicationDefinition ad : virtue.getApps().values()) {
				at.removeApplication(ad, virtue);
			}

			al.removeVirtue(virtue);
			vt.removeVirtue(virtue);
			vl.removeVirtue(virtue);
		}
	}

	private Color getNextColor() {
		if (!colorItr.hasNext()) {
			colorItr = colorList.iterator();
		}
		return colorItr.next();
	}

	// This will setup the main display after login
	@SuppressWarnings("serial")
	public void setup(DesktopUser user) throws IOException {
		ToolTipManager.sharedInstance().setReshowDelay(1);
		ToolTipManager.sharedInstance().setInitialDelay(1250);

		Image closeImage = closeIcon.getImage();
		Image newCloseImg = closeImage.getScaledInstance(22, 22, java.awt.Image.SCALE_SMOOTH);
		closeIcon = new ImageIcon(newCloseImg);

		colorItr = colorList.iterator();
		this.desktopContainer = new JPanel();
		this.sp = new JScrollPane();
		this.at = new AppsTile(virtueService, sp);
		this.al = new AppsList(virtueService, sp);
		this.vt = new VirtueTile(sp);
		this.vl = new VirtueList(sp);
		this.fv = new FavoritesView(virtueService, sp, favorites);
		desktopContainer.setLayout(new BorderLayout(0, 0));

		applicationsOpen = true;

		JPanel topBorder = new JPanel();
		topBorder.setLayout(new BorderLayout());
		topBorder.setBackground(Color.DARK_GRAY);
		topBorder.setSize(20, 100);
		desktopContainer.add(topBorder, BorderLayout.NORTH);

		JLabel name = new JLabel(user.getUsername());
		name.setFont(new Font("Roboto", Font.PLAIN, 17));
		name.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 0));
		name.setIcon(null);
		name.setForeground(Color.WHITE);
		topBorder.add(name, BorderLayout.WEST);

		ImageIcon aboutIcon = new ImageIcon(Sidebar.class.getResource("/images/info-icon.png"));
		Image aboutImage = aboutIcon.getImage();
		Image newAboutImg = aboutImage.getScaledInstance(20, 20, java.awt.Image.SCALE_SMOOTH);
		aboutIcon = new ImageIcon(newAboutImg);
		this.about = new JLabel();
		about.setIcon(aboutIcon);
		about.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 10));
		topBorder.add(about, BorderLayout.EAST);

		this.bottomBorder = new JPanel();
		FlowLayout flowLayout_1 = (FlowLayout) bottomBorder.getLayout();
		flowLayout_1.setVgap(0);
		bottomBorder.setBorder(BorderFactory.createEmptyBorder(4, 0, 4, 0));
		bottomBorder.setBackground(Color.DARK_GRAY);
		desktopContainer.add(bottomBorder, BorderLayout.SOUTH);

		JLabel logoutLabel = new JLabel();

		ImageIcon imageIcon = new ImageIcon(Sidebar.class.getResource("/images/u73.png"));
		Image image = imageIcon.getImage(); // transform it
		Image newimg = image.getScaledInstance(27, 30, java.awt.Image.SCALE_SMOOTH); // scale it the smooth way
		imageIcon = new ImageIcon(newimg); // transform it back
		logoutLabel.setIcon(imageIcon);

		bottomBorder.add(logoutLabel);

		JLabel logout = new JLabel("Logout");
		logout.setFont(new Font("Roboto", Font.PLAIN, 17));
		logout.setForeground(Color.WHITE);
		bottomBorder.add(logout);

		JPanel center = new JPanel();
		desktopContainer.add(center, BorderLayout.CENTER);
		center.setLayout(new GridBagLayout());

		GridBagConstraints c = new GridBagConstraints();

		c.fill = GridBagConstraints.HORIZONTAL;
		c.anchor = GridBagConstraints.CENTER;

		this.applications = new JPanel();
		applications.setMinimumSize(new Dimension(140, 38));
		applications.setBorder(new LineBorder(SystemColor.windowBorder));
		applications.setBackground(new Color(239, 239, 239));
		c.weightx = 0.5;

		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 0;
		c.gridy = 0;
		center.add(applications, c);
		applications.setLayout(new GridBagLayout());

		GridBagConstraints appConstraints = new GridBagConstraints();
		appConstraints.gridx = 0;
		appConstraints.gridy = 0;
		appConstraints.insets = new Insets(8, 0, 0, 0);

		JLabel applicationsLabel = new JLabel("Applications");
		applicationsLabel.setFont(new Font("Roboto", Font.PLAIN, 14));
		applicationsLabel.setVerticalAlignment(SwingConstants.CENTER);
		applicationsLabel.setHorizontalAlignment(SwingConstants.CENTER);
		applications.add(applicationsLabel, appConstraints);

		appConstraints.gridy = 1;
		appConstraints.insets = new Insets(6, 0, 0, 0);
		appConstraints.ipady = 1;
		appConstraints.weightx = 1.0;
		appConstraints.anchor = GridBagConstraints.PAGE_END;
		appConstraints.fill = GridBagConstraints.HORIZONTAL;
		this.applicationsSelected = new JPanel();
		FlowLayout flowLayout_2 = (FlowLayout) applicationsSelected.getLayout();
		flowLayout_2.setVgap(1);
		applicationsSelected.setBackground(new Color(153, 51, 204));
		applications.add(applicationsSelected, appConstraints);

		this.virtues = new JPanel();
		virtues.setMinimumSize(new Dimension(140, 38));
		virtues.setBorder(new LineBorder(SystemColor.windowBorder));
		virtues.setBackground(new Color(239, 239, 239));
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 0.5;
		c.gridx = 1;
		c.gridy = 0;
		center.add(virtues, c);
		virtues.setLayout(new GridBagLayout());

		GridBagConstraints virtuesConstraints = new GridBagConstraints();
		virtuesConstraints.gridx = 0;
		virtuesConstraints.gridy = 0;
		virtuesConstraints.insets = new Insets(8, 0, 0, 0);

		JLabel virtuesLabel = new JLabel("Virtues");
		virtuesLabel.setFont(new Font("Roboto", Font.PLAIN, 14));
		virtuesLabel.setHorizontalAlignment(SwingConstants.CENTER);
		virtues.add(virtuesLabel, virtuesConstraints);

		virtuesConstraints.gridy = 1;
		virtuesConstraints.insets = new Insets(6, 0, 0, 0);
		virtuesConstraints.ipady = 1;
		virtuesConstraints.weightx = 1.0;
		virtuesConstraints.anchor = GridBagConstraints.PAGE_END;
		virtuesConstraints.fill = GridBagConstraints.HORIZONTAL;
		this.virtuesSelected = new JPanel();
		FlowLayout flowLayout_3 = (FlowLayout) virtuesSelected.getLayout();
		flowLayout_3.setVgap(1);
		virtuesSelected.setBackground(new Color(239, 239, 239));
		virtues.add(virtuesSelected, virtuesConstraints);

		JPanel search = new JPanel();
		search.setMinimumSize(new Dimension(140, 38));
		search.setMinimumSize(new Dimension(140, 38));
		search.setBorder(new LineBorder(SystemColor.windowBorder));
		search.setBackground(new Color(239, 239, 239));
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

		searchLabel.setBackground(new Color(239, 239, 239));

		searchLabel.setIcon(searchIcon);

		c.insets = new Insets(0, 9, 0, 0);

		search.add(textField, c);

		c.insets = new Insets(0, 5, 0, 5);
		c.weightx = 0.0;
		c.gridx = 1;
		search.add(searchLabel, c);

		c.insets = new Insets(0, 0, 0, 0);
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 0.5;
		c.gridx = 2;
		c.gridy = 1;

		JPanel icons = new JPanel();
		icons.setBackground(new Color(248, 248, 255));
		center.add(icons, c);
		icons.setLayout(new FlowLayout(FlowLayout.RIGHT, 5, 0));

		c.fill = GridBagConstraints.BOTH;
		c.gridx = 0;
		c.gridwidth = 2;
		JPanel sortBy = new JPanel();
		sortBy.setBackground(new Color(248, 248, 255));
		sortBy.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
		center.add(sortBy, c);

		JLabel sortByLabel = new JLabel("sorted by: ");
		sortByLabel.setFont(new Font("Roboto", Font.PLAIN, 14));
		sortByLabel.setBorder(BorderFactory.createEmptyBorder(5, 10, 0, 0));
		String[] sortingOptions = { "Alphabetical", "Status" };
		this.cb = new JComboBox<String>(sortingOptions);
		cb.setSelectedItem(lastSort.get("sort", "Alphabetical"));
		cb.setBorder(BorderFactory.createEmptyBorder(7, 0, 0, 0));
		cb.setBackground(new Color(248, 248, 255));
		Color bgColor = cb.getBackground();
		cb.setRenderer(new DefaultListCellRenderer() {
			@Override
			public void paint(Graphics g) {
				setBackground(bgColor);
				super.paint(g);
			}
		});

		cb.setVisible(true);
		sortBy.add(sortByLabel);
		sortBy.add(cb);

		this.listLabel = new JLabel(inactiveListIcon);
		listLabel.setBackground(new Color(248, 248, 255));

		this.tileLabel = new JLabel(activeTileIcon);
		tileLabel.setBackground(new Color(248, 248, 255));

		this.favoritesLabel = new JLabel(inactiveFavoriteIcon);
		favoritesLabel.setBackground(new Color(248, 248, 255));

		this.favoritesView = new JPanel();
		favoritesView.setBackground(new Color(248, 248, 255));
		favoritesView.add(favoritesLabel);
		icons.add(favoritesView);
		favoritesView.setToolTipText("Favorites view");

		this.listView = new JPanel();
		listView.setBackground(new Color(248, 248, 255));
		listView.add(listLabel);
		icons.add(listView);
		listView.setToolTipText("List view");

		this.tileView = new JPanel();
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

		frame.pack();

		addEventListeners();
	}

	public void addEventListeners() {
		tileView.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent event) {
				if (applicationsOpen) {
					if (desktopView != DesktopView.APPS_TILE) {
						renderAppsTileView();
					}
				} else {
					if (desktopView != DesktopView.VIRTUE_TILE) {
						renderVirtueTileView();
					}
				}
			}
		});

		listView.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent event) {
				if (applicationsOpen) {
					if (desktopView != DesktopView.APPS_LIST) {
						renderAppsListView();
					}
				} else {
					if (desktopView != DesktopView.VIRTUE_LIST) {
						renderVirtueListView();
					}
				}
			}
		});

		favoritesView.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent event) {
				if (desktopView != DesktopView.FAVORITES) {
					renderFavoritesView();
				}
			}
		});

		applications.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent event) {
				if (!applicationsOpen) {
					renderAppsTileView();
				}
			}
		});

		virtues.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent event) {
				if (applicationsOpen) {
					renderVirtueTileView();
				}
			}
		});

		bottomBorder.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent event) {
				authService.logout();
				loading = true;
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
					resetViews();
				}
			}
		});

		textField.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void insertUpdate(DocumentEvent e) {
				searchMode = true;
				String keyword = textField.getText();
				if (ghostText.getIsVisible()) {
					keyword = "";
				}
				sortByOption(keyword);
				sp.setViewportView(sp.getViewport().getView());
				searchLabel.setIcon(closeIcon);
			}

			@Override
			public void changedUpdate(DocumentEvent e) {
			}

			@Override
			public void removeUpdate(DocumentEvent e) {
				searchMode = true;
				String keyword = textField.getText();
				if (ghostText.getIsVisible()) {
					keyword = "";
				}
				sortByOption(keyword);
				sp.setViewportView(sp.getViewport().getView());
				if (keyword.equals("")) {
					searchLabel.setIcon(searchIcon);
				} else {
					searchLabel.setIcon(closeIcon);
				}
			}

		});

		textField.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent event) {
				if (event.getKeyCode() == KeyEvent.VK_ENTER) {
					if (textField.getText().equals("")) {
						resetViews();
					} else {
						searchMode = true;
						String keyword = textField.getText();
						if (ghostText.getIsVisible()) {
							keyword = "";
						}
						sortByOption(keyword);
						sp.setViewportView(sp.getViewport().getView());
						searchLabel.setIcon(closeIcon);
					}
				}
			}
		});

		cb.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				String selected = (String) cb.getSelectedItem();
				lastSort.put("sort", selected);
				String keyword = textField.getText();
				if (ghostText.getIsVisible()) {
					keyword = "";
				}
				sortByOption(keyword);
			}

		});

		about.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent event) {
				setupDialog();
			}
		});
	}

	public void sortByOption(String keyword) {
		String selected = (String) cb.getSelectedItem();
		switch (selected) {
		case "Alphabetical":
			al.search(keyword, null, va -> va.getApplicationName().toLowerCase().contains(keyword.toLowerCase()));
			at.search(keyword, null, va -> va.getApplicationName().toLowerCase().contains(keyword.toLowerCase()));
			fv.search(keyword, null, va -> va.getApplicationName().toLowerCase().contains(keyword.toLowerCase()));
			vt.search(keyword, null, null);
			vl.search(keyword, null, null);
			break;
		case "Status":
			al.search(keyword, sortAppsByStatus,
					va -> va.getApplicationName().toLowerCase().contains(keyword.toLowerCase()));
			at.search(keyword, sortAppsByStatus,
					va -> va.getApplicationName().toLowerCase().contains(keyword.toLowerCase()));
			fv.search(keyword, sortAppsByStatus,
					va -> va.getApplicationName().toLowerCase().contains(keyword.toLowerCase()));
			vt.search(keyword, sortVtByStatus, null);
			vl.search(keyword, sortVlByStatus, null);
			break;
		}
	}

	public void renderFavoritesView() {
		lastView.put("view", "fv");
		desktopView = DesktopView.FAVORITES;
		applicationsOpen = true;
		favoritesView.setVisible(true);
		virtuesSelected.setBackground(new Color(239, 239, 239));
		applicationsSelected.setBackground(new Color(153, 51, 204));
		favoritesLabel.setIcon(activeFavoriteIcon);
		tileLabel.setIcon(inactiveTileIcon);
		listLabel.setIcon(inactiveListIcon);
		if (!loading) {
			sp.setViewportView(fv.getContainer());
		}
	}

	public void renderAppsListView() {
		lastView.put("view", "al");
		desktopView = DesktopView.APPS_LIST;
		applicationsOpen = true;
		favoritesView.setVisible(true);
		virtuesSelected.setBackground(new Color(239, 239, 239));
		applicationsSelected.setBackground(new Color(153, 51, 204));
		favoritesLabel.setIcon(inactiveFavoriteIcon);
		tileLabel.setIcon(inactiveTileIcon);
		listLabel.setIcon(activeListIcon);
		if (!loading) {
			sp.setViewportView(al.getContainer());
		}
	}

	public void renderAppsTileView() {
		lastView.put("view", "at");
		desktopView = DesktopView.APPS_TILE;
		applicationsOpen = true;
		favoritesView.setVisible(true);
		virtuesSelected.setBackground(new Color(239, 239, 239));
		applicationsSelected.setBackground(new Color(153, 51, 204));
		favoritesLabel.setIcon(inactiveFavoriteIcon);
		tileLabel.setIcon(activeTileIcon);
		listLabel.setIcon(inactiveListIcon);
		if (!loading) {
			sp.setViewportView(at.getContainer());
		}
	}

	public void renderVirtueTileView() {
		lastView.put("view", "vt");
		desktopView = DesktopView.VIRTUE_TILE;
		applicationsOpen = false;
		tileLabel.setIcon(activeTileIcon);
		listLabel.setIcon(inactiveListIcon);
		favoritesView.setVisible(false);
		applicationsSelected.setBackground(new Color(239, 239, 239));
		virtuesSelected.setBackground(new Color(153, 51, 204));
		if (!loading) {
			sp.setViewportView(vt.getContainer());
		}
	}

	public void renderVirtueListView() {
		lastView.put("view", "vl");
		desktopView = DesktopView.VIRTUE_LIST;
		applicationsOpen = false;
		tileLabel.setIcon(inactiveTileIcon);
		listLabel.setIcon(activeListIcon);
		favoritesView.setVisible(false);
		applicationsSelected.setBackground(new Color(239, 239, 239));
		virtuesSelected.setBackground(new Color(153, 51, 204));
		if (!loading) {
			sp.setViewportView(vl.getContainer());
		}
	}

	public void resetViews() {
		searchMode = false;
		searchLabel.setIcon(searchIcon);
		textField.setText("");
		String selected = (String) cb.getSelectedItem();
		switch (selected) {
		case "Alphabetical":
			al.search(null, null, null);
			at.search(null, null, null);
			fv.search(null, null, null);
			vt.search(null, null, null);
			vl.search(null, null, null);
			break;
		case "Status":
			al.search(null, sortAppsByStatus, null);
			at.search(null, sortAppsByStatus, null);
			fv.search(null, sortAppsByStatus, null);
			vt.search(null, sortVtByStatus, null);
			vl.search(null, sortVlByStatus, null);
			break;
		}
		sp.setViewportView(sp.getViewport().getView());
	}

	public void setupComparators() {
		this.sortAppsByStatus = new Comparator<VirtueApplicationItem>() {

			@Override
			public int compare(VirtueApplicationItem va1, VirtueApplicationItem va2) {

				Integer va1State = va1.getVirtue().getVirtueState().getValue();
				Integer va2State = va2.getVirtue().getVirtueState().getValue();

				int valComp = va1State.compareTo(va2State);

				if (valComp != 0) {
					return valComp;
				}

				return va1.getApplicationName().compareTo(va2.getApplicationName());
			}
		};

		this.sortVtByStatus = new Comparator<VirtueTileContainer>() {

			@Override
			public int compare(VirtueTileContainer va1, VirtueTileContainer va2) {

				Integer va1State = va1.getVirtue().getVirtueState().getValue();
				Integer va2State = va2.getVirtue().getVirtueState().getValue();

				int valComp = va1State.compareTo(va2State);

				if (valComp != 0) {
					return valComp;
				}

				return va1.getName().compareTo(va2.getName());
			}
		};

		this.sortVlByStatus = new Comparator<VirtueListContainer>() {

			@Override
			public int compare(VirtueListContainer va1, VirtueListContainer va2) {

				Integer va1State = va1.getVirtue().getVirtueState().getValue();
				Integer va2State = va2.getVirtue().getVirtueState().getValue();

				int valComp = va1State.compareTo(va2State);

				if (valComp != 0) {
					return valComp;
				}

				return va1.getName().compareTo(va2.getName());
			}
		};
	}

	public void setInitialViewPort() {
		String view = lastView.get("view", null);
		if (view == null) {
			lastView.put("view", "at");
			view = "at";
		}

		switch (view) {
		case "at":
			renderAppsTileView();
			break;
		case "al":
			renderAppsListView();
			break;
		case "fv":
			renderFavoritesView();
			break;
		case "vt":
			renderVirtueTileView();
			break;
		case "vl":
			renderVirtueListView();
			break;
		}
	}

	public JPanel getContainer() {
		return desktopContainer;
	}

	public void setupDialog() {
		JDialog dialog = new JDialog();

		String registeredSymbol = "\u00ae";
		String trademarkSymbol = "\u2122";
		String copyrightSymbol = "\u00a9";

		dialog.setIconImage(saviorIcon.getImage());

		JPanel container = new JPanel();
		container.setBackground(Color.WHITE);
		container.setLayout(new BorderLayout(0, 0));

		JLabel title = new JLabel("Savior VirtUE Desktop");
		title.setFont(new Font("Arial", Font.BOLD, 18));
		title.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 30));

		ImageIcon imageIcon = new ImageIcon(Sidebar.class.getResource("/images/saviorLogo.png"));
		Image image = imageIcon.getImage(); // transform it
		Image newimg = image.getScaledInstance(27, 30, java.awt.Image.SCALE_SMOOTH); // scale it the smooth way
		imageIcon = new ImageIcon(newimg); // transform it back
		title.setIcon(imageIcon);

		title.setHorizontalAlignment(SwingConstants.CENTER);

		container.add(title, BorderLayout.NORTH);

		JPanel footer = new JPanel();
		footer.setBackground(Color.WHITE);
		container.add(footer, BorderLayout.SOUTH);

		JLabel copyright = new JLabel(copyrightSymbol + " 2018-2019 Next Century Corporation. All rights reserved");
		footer.add(copyright);

		JScrollPane scrollPane = new JScrollPane();
		scrollPane.getVerticalScrollBar().setUnitIncrement(16);
		container.add(scrollPane, BorderLayout.CENTER);

		JPanel textContainer = new JPanel();
		textContainer.setBackground(Color.WHITE);
		textContainer.setLayout(new BoxLayout(textContainer, BoxLayout.Y_AXIS));

		JLabel disclaimerHeader = new JLabel("<html><center> Disclaimer Third Parties <br><br></center></html>",
				SwingConstants.CENTER);
		JLabel disclaimers = new JLabel(
				"<html><center> All product and company names are trademarks" + trademarkSymbol + " or <br> registered"
						+ registeredSymbol
						+ " trademarks of their respective holders. Use of <br> them does not imply any affiliation with or endorsement by them.<br><br>"
						+ "All specifications are subject to change without notice.<br><br>"
						+ "Chrome and Chromium are trademarks owned by Google LLC.<br><br>"
						+ "Firefox and the Firefox logos are trademarks of the <br> Mozilla Foundation.<br><br>"
						+ "LibreOffice and LibreOffice logos are trademarks of The <br> Document Foundation.<br><br>"
						+ "Microsoft, Microsoft Office, Microsoft Excel, Microsoft PowerPoint <br> and Microsoft Word are registered trademarks of Microsoft <br> Corporation in the United States and/or other countries.<br><br></center></html>",
				SwingConstants.CENTER);
		JLabel credits = new JLabel("<html><center> Software Team Credits: <br><br></center></html>",
				SwingConstants.CENTER);
		JLabel nextCentury = new JLabel("<html><center> Next Century Corporation<br><br></center></html>",
				SwingConstants.CENTER);

		JLabel twoSix = new JLabel("<html><center> Two Six Labs<br><br></center></html>", SwingConstants.CENTER);

		JLabel vt = new JLabel("<html><center> Virginia Tech<br><br></center></html>", SwingConstants.CENTER);

		disclaimerHeader.setFont(new Font("Tahoma", Font.BOLD, 15));
		credits.setFont(new Font("Tahoma", Font.BOLD, 15));
		nextCentury.setFont(new Font("Tahoma", Font.BOLD, 13));
		twoSix.setFont(new Font("Tahoma", Font.BOLD, 13));
		vt.setFont(new Font("Tahoma", Font.BOLD, 13));

		textContainer.add(disclaimerHeader);
		textContainer.add(disclaimers);
		textContainer.add(credits);
		textContainer.add(nextCentury);
		textContainer.add(twoSix);
		textContainer.add(vt);

		scrollPane.setViewportView(textContainer);

		dialog.addWindowListener(new WindowListener() {

			@Override
			public void windowActivated(WindowEvent arg0) {
				// do nothing
			}

			@Override
			public void windowClosed(WindowEvent arg0) {
				// do nothing
			}

			@Override
			public void windowClosing(WindowEvent arg0) {
				// do nothing
			}

			@Override
			public void windowDeactivated(WindowEvent arg0) {
				dialog.setVisible(false);
				dialog.dispose();
				desktopContainer.validate();
				desktopContainer.repaint();
			}

			@Override
			public void windowDeiconified(WindowEvent arg0) {
				// do nothing
			}

			@Override
			public void windowIconified(WindowEvent arg0) {
				// do nothing

			}

			@Override
			public void windowOpened(WindowEvent arg0) {
				// do nothing
			}

		});

		dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		dialog.add(container);
		dialog.setLocationRelativeTo(container);
		dialog.pack();
		dialog.setSize(new Dimension(415, 350));
		dialog.setLocationRelativeTo(frame);
		dialog.setVisible(true);
	}
}
