package com.ncc.savior.desktop.sidebar;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ncc.savior.desktop.authorization.AuthorizationService;
import com.ncc.savior.desktop.authorization.DesktopUser;
import com.ncc.savior.desktop.sidebar.LoginScreen.ILoginEventListener;
import com.ncc.savior.desktop.sidebar.SidebarController.VirtueChangeHandler;
import com.ncc.savior.desktop.virtues.VirtueService;
import com.ncc.savior.virtueadmin.model.desktop.DesktopVirtue;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ScrollPane.ScrollBarPolicy;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

/**
 * Class to start the utility sidebar of the Savior application
 *
 *
 */
public class Sidebar implements VirtueChangeHandler {
	private static final Logger logger = LoggerFactory.getLogger(Sidebar.class);
	private static final int ICON_SIZE = 32;
	private boolean debug = false;
	private BorderStroke paneDebugStroke = new BorderStroke(Color.RED, BorderStrokeStyle.DOTTED, new CornerRadii(0),
			new BorderWidths(2));
	private BorderStroke sectionDebugStroke = new BorderStroke(Color.ORANGE, BorderStrokeStyle.DOTTED,
			new CornerRadii(0), new BorderWidths(2));
	private BorderStroke subSectionDebugStroke = new BorderStroke(Color.YELLOW, BorderStrokeStyle.DOTTED,
			new CornerRadii(0), new BorderWidths(2));
	private BorderStroke calloutDebugStroke = new BorderStroke(Color.GREEN, BorderStrokeStyle.DOTTED,
			new CornerRadii(0), new BorderWidths(2));

	private int width = 300;
	private int height = 800;
	private VirtueService virtueService;
	private VBox virtuePane;
	private Map<String, VirtueMenuItem> virtueIdToVmi;
	private AuthorizationService authService;
	private Label userLabel;
	private Stage stage;
	private Image statusImage;

	public Sidebar(VirtueService virtueService, AuthorizationService authService) {
		this.authService = authService;
		this.virtueIdToVmi = new HashMap<String, VirtueMenuItem>();
		this.virtueService = virtueService;
		this.statusImage = new Image("images/loading.gif");
	}

