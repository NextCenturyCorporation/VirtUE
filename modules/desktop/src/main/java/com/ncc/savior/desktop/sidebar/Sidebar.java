package com.ncc.savior.desktop.sidebar;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

	public Sidebar(VirtueService virtueService) {
		this.virtueIdToVmi = new HashMap<String, VirtueMenuItem>();
		this.virtueService = virtueService;
	}

	public void start(Stage stage, List<DesktopVirtue> initialVirtues) throws Exception {
		stage.setTitle("Savior");

		VBox pane = new VBox();
		if (debug) {
			pane.setBorder(new Border(paneDebugStroke));
		}
		pane.setAlignment(Pos.TOP_CENTER);
		ObservableList<Node> children = pane.getChildren();
		children.add(getLabel());// , 1, 1);
		// pane.add(getMinimizeMaximize());
		children.add(getUserIcon());// , 1, 3);
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

	private Node initialVirtueList(Stage primaryStage, List<DesktopVirtue> initialVirtues) {
		virtuePane = new VBox();
		virtuePane.setPrefWidth(width);
		virtuePane.setAlignment(Pos.BOTTOM_CENTER);
		ObservableList<Node> children = virtuePane.getChildren();
		if (debug) {
			virtuePane.setBorder(new Border(calloutDebugStroke));
		}
		for (DesktopVirtue virtue : initialVirtues) {
			VirtueMenuItem vmi = new VirtueMenuItem(virtue, virtueService);
			children.add(vmi.getNode());
			virtueIdToVmi.put(virtue.getId(), vmi);
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

	@Override
	public void changeVirtue(DesktopVirtue virtue) {
		VirtueMenuItem vmi = virtueIdToVmi.get(virtue.getId());
		vmi.updateVirtue(virtue);
	}

	@Override
	public void addVirtue(DesktopVirtue virtue) {
		ObservableList<Node> children = virtuePane.getChildren();
		VirtueMenuItem vmi = new VirtueMenuItem(virtue, virtueService);
		virtueIdToVmi.put(virtue.getId(), vmi);
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				children.add(vmi.getNode());
			}
		});
	}

	@SuppressWarnings("unlikely-arg-type")
	@Override
	public void removeVirtue(DesktopVirtue virtue) {
		ObservableList<Node> children = virtuePane.getChildren();
		VirtueMenuItem vmi = virtueIdToVmi.remove(virtue.getId());
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				children.remove(vmi);
			}
		});
	}
}
