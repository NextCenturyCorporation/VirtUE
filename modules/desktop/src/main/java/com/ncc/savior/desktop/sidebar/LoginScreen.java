package com.ncc.savior.desktop.sidebar;

import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ncc.savior.desktop.authorization.AuthorizationService;
import com.ncc.savior.desktop.authorization.DesktopUser;

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
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class LoginScreen extends Stage {
	private static Logger logger = LoggerFactory.getLogger(LoginScreen.class);

	private AuthorizationService authService;
	private final boolean forceSuccessfulLoginOrQuit;
	private Set<ILoginEventListener> loginListeners;

	private Label warningLbl;

	public LoginScreen(AuthorizationService authService, boolean forceSuccessfulLoginOrQuit) {
		super(StageStyle.UTILITY);
		this.authService = authService;
		this.forceSuccessfulLoginOrQuit = forceSuccessfulLoginOrQuit;
		loginListeners = new HashSet<ILoginEventListener>();
		this.setResizable(false);
	}

	public void start(Stage owner) {
		initOwner(owner);
		initModality(Modality.APPLICATION_MODAL);
		setTitle("Login");
		Group root = new Group();
		Scene scene = new Scene(root, 250, 180);
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
		passwordFld.setText("");
		gridpane.add(passwordFld, 1, 3);
		Button login = new Button("Login");
		EventHandler<ActionEvent> attemptLogin = new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
				doLogin(domainFld, userNameFld, passwordFld);
			}
		};
		login.setOnAction(attemptLogin);
		EventHandler<KeyEvent> textFieldLoginEventHandler = new EventHandler<KeyEvent>() {
			@Override
			public void handle(KeyEvent event) {
				if (KeyCode.ENTER.equals(event.getCode())) {
					doLogin(domainFld, userNameFld, passwordFld);
				}
			}
		};
		passwordFld.setOnKeyReleased(textFieldLoginEventHandler);
		userNameFld.setOnKeyReleased(textFieldLoginEventHandler);
		domainFld.setOnKeyReleased(textFieldLoginEventHandler);
		Button cancelClose = new Button(forceSuccessfulLoginOrQuit ? "Close" : "Cancel");
		cancelClose.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				if (forceSuccessfulLoginOrQuit) {
					System.exit(0);
				} else {
					onCancel();
				}
			}
		});
		this.setOnCloseRequest(e -> {
			if (forceSuccessfulLoginOrQuit) {
				e.consume();
			} else {
				onCancel();
			}
		});
		gridpane.add(login, 0, 4);
		gridpane.add(cancelClose, 1, 4);
		warningLbl = new Label("");
		GridPane.setHalignment(login, HPos.RIGHT);
		warningLbl.setMaxWidth(scene.getWidth());
		warningLbl.setWrapText(true);

		VBox base = new VBox();
		base.getChildren().add(gridpane);
		base.getChildren().add(warningLbl);
		root.getChildren().add(base);
		this.show();
		if (authService.getRequiredDomain() != null) {
			domainFld.setText(authService.getRequiredDomain());
			domainFld.setEditable(false);
			userNameFld.requestFocus();
		}
	}

	private void onCancel() {
		triggerLoginCancelListener();
		close();
	}

	private void doLogin(final TextField domainFld, final TextField userNameFld, final PasswordField passwordFld) {
		String domain = domainFld.getText();
		String username = userNameFld.getText();
		try {
			DesktopUser user = authService.login(domain, username, passwordFld.getText());
			logger.debug("Login to domain=" + domain + " user=" + username + " was successful!");
			triggerLoginSuccessListener(user);
			close();
		} catch (Exception t) {
			logger.error("login failed", t);
			warningLbl.setText("Login Failed: " + t.getMessage());
		}
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

	protected void triggerLoginCancelListener() {
		for (ILoginEventListener listener : loginListeners) {
			listener.onCancel();
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

		public void onCancel();
	}

}
