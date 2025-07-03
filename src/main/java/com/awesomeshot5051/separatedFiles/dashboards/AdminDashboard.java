package com.awesomeshot5051.separatedFiles.dashboards;

import com.awesomeshot5051.*;
import com.awesomeshot5051.separatedFiles.*;
import com.awesomeshot5051.separatedFiles.Messages.*;
import com.awesomeshot5051.separatedFiles.Styler.*;
import com.awesomeshot5051.separatedFiles.accesskey.*;
import com.awesomeshot5051.separatedFiles.adminAccess.*;
import com.awesomeshot5051.separatedFiles.extraStuff.*;
import com.awesomeshot5051.separatedFiles.logs.*;
import com.awesomeshot5051.separatedFiles.personalInfo.*;
import com.awesomeshot5051.separatedFiles.security.PasswordManagement.*;
import com.awesomeshot5051.separatedFiles.security.*;
import com.awesomeshot5051.separatedFiles.session.*;
import com.awesomeshot5051.separatedFiles.systemConfiguration.folderManagement.*;
import com.awesomeshot5051.separatedFiles.userManagement.*;
import javafx.application.*;
import javafx.geometry.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.util.*;

public class AdminDashboard implements MainScreen.DashboardScreen {

    @Override
    public Parent getView() {
        VBox root = new VBox(15);
        root.setPadding(new Insets(20));
        root.setAlignment(Pos.CENTER);
        root.getStyleClass().add("root");

        root.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/styles/dashboard.css")).toExternalForm());

        Label welcome = new Label("Welcome, " + SessionManager.getName());
        welcome.getStyleClass().add("dashboard-header");

        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.getStyleClass().add("scroll-pane");

        GridPane buttonGrid = new GridPane();
        buttonGrid.setHgap(10);
        buttonGrid.setVgap(10);
        buttonGrid.setPadding(new Insets(5));
        buttonGrid.setAlignment(Pos.CENTER);
        buttonGrid.getStyleClass().add("grid-pane");

        ColumnConstraints column1 = new ColumnConstraints();
        column1.setPercentWidth(50);
        column1.setHalignment(HPos.CENTER);

        ColumnConstraints column2 = new ColumnConstraints();
        column2.setPercentWidth(50);
        column2.setHalignment(HPos.CENTER);

        buttonGrid.getColumnConstraints().addAll(column1, column2);

        List<Button> dashboardButtons = createDashboardButtons();

        int row = 0;
        int col = 0;
        for (Button button : dashboardButtons) {
            button.setMaxWidth(Double.MAX_VALUE);
            button.getStyleClass().add("button");
            buttonGrid.add(button, col, row);
            GridPane.setFillWidth(button, true);

            col++;
            if (col > 1) {
                col = 0;
                row++;
            }
        }

        VBox panelWrapper = new VBox(buttonGrid);
        panelWrapper.setPadding(new Insets(20));
        panelWrapper.getStyleClass().add("panel");

        scrollPane.setContent(panelWrapper);

        Button logoutButton = getLogoutButton();
        logoutButton.setMaxWidth(300);
        logoutButton.getStyleClass().add("button");

        root.getChildren().addAll(welcome, scrollPane, logoutButton);

        return root;
    }

    private List<Button> createDashboardButtons() {
        List<Button> buttons = new ArrayList<>();

        Button changeUsername = new Button("Change Username");
        changeUsername.setOnAction(e -> new infoChangeGUI().showChangeUsernameGUI());
        buttons.add(changeUsername);

        Button changePassword = new Button("Change Password");
        changePassword.setOnAction(e -> new infoChangeGUI().showChangePasswordGUI());
        buttons.add(changePassword);

        Button manageUsersButton = new Button("Manage Users");
        manageUsersButton.setOnAction(e -> new ManageUsers(Main.getStage()));
        buttons.add(manageUsersButton);

        Button createUsersButton = new Button("Create Users");
        createUsersButton.setOnAction(e -> new CreateUser(Main.getStage()));
        buttons.add(createUsersButton);

        Button viewLogsButton = new Button("View Logs");
        viewLogsButton.setOnAction(e -> new LogViewer().showLogSelectionDialog());
        buttons.add(viewLogsButton);

        Button manageFilesButton = new Button("Manage Files and Folders");
        manageFilesButton.setOnAction(e -> ManageFiles.launchFolderScanner());
        buttons.add(manageFilesButton);

        Button vault = accessVault();
        buttons.add(vault);

        Button switchUserButton = getSwitchUserButton();
        buttons.add(switchUserButton);

        Button codeCrackerButton = new Button("Code Cracker");
        codeCrackerButton.setOnAction(e -> new BruteForceCodeCracker().start(Main.getStage()));
        buttons.add(codeCrackerButton);

        Button numberGameButton = new Button("Number Game");
        numberGameButton.setOnAction(e -> new NumberGame());
        buttons.add(numberGameButton);

        Button passwordManagerButton = getPasswordManagerButton();
        buttons.add(passwordManagerButton);
        return buttons;
    }

