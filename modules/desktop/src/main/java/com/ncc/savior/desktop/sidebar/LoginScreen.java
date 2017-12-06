package com.ncc.savior.desktop.sidebar;

import java.util.Set;
import java.util.TreeSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ncc.savior.desktop.authorization.AuthorizationService;
import com.ncc.savior.desktop.authorization.DesktopUser;
import com.sun.jna.platform.win32.Win32Exception;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class LoginScreen extends Stage {
	private static Logger logger = LoggerFactory.getLogger(LoginScreen.class);

	private AuthorizationService authService;
	private final boolean forceSuccessfulLoginOrQuit;
	private Set<ILoginEventListener> loginListeners;

	public LoginScreen(AuthorizationService authService, boolean forceSuccessfulLoginOrQuit) {
		this.authService = authService;
		this.forceSuccessfulLoginOrQuit = forceSuccessfulLoginOrQuit;
		loginListeners = new TreeSet<ILoginEventListener>();
	}

	public void start(Stage owner) {
		initOwner(owner);
		initModality(Modality.APPLICATION_MODAL);
		setTitle("title");
		Group root = new Group();
		Scene scene = new Scene(root, 250, 150, Color.WHITE);
		setScene(scene);

		GridPane gridpane = new GridPane();
		gridpane.setPadding(new Insets(5));
		gridpane.setHgap(5);
		gridpane.setVgap(5);

		Label domainLbl = new Label("Domain: ");
		gridpane.add(domainLbl, 0, 1);

		Label userNameLbl = new Label("User Name: ");
		gridpane.add(userNameLbl, 0, 2);

		Label passwordLbl = new Label("Password: ");
		gridpane.add(passwordLbl, 0, 3);
		final TextField domainFld = new TextField("");
		gridpane.add(domainFld, 1, 1);
		final TextField userNameFld = new TextField("");
		gridpane.add(userNameFld, 1, 2);

		final PasswordField passwordFld = new PasswordField();
		passwordFld.setText("password");
		gridpane.add(passwordFld, 1, 3);

		Button login = new Button("Login");
		login.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
				try {
					String domain = domainFld.getText();
					String username = userNameFld.getText();
					DesktopUser user = authService.login(domain, username, passwordFld.getText());
					logger.debug("Login to domain=" + domain + " user=" + username + " was successful!");
					triggerLoginSuccessListener(user);
					close();
				} catch (Win32Exception t) {
					logger.error("failure", t);
				}

			}
		});
		Button cancelClose = new Button(forceSuccessfulLoginOrQuit ? "Close" : "Cancel");
		cancelClose.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				if (forceSuccessfulLoginOrQuit) {
					System.exit(0);
				} else {
					close();
				}
			}
		});
		gridpane.add(login, 0, 4);
		GridPane.setHalignment(login, HPos.RIGHT);
		root.getChildren().add(gridpane);
		this.show();
	}

	public void addLoginEventListener(ILoginEventListener listener) {
		loginListeners.add(listener);
	}

	public void removeLoginEventListener(ILoginEventListener listener) {
		loginListeners.remove(listener);
	}

	protected void triggerLoginSuccessListener(DesktopUser user) {
		for (ILoginEventListener listener : loginListeners) {
			listener.onLoginSuccess(user);
		}
	}

	protected void triggerLoginFailureListener(String username, String domain, RuntimeException e) {
		for (ILoginEventListener listener : loginListeners) {
			listener.onLoginFailure(username, domain, e);
		}
	}

	public static interface ILoginEventListener {
		public void onLoginSuccess(DesktopUser user);
		public void onLoginFailure(String username, String domain, RuntimeException e);
	}

}
