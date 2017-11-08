package com.ncc.savior.desktop.sidebar;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.geometry.Bounds;
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
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
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
import javafx.stage.Popup;
import javafx.stage.Stage;

public class SidebarApplication extends Application {
	public static void main(String[] args) {
		launch(args);
	}

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

	private Popup openedPopup;

	@Override
	public void start(Stage primaryStage) throws Exception {
		primaryStage.setTitle("Savior");

		VBox pane = new VBox();
		if (debug) {
			pane.setBorder(new Border(paneDebugStroke));
		}
		pane.setAlignment(Pos.TOP_CENTER);
		ObservableList<Node> children = pane.getChildren();
		children.add(getLabel());// , 1, 1);
		// pane.add(getMinimizeMaximize());
		children.add(getUserIcon());// , 1, 3);
		Node vlist = getVirtueList(primaryStage);
		children.add(vlist);// , 1, 4);
		// Region region = new Region();
		// region.setBorder(new Border(sectionDebugStroke));
		// children.add(region);// , 1, 5);
		children.add(getLogout());// , 1, 6);
		VBox.setVgrow(vlist, Priority.ALWAYS);
		Scene scene = new Scene(pane, width, height);
		Image icon = new Image("images/saviorLogo.png");
		primaryStage.getIcons().clear();
		primaryStage.getIcons().add(icon);
		primaryStage.setScene(scene);
		primaryStage.show();
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

	private Node getVirtueList(Stage primaryStage) {
		VBox pane = new VBox();
		pane.setPrefWidth(width);
		pane.setAlignment(Pos.BOTTOM_CENTER);
		List<VirtueDto> virtueList = getVirtues();
		ObservableList<Node> children = pane.getChildren();
		if (debug) {
			pane.setBorder(new Border(calloutDebugStroke));
		}
		for (VirtueDto virtue : virtueList) {
			addVirtue(children, virtue, primaryStage);
		}
		ScrollPane scroll = new ScrollPane(pane);
		scroll.setHbarPolicy(ScrollBarPolicy.NEVER);
		scroll.setVbarPolicy(ScrollBarPolicy.AS_NEEDED);
		scroll.prefHeight(height);

		return scroll;
	}

	private void addVirtue(ObservableList<Node> children, VirtueDto virtue, Stage primaryStage) {
		Image image = new Image("images/saviorLogo.png");
		ImageView view = new ImageView(image);
		view.setFitWidth(24);
		view.setFitHeight(24);
		Label label = new Label(virtue.getName());
		label.setPrefWidth(width);
		label.setAlignment(Pos.CENTER);

		HBox pane = new HBox();
		HBox.setHgrow(view, Priority.NEVER);
		HBox.setHgrow(label, Priority.ALWAYS);
		pane.setPadding(new Insets(5, 20, 5, 20));
		BorderStroke style = new BorderStroke(Color.GRAY, BorderStrokeStyle.SOLID, new CornerRadii(0),
				new BorderWidths(0, 0, 1, 0));
		pane.setBorder(new Border(style));
		pane.getChildren().add(view);
		pane.getChildren().add(label);
		Popup popup = new Popup();
		VBox box = new VBox();
		for (VirtueAppDto app : virtue.getApps()) {
			URI uri = app.getIconUri();
			Image appImage = null;
			if (uri != null) {
				try {
					appImage = new Image(uri.toURL().openStream());
				} catch (Exception e) {

				}
			}
			if (image == null) {
				appImage = new Image(app.getIconLocation());
			}
			Label appLabel;
			if (image == null) {
				appLabel = new Label(app.getName());
			} else {
				ImageView iv = new ImageView(appImage);
				iv.setFitWidth(24);
				iv.setFitHeight(24);
				appLabel = new Label(app.getName(), iv);
			}
			box.getChildren().add(appLabel);
			box.setBackground(new Background(new BackgroundFill(Color.ALICEBLUE, null, null)));
		}

		popup.getContent().add(box);
		pane.setOnMouseEntered(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				Bounds boundsInScreen = pane.localToScreen(pane.getBoundsInLocal());
				popup.setX(boundsInScreen.getMaxX() - 5);
				popup.setY(boundsInScreen.getMinY());
				if (openedPopup != popup) {
					if (openedPopup != null) {
						openedPopup.hide();
					}
					popup.show(primaryStage);
					openedPopup = popup;

				}
			}
		});
		pane.setOnMouseExited(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				// popup.hide();
			}
		});
		box.setOnMouseEntered(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent arg0) {
				if (openedPopup != popup) {
					openedPopup.hide();
					popup.show(primaryStage);
					openedPopup = popup;
				}
			}
		});
		box.setOnMouseExited(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent arg0) {
				popup.hide();
			}
		});
		children.add(pane);
		// pane.setAlignment(Pos.CENTER);
		// pane.setPrefWidth(width);
		// pane.setMinHeight(label.getHeight());
		if (debug) {
			label.setBorder(new Border(subSectionDebugStroke));
		}
	}

	private List<VirtueDto> getVirtues() {
		ArrayList<VirtueDto> virtue = new ArrayList<VirtueDto>();
		virtue.add(new VirtueDto("Web Browsers", new VirtueAppDto("Chrome", ""), new VirtueAppDto("Firefox", ""),
				new VirtueAppDto("Netscape", "")));

		virtue.add(new VirtueDto("Microsoft Office", new VirtueAppDto("Word", ""), new VirtueAppDto("Excel", ""),
				new VirtueAppDto("Powerpoint", "")));

		virtue.add(new VirtueDto("Drawing", new VirtueAppDto("Paint", ""), new VirtueAppDto("Gimp", "")));

		virtue.add(new VirtueDto("Web Browsers2", new VirtueAppDto("Chrome", ""), new VirtueAppDto("Firefox", ""),
				new VirtueAppDto("Netscape", "")));

		virtue.add(new VirtueDto("Web Browsers3", new VirtueAppDto("Chrome", ""), new VirtueAppDto("Firefox", ""),
				new VirtueAppDto("Netscape", "")));

		virtue.add(new VirtueDto("Web Browsers4", new VirtueAppDto("Chrome", ""), new VirtueAppDto("Firefox", ""),
				new VirtueAppDto("Netscape", "")));

		return virtue;
	}

	private Node getUserIcon() {
		Image image = new Image("images/defaultUserIcon.png");
		ImageView iv = new ImageView(image);

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

	// private Node getMinimizeMaximize() {
	// // TODO Auto-generated method stub
	// return null;
	// }

	private Node getLabel() {
		Image image = new Image("images/saviorLogo.png");
		ImageView view = new ImageView(image);
		view.setFitWidth(32);
		view.setFitHeight(32);
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
}