    private static Button getPasswordManagerButton() {
        Button passwordManagerButton = new Button("Password Manager");
        passwordManagerButton.setOnAction(e -> {
            Platform.runLater(() -> {
                if (SessionManager.isSwitchedUser()) {
                    Alert alert = new Alert(Alert.AlertType.WARNING);
                    FXAlertStyler.style(alert);
                    alert.setTitle("Switch User Mode");
                    alert.setHeaderText(null);
                    alert.setContentText("You must exit user mode before accessing the password manager! Please logout and try again!");
                    MessageHandler.sendMessage(SessionManager.getUser(), SessionManager.getOriginalAdminUser().getUsername() + " tried to access your password manager while in switched-user mode! Please report this to the admin. If they are the admin, please disregard this message, as it was a test of User Access Control");
                    alert.showAndWait();
                    return;
                }
                try {
                    new PasswordManager().showPasswordManager(Main.getStage());
                } catch (Exception ex) {
                    throw new RuntimeException(ex);
                }
            });
        });
        return passwordManagerButton;
    }

    private static Button accessVault() {
        Button vault = new Button("Vault");
        vault.setOnAction(e -> {
            if (SessionManager.isSwitchedUser()) {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                FXAlertStyler.style(alert);
                alert.setTitle("Switch User Mode");
                alert.setHeaderText(null);
                alert.setContentText("You must exit user mode before accessing the vault! Please logout and try again!");
                alert.showAndWait();
                Main.getLogger().severe(SessionManager.getOriginalAdminUser().getUsername() + " tried to access someone else's vault while in switched-user mode! This will be reported!");
                MessageHandler.sendMessage(SessionManager.getUser(), SessionManager.getOriginalAdminUser().getUsername() + " tried to access your vault while in switched-user mode! Please report this to the admin If they are the admin, please disregard this message, as it was a test of User Access Control");
                return;
            }
            if (SessionManager.isAccessKeyValid()) {
                new VaultManagementScreen().VaultManagementMainGUI();
            } else {
                new AccessKeyVerification().AccessKeyVerificationWindow();
            }
        });
        return vault;
    }

    private static Button getSwitchUserButton() {
        Button switchUserButton = new Button("Switch User");

        if (!SessionManager.isSwitchedUser()) {
            switchUserButton.setOnAction(e -> new AdminUserSwitcher().showUserSwitchGUI());
        } else {
            switchUserButton.setOnAction(e -> {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                FXAlertStyler.style(alert);
                alert.setTitle("Already Switched");
                alert.setHeaderText(null);
                alert.setContentText("You're already logged in as someone else! Logout to switch to a different user.");
                alert.showAndWait();
            });
        }

        switchUserButton.getStyleClass().addAll("button");
        return switchUserButton;
    }

    @Override
    public Button getLogoutButton() {
        Button logoutButton = new Button(SessionManager.isSwitchedUser() ? "Exit User Mode" : "Logout");
        logoutButton.setOnAction(e -> {
            if (SessionManager.isSwitchedUser()) {
                SessionManager.revertToAdmin();
                Main.getStage().setScene(new Scene(new AdminDashboard().getView()));
            } else {
                Main.getStage().close();
                new Main().loginScreen();
            }
        });
        return logoutButton;
    }
}