	public void start(Stage stage, List<DesktopVirtue> initialVirtues) throws Exception {
		stage.setTitle("Savior");
		this.stage = stage;

		VBox pane = new VBox();
		if (debug) {
			pane.setBorder(new Border(paneDebugStroke));
		}
		pane.setAlignment(Pos.TOP_CENTER);
		ObservableList<Node> children = pane.getChildren();
		children.add(getLabel());// , 1, 1);
		// pane.add(getMinimizeMaximize());
		children.add(getUserIcon());// , 1, 3);
		children.add(getUserNameLabel());
		Node vlist = initialVirtueList(stage, initialVirtues);
		children.add(vlist);// , 1, 4);
		// Region region = new Region();
		// region.setBorder(new Border(sectionDebugStroke));
		// children.add(region);// , 1, 5);
		children.add(getLogout());// , 1, 6);
		VBox.setVgrow(vlist, Priority.ALWAYS);
		Scene scene = new Scene(pane, width, height);
		Image icon = new Image("images/saviorLogo.png");
		stage.getIcons().clear();
		stage.getIcons().add(icon);
		stage.setScene(scene);
		stage.show();
		DesktopUser user = authService.getUser();
		String reqDomain = authService.getRequiredDomain();
		if (user == null || (reqDomain != null && !reqDomain.equals(user.getDomain()))) {
			LoginScreen login = new LoginScreen(authService, true);
			login.addLoginEventListener(new ILoginEventListener() {
				@Override
				public void onLoginSuccess(DesktopUser user) {
					Platform.runLater(new Runnable() {
						@Override
						public void run() {
							userLabel.setText(user.getUsername());
						}
					});
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
			login.start(stage);
		}
	}

	private Node getLogout() {
		Image image = new Image("images/logout.png");
		ImageView view = new ImageView(image);
		view.setFitWidth(32);
		view.setFitHeight(32);
		Button button = new Button("Logout", view);
		button.setPrefWidth(width);
		button.setOnMouseClicked(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				// do cleanup stuff.
				authService.logout();
				Platform.exit();
			}
		});
		HBox.setHgrow(button, Priority.ALWAYS);
		GridPane pane = new GridPane();
		pane.getChildren().add(button);
		pane.setAlignment(Pos.CENTER);
		pane.setMaxHeight(height);
		pane.setPrefWidth(width);
		if (debug) {
			button.setBorder(new Border(subSectionDebugStroke));
			pane.setBorder(new Border(sectionDebugStroke));
		}
		return pane;
	}

	private Node initialVirtueList(Stage primaryStage, List<DesktopVirtue> initialVirtues) {
		virtuePane = new VBox();
		virtuePane.setPrefWidth(width);
		virtuePane.setAlignment(Pos.BOTTOM_CENTER);
		ObservableList<Node> children = virtuePane.getChildren();
		if (debug) {
			virtuePane.setBorder(new Border(calloutDebugStroke));
		}
		for (DesktopVirtue virtue : initialVirtues) {
			VirtueMenuItem vmi = new VirtueMenuItem(virtue, virtueService, statusImage, width);
			children.add(vmi.getNode());
			String id = virtue.getId() == null ? virtue.getTemplateId() : virtue.getId();
			virtueIdToVmi.put(id, vmi);
		}
		ScrollPane scroll = new ScrollPane(virtuePane);
		scroll.setHbarPolicy(ScrollBarPolicy.NEVER);
		scroll.setVbarPolicy(ScrollBarPolicy.AS_NEEDED);
		scroll.prefHeight(height);

		return scroll;
	}

	private Node getUserIcon() {
		Image image = new Image("images/defaultUserIcon.png");
		ImageView iv = new ImageView(image);
		iv.setOnMouseClicked(new EventHandler<MouseEvent>() {

			@Override
			public void handle(MouseEvent event) {
				LoginScreen loginScreen = new LoginScreen(authService, false);
				loginScreen.addLoginEventListener(new ILoginEventListener() {
					@Override
					public void onLoginSuccess(DesktopUser user) {
						Platform.runLater(new Runnable() {
							@Override
							public void run() {
								virtuePane.getChildren().clear();
								virtueIdToVmi.clear();
								userLabel.setText(user.getUsername());
							}
						});
					}

					@Override
					public void onLoginFailure(String username, String domain, RuntimeException e) {
						// do nothing
					}

					@Override
					public void onCancel() {
						// do nothing
					}
				});
				loginScreen.start(stage);
			}
		});

		int size = 96;
		int paddingSize = 5;
		iv.setFitWidth(size);
		iv.setFitHeight(size);
		GridPane pane = new GridPane();
		pane.getChildren().add(iv);
		pane.setPrefWidth(width);
		pane.setMinHeight(size + 2 * paddingSize);
		pane.setAlignment(Pos.BOTTOM_CENTER);
		pane.setPadding(new Insets(paddingSize));
		if (debug) {
			pane.setBorder(new Border(sectionDebugStroke));
		}
		return pane;
	}

	private Node getLabel() {
		Image image = new Image("images/saviorLogo.png");
		ImageView view = new ImageView(image);
		view.setFitWidth(ICON_SIZE);
		view.setFitHeight(ICON_SIZE);
		Label label = new Label("Savior VirtUE Desktop", view);
		label.setPrefWidth(width);
		label.setAlignment(Pos.CENTER);
		GridPane pane = new GridPane();
		pane.getChildren().add(label);
		pane.setAlignment(Pos.CENTER);
		pane.setPrefWidth(width);
		pane.setMinHeight(label.getHeight());
		if (debug) {
			label.setBorder(new Border(subSectionDebugStroke));
			pane.setBorder(new Border(sectionDebugStroke));
		}
		return label;
	}

	private Node getUserNameLabel() {
		if (userLabel == null) {
			DesktopUser user = authService.getUser();
			String username = user == null ? "" : user.getUsername();
			userLabel = new Label(username);
		}
		return userLabel;
	}

	@Override
	public void changeVirtue(DesktopVirtue virtue) {
		VirtueMenuItem vmi = virtueIdToVmi.get(virtue.getId());
		vmi.updateVirtue(virtue);
	}

	@Override
	public void addVirtue(DesktopVirtue virtue) {
		ObservableList<Node> children = virtuePane.getChildren();
		VirtueMenuItem vmi = new VirtueMenuItem(virtue, virtueService, statusImage, width);
		String id = virtue.getId() == null ? virtue.getTemplateId() : virtue.getId();
		virtueIdToVmi.put(id, vmi);
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				children.add(vmi.getNode());
			}
		});
	}

	@Override
	public void removeVirtue(DesktopVirtue virtue) {
		ObservableList<Node> children = virtuePane.getChildren();
		VirtueMenuItem vmi = virtueIdToVmi.remove(virtue.getId());
		if (vmi == null) {
			vmi = virtueIdToVmi.remove(virtue.getTemplateId());
		}
		if (vmi != null) {
			VirtueMenuItem finalVmi = vmi;
			Platform.runLater(new Runnable() {
				@Override
				public void run() {
					children.remove(finalVmi.getNode());
				}
			});
		}
	}
}
