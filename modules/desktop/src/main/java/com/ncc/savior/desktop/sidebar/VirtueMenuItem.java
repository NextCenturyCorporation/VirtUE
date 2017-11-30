package com.ncc.savior.desktop.sidebar;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URI;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ncc.savior.desktop.virtues.VirtueAppDto;
import com.ncc.savior.desktop.virtues.VirtueDto;
import com.ncc.savior.desktop.virtues.VirtueService;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.paint.Color;
import javafx.stage.WindowEvent;

/**
 * This represents a Virtue in the sidebar menu. It controls the sub menu when
 * the virtue is selected.
 *
 *
 */
public class VirtueMenuItem {
	@SuppressWarnings("unused")
	private static Logger logger = LoggerFactory.getLogger(VirtueMenuItem.class);
	private VirtueDto virtue;
	private boolean debug = false;
	private Node node;
	private ContextMenu contextMenu;
	private Label label;
	private BorderStroke subSectionDebugStroke = new BorderStroke(Color.YELLOW, BorderStrokeStyle.DOTTED,
			new CornerRadii(0), new BorderWidths(2));
	protected boolean showingMenu;
	private VirtueService virtueService;

	public VirtueMenuItem(VirtueDto virtue, VirtueService virtueService) {
		this.virtueService = virtueService;
		this.virtue = virtue;
		this.node = createNode();
		this.contextMenu = createContextMenu();
		addEventHandlers();
	}

	private Node createNode() {
		Image image = new Image("images/saviorLogo.png");
		ImageView view = new ImageView(image);
		view.setFitWidth(24);
		view.setFitHeight(24);
		label = new Label(virtue.getName());
		// label.setPrefWidth(width);
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
		return pane;
	}

	private ContextMenu createContextMenu() {
		ContextMenu menu = new ContextMenu();
		for (VirtueAppDto app : virtue.getApps()) {
			URI uri = app.getIconUri();
			Image appImage = null;
			if (uri != null) {
				try {
					appImage = new Image(uri.toURL().openStream());
				} catch (Exception e) {

				}
			}
			if (appImage == null) {
				try {
					appImage = new Image(app.getIconLocation());
				} catch (IllegalArgumentException e) {

				}
			}
			MenuItem menuItem;
			if (appImage == null) {
				menuItem = new MenuItem(app.getName());
			} else {
				ImageView iv = new ImageView(appImage);
				iv.setFitWidth(24);
				iv.setFitHeight(24);
				menuItem = new MenuItem(app.getName(), iv);
			}
			menuItem.setOnAction(new EventHandler<ActionEvent>() {
				@Override
				public void handle(ActionEvent event) {
					Thread thread = new Thread(new Runnable() {
						@Override
						public void run() {
							try {
								virtueService.connectAndStartApp(app);
							} catch (IOException e) {
								logger.error("Error starting " + app.getName(), e);
								Platform.runLater(new Runnable() {
									@Override
									public void run() {
										Alert alert = new Alert(AlertType.ERROR);
										alert.setTitle("Error starting " + app.getName());
										alert.setHeaderText(
												"Error starting '" + app.getName() + "'");

										String text = stacktraceToString(e);
										alert.setContentText(text);
										alert.showAndWait();
									}
								});
							}
						}

					});
					thread.setDaemon(true);
					thread.start();
				}
			});
			menu.getItems().add(menuItem);
		}
		return menu;
	}

	private String stacktraceToString(IOException e) {
		String text;
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		e.printStackTrace(pw);
		text = sw.toString(); // stack trace as a string
		return text;
	}

	private void addEventHandlers() {

		node.setOnMouseClicked(new EventHandler<MouseEvent>() {

			@Override
			public void handle(MouseEvent event) {
				if (showingMenu) {
					contextMenu.hide();
				} else {
					contextMenu.show(node, Side.RIGHT, -3, 0);
				}
			}
		});
		contextMenu.setOnShown(new EventHandler<WindowEvent>() {
			@Override
			public void handle(WindowEvent event) {
				showingMenu = true;
			}
		});
		contextMenu.setOnHidden(new EventHandler<WindowEvent>() {
			@Override
			public void handle(WindowEvent event) {
				showingMenu = false;
			}
		});
		// contextMenu.seton
		// pane.setAlignment(Pos.CENTER);
		// pane.setPrefWidth(width);
		// pane.setMinHeight(label.getHeight());
		if (debug) {
			label.setBorder(new Border(subSectionDebugStroke));
		}
	}

	public Node getNode() {
		return node;
	}
}
