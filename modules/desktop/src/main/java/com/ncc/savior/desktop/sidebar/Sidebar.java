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
import javax.swing.DefaultListCellRenderer;
import javax.swing.ImageIcon;
import javax.swing.JComboBox;
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

import com.ncc.savior.desktop.alerting.UserAlertingServiceHolder;
import com.ncc.savior.desktop.authorization.AuthorizationService;
import com.ncc.savior.desktop.authorization.DesktopUser;
import com.ncc.savior.desktop.sidebar.AbstractVirtueContainer.IUpdateListener;
import com.ncc.savior.desktop.sidebar.AbstractVirtueView.IRemoveVirtueListener;
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

	private ImageIcon inactiveTileIcon = (new ImageIcon(Sidebar.class.getResource("/images/tile-inactive.png")));
	private ImageIcon activeTileIcon = (new ImageIcon(Sidebar.class.getResource("/images/tile-active.png")));

	private ImageIcon inactiveListIcon = (new ImageIcon(Sidebar.class.getResource("/images/list-inactive.png")));
	private ImageIcon activeListIcon = (new ImageIcon(Sidebar.class.getResource("/images/list-active.png")));

	private static ImageIcon saviorIcon = new ImageIcon(Sidebar.class.getResource("/images/saviorLogo.png"));

	private static Image defaultImage = saviorIcon.getImage();
	private static Image saviorTile = defaultImage.getScaledInstance(47, 50, java.awt.Image.SCALE_SMOOTH);
	private static Image saviorList = defaultImage.getScaledInstance(30, 30, java.awt.Image.SCALE_SMOOTH);

	private static ImageIcon loadingIcon = new ImageIcon(Sidebar.class.getResource("/images/loading.gif"));

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
	private LoginPage loginPageView;

	private GhostText ghostText;

	private JTextField textField;
	private JLabel favoritesLabel;
	private JLabel tileLabel;
	private JLabel listLabel;

	private JLabel searchLabel;
	private JLabel about;
	private JLabel alert;

	private JPanel virtues;
	private JPanel applications;

	private JPanel listView;
	private JPanel favoritesView;
	private JPanel tileView;

	private JPanel virtuesSelected;
	private JPanel applicationsSelected;

	private JPanel bottomBorder;

	private JComboBox<String> dropDownBox;

	private JPanel desktopContainer;
	private boolean applicationsOpen = true;

	private DesktopView desktopView;

	private boolean searchMode = false;

	private JScrollPane scrollPane;
	private AppsTile appsTileView;
	private AppsList appsListView;
	private VirtueTile virtueTileView;
	private VirtueList virtueListView;
	private FavoritesView favoritesTileView;

	private Preferences favorites;
	private Preferences lastView;
	private Preferences lastSort;

	public static boolean askAgain = true;
	private boolean loading = true;
	private boolean empty = false;

	private Comparator<VirtueApplicationItem> sortAppsByStatus;
	private Comparator<VirtueTileContainer> sortVtByStatus;
	private Comparator<VirtueListContainer> sortVlByStatus;

	private JPanel loadingContainer;

	private AboutDialog aboutDialog;

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
		this.aboutDialog = new AboutDialog();
		setupComparators();
		setupLoadingGif();


		AbstractVirtueView.addRemoveVirtueListener(new IRemoveVirtueListener() {

			@Override
			public void onRemove() {
				sortWithKeyword();
			}

		});

		AbstractAppsView.addRemoveVirtueListener(new IRemoveVirtueListener() {

			@Override
			public void onRemove() {
				sortWithKeyword();
			}

		});

		AbstractVirtueContainer.addUpdateListener(new IUpdateListener() {

			@Override
			public void onUpdate() {
				sortWithKeyword();
			}

		});
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
	}

	public void startLogin() throws IOException {
		this.loginPageView = new LoginPage(authService);
		frame.getContentPane().removeAll();
		frame.getContentPane().validate();
		frame.getContentPane().repaint();
		this.frame.getContentPane().add(loginPageView.getContainer());
		frame.getContentPane().validate();
		frame.getContentPane().repaint();
		this.frame.setVisible(true);
		initiateLoginScreen(loginPageView);
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
		});
	}

	private void onLogin(DesktopUser user) throws IOException {
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
		if (loading) {
			scrollPane.setViewportView(loadingContainer);
		}
		if (empty) {
			renderEmpty();
		}

		UserAlertingServiceHolder.resetHistoryManager();
		frame.setVisible(true);
	}

	@Override
	public void changeVirtue(DesktopVirtue virtue) {
		VirtueTileContainer vtc = virtueIdToVtc.get(virtue.getTemplateId());
		VirtueListContainer vlc = virtueIdToVlc.get(virtue.getTemplateId());

		vtc.updateVirtue(virtue);
		vlc.updateVirtue(virtue);

		for (ApplicationDefinition ad : virtue.getApps().values()) {
			appsListView.updateApp(ad, virtue);
			appsTileView.updateApp(ad, virtue);
			favoritesTileView.updateApp(ad, virtue);
			virtueListView.updateApp(ad, virtue);
			virtueTileView.updateApp(ad, virtue);
		}
	}

	// ***Updating Virtues***
	@Override
	public void addVirtues(List<DesktopVirtue> virtues) throws IOException, InterruptedException, ExecutionException {
		if (loading || empty) {
			loading = false;
			empty = false;
			setInitialViewPort();
		}

		for (DesktopVirtue virtue : virtues) {
			Color headerColor = getNextColor();
			VirtueTileContainer vtc = new VirtueTileContainer(virtue, virtueService, headerColor, getNextColor(), scrollPane,
					textField, ghostText);
			VirtueListContainer vlc = new VirtueListContainer(virtue, virtueService, headerColor, scrollPane, textField,
					ghostText);

			virtueIdToVtc.put(virtue.getTemplateId(), vtc);
			virtueIdToVlc.put(virtue.getTemplateId(), vlc);

			SwingUtilities.invokeLater(new Runnable() {

				@Override
				public void run() {
					virtueTileView.addVirtueToRow(virtue, vtc, vtc.getRow());
					virtueListView.addVirtueToRow(virtue, vlc, vlc.getRow());
				}

			});

			for (ApplicationDefinition ad : virtue.getApps().values()) {

				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						try {
							boolean isFavorited = favorites.getBoolean(ad.getId() + virtue.getTemplateId(), false);
							ApplicationDom dom = new ApplicationDom(ad, isFavorited);

							VirtueApplicationItem appsTileVa = new VirtueApplicationItem(ad, virtueService, scrollPane, vtc,
									virtue, favoritesTileView, dom.getChangeListener(), saviorTile, isFavorited, frame, textField, dropDownBox,
									sortAppsByStatus, ghostText, headerColor, DesktopView.APPS_TILE);
							appsTileVa.tileSetup();
							appsTileVa.registerListener(dom.getChangeListener());
							appsTileView.addApplication(ad, appsTileVa);

							VirtueApplicationItem virtueTileVa = new VirtueApplicationItem(ad, virtueService, scrollPane, vtc,
									virtue, favoritesTileView, dom.getChangeListener(), saviorTile, isFavorited, frame, textField, dropDownBox,
									sortAppsByStatus, ghostText, headerColor, DesktopView.VIRTUE_TILE);
							virtueTileVa.tileSetup();
							virtueTileVa.registerListener(dom.getChangeListener());

							VirtueApplicationItem virtueListVa = new VirtueApplicationItem(ad, virtueService, scrollPane, vtc,
									virtue, favoritesTileView, dom.getChangeListener(), saviorList, isFavorited, frame, textField, dropDownBox,
									sortAppsByStatus, ghostText, headerColor, DesktopView.VIRTUE_LIST);
							virtueListVa.listSetup();
							virtueListVa.registerListener(dom.getChangeListener());

							VirtueApplicationItem appsListVa = new VirtueApplicationItem(ad, virtueService, scrollPane, vtc, virtue,
									favoritesTileView, dom.getChangeListener(), saviorList, isFavorited, frame, textField, dropDownBox,
									sortAppsByStatus, ghostText, headerColor, DesktopView.APPS_LIST);
							appsListVa.listSetup();
							appsListVa.registerListener(dom.getChangeListener());

							appsListView.addApplication(ad, appsListVa);
							vtc.addApplication(ad, virtueTileVa);
							vlc.addApplication(ad, virtueListVa);

							Consumer<Image> consumer = i -> {
								appsTileVa.setTileImage(i);
								virtueTileVa.setTileImage(i);
								virtueListVa.setListImage(i);
								appsListVa.setListImage(i);
								favoritesTileView.setTileImage(ad, virtue, i);
							};

							iconService.getImage(ad.getIconKey(), consumer);

							dom.addListener(appsTileVa.getChangeListener());
							dom.addListener(appsListVa.getChangeListener());
							dom.addListener(virtueTileVa.getChangeListener());
							dom.addListener(virtueListVa.getChangeListener());

							if (isFavorited) {
								String selected = (String) dropDownBox.getSelectedItem();
								VirtueApplicationItem favoritedVa;
								switch (selected) {
								case "Alphabetical":
									favoritedVa = new VirtueApplicationItem(ad, virtueService, scrollPane, vtc, virtue, favoritesTileView,
											dom.getChangeListener(), saviorTile, true, frame, textField, dropDownBox, null,
											ghostText, headerColor, DesktopView.APPS_TILE);
									favoritedVa.tileSetup();
									favoritesTileView.addFavorite(ad, virtue, favoritedVa, textField, null, ghostText);
									break;
								case "Status":
									favoritedVa = new VirtueApplicationItem(ad, virtueService, scrollPane, vtc, virtue, favoritesTileView,
											dom.getChangeListener(), saviorTile, true, frame, textField, dropDownBox, null,
											ghostText, headerColor, DesktopView.APPS_TILE);
									favoritedVa.tileSetup();
									favoritesTileView.addFavorite(ad, virtue, favoritedVa, textField, sortAppsByStatus, ghostText);
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

		scrollPane.getViewport().validate();
	}

	@Override
	public void addNoVirtues() {
		if (loading) {
			loading = false;
			empty = true;
			System.out.println("zoowee mama");
			renderEmpty();
		}
	}

	public void renderEmpty() {
		JPanel emptyPanel = new JPanel();
		emptyPanel.setLayout(new BorderLayout());
		JLabel empty = new JLabel("No Virtues!");
		empty.setHorizontalAlignment(SwingConstants.CENTER);
		emptyPanel.add(empty);

		scrollPane.setViewportView(emptyPanel);
	}

	@Override
	public void removeVirtue(DesktopVirtue virtue) {
		VirtueTileContainer vtc = virtueIdToVtc.remove(virtue.getTemplateId());
		virtueIdToVlc.remove(virtue.getTemplateId());

		if (vtc != null) {
			appsTileView.removeVirtue(virtue);
			appsListView.removeVirtue(virtue);
			favoritesTileView.removeVirtue(virtue);
			virtueTileView.removeVirtue(virtue);
			virtueListView.removeVirtue(virtue);
		}
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
		this.scrollPane = new JScrollPane();
		this.appsTileView = new AppsTile(virtueService, scrollPane);
		this.appsListView = new AppsList(virtueService, scrollPane);
		this.virtueTileView = new VirtueTile(scrollPane);
		this.virtueListView = new VirtueList(scrollPane);
		this.favoritesTileView = new FavoritesView(virtueService, scrollPane, favorites);
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

		ImageIcon alertIcon = new ImageIcon(Sidebar.class.getResource("/images/alert.png"));
		Image alertImage = alertIcon.getImage();
		Image newAlertImg = alertImage.getScaledInstance(20, 20, java.awt.Image.SCALE_SMOOTH);
		alertIcon = new ImageIcon(newAlertImg);
		this.alert = new JLabel();
		alert.setHorizontalAlignment(SwingConstants.RIGHT);
		alert.setIcon(alertIcon);
		alert.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 10));
		JPanel alertContainer = new JPanel(new BorderLayout());
		alertContainer.add(alert, BorderLayout.EAST);
		alertContainer.setBackground(Color.DARK_GRAY);
		topBorder.add(alertContainer, BorderLayout.CENTER);

		this.bottomBorder = new JPanel();
		FlowLayout flowLayout_1 = (FlowLayout) bottomBorder.getLayout();
		flowLayout_1.setVgap(0);
		bottomBorder.setBorder(BorderFactory.createEmptyBorder(4, 0, 4, 0));
		bottomBorder.setBackground(Color.DARK_GRAY);
		desktopContainer.add(bottomBorder, BorderLayout.SOUTH);

		JLabel logoutLabel = new JLabel();

		ImageIcon imageIcon = new ImageIcon(Sidebar.class.getResource("/images/logout.png"));
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
		this.dropDownBox = new JComboBox<String>(sortingOptions);
		dropDownBox.setSelectedItem(lastSort.get("sort", "Alphabetical"));
		dropDownBox.setBorder(BorderFactory.createEmptyBorder(7, 0, 0, 0));
		dropDownBox.setBackground(new Color(248, 248, 255));
		Color bgColor = dropDownBox.getBackground();
		dropDownBox.setRenderer(new DefaultListCellRenderer() {
			@Override
			public void paint(Graphics g) {
				setBackground(bgColor);
				super.paint(g);
			}
		});

		dropDownBox.setVisible(true);
		sortBy.add(sortByLabel);
		sortBy.add(dropDownBox);

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

		scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		scrollPane.setSize(300, 800);
		scrollPane.setPreferredSize(new Dimension(0, 800));
		scrollPane.getVerticalScrollBar().setUnitIncrement(16);
		c.fill = GridBagConstraints.BOTH;
		c.ipady = 0;
		c.weighty = 1.0; // request any extra vertical space
		c.anchor = GridBagConstraints.PAGE_END; // bottom of space
		c.gridx = 0;
		c.gridwidth = 3; // 3 columns wide
		c.gridy = 2; // third row
		center.add(scrollPane, c);

		scrollPane.getViewport().revalidate();
		scrollPane.validate();
		scrollPane.repaint();

		frame.pack();

		addEventListeners();
	}

	public void sortWithKeyword() {
		String keyword;
		if (ghostText.getIsVisible()) {
			keyword = "";
		} else {
			keyword = textField.getText();
		}
		sortByOption(keyword);
	}

	public void sortByOption(String keyword) {
		String selected = (String) dropDownBox.getSelectedItem();
		switch (selected) {
		case "Alphabetical":
			appsListView.search(keyword, null,
					va -> va.getApplicationName().toLowerCase().contains(keyword.toLowerCase()));
			appsTileView.search(keyword, null,
					va -> va.getApplicationName().toLowerCase().contains(keyword.toLowerCase()));
			favoritesTileView.search(keyword, null,
					va -> va.getApplicationName().toLowerCase().contains(keyword.toLowerCase()));
			virtueTileView.search(keyword, null, null);
			virtueListView.search(keyword, null, null);
			break;
		case "Status":
			appsListView.search(keyword, sortAppsByStatus,
					va -> va.getApplicationName().toLowerCase().contains(keyword.toLowerCase()));
			appsTileView.search(keyword, sortAppsByStatus,
					va -> va.getApplicationName().toLowerCase().contains(keyword.toLowerCase()));
			favoritesTileView.search(keyword, sortAppsByStatus,
					va -> va.getApplicationName().toLowerCase().contains(keyword.toLowerCase()));
			virtueTileView.search(keyword, sortVtByStatus, null);
			virtueListView.search(keyword, sortVlByStatus, null);
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
		if (!loading && !empty) {
			scrollPane.setViewportView(favoritesTileView.getContainer());
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
		if (!loading && !empty) {
			scrollPane.setViewportView(appsListView.getContainer());
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
		if (!loading && !empty) {
			scrollPane.setViewportView(appsTileView.getContainer());
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
		if (!loading && !empty) {
			scrollPane.setViewportView(virtueTileView.getContainer());
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
		if (!loading && !empty) {
			scrollPane.setViewportView(virtueListView.getContainer());
		}
	}

	public void resetViews() {
		searchMode = false;
		searchLabel.setIcon(searchIcon);
		textField.setText("");
		String selected = (String) dropDownBox.getSelectedItem();
		switch (selected) {
		case "Alphabetical":
			appsListView.search(null, null, null);
			appsTileView.search(null, null, null);
			favoritesTileView.search(null, null, null);
			virtueTileView.search(null, null, null);
			virtueListView.search(null, null, null);
			break;
		case "Status":
			appsListView.search(null, sortAppsByStatus, null);
			appsTileView.search(null, sortAppsByStatus, null);
			favoritesTileView.search(null, sortAppsByStatus, null);
			virtueTileView.search(null, sortVtByStatus, null);
			virtueListView.search(null, sortVlByStatus, null);
			break;
		}
		scrollPane.setViewportView(scrollPane.getViewport().getView());
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
				sortWithKeyword();
				scrollPane.setViewportView(scrollPane.getViewport().getView());
				searchLabel.setIcon(closeIcon);
			}

			@Override
			public void changedUpdate(DocumentEvent e) {
			}

			@Override
			public void removeUpdate(DocumentEvent e) {
				searchMode = true;
				String keyword = textField.getText();
				sortWithKeyword();
				scrollPane.setViewportView(scrollPane.getViewport().getView());
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
						sortWithKeyword();
						scrollPane.setViewportView(scrollPane.getViewport().getView());
						searchLabel.setIcon(closeIcon);
					}
				}
			}
		});

		dropDownBox.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				String selected = (String) dropDownBox.getSelectedItem();
				lastSort.put("sort", selected);
				sortWithKeyword();
			}

		});

		about.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent event) {
				aboutDialog.show(frame);
			}
		});

		alert.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent event) {
				try {
					AlertHistoryReader.displayAlerts(frame);
				} catch (IOException e) {
					logger.error("error displaying alerts", e);
				}
			}
		});
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

	private Color getNextColor() {
		if (!colorItr.hasNext()) {
			colorItr = colorList.iterator();
		}
		return colorItr.next();
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

	private void setupLoadingGif() {
		this.loadingContainer = new JPanel();
		loadingContainer.setLayout(new BorderLayout());
		JLabel gifLabel = new JLabel();
		gifLabel.setIcon(loadingIcon);
		gifLabel.setVerticalAlignment(SwingConstants.CENTER);
		gifLabel.setHorizontalAlignment(SwingConstants.CENTER);
		loadingContainer.add(gifLabel, BorderLayout.CENTER);
	}
}
