package com.awesomeshot5051.separatedFiles.dashboards;

import com.awesomeshot5051.*;
import com.awesomeshot5051.separatedFiles.*;
import com.awesomeshot5051.separatedFiles.Messages.*;
import com.awesomeshot5051.separatedFiles.Styler.*;
import com.awesomeshot5051.separatedFiles.accesskey.*;
import com.awesomeshot5051.separatedFiles.extraStuff.*;
import com.awesomeshot5051.separatedFiles.personalInfo.*;
import com.awesomeshot5051.separatedFiles.security.PasswordManagement.*;
import com.awesomeshot5051.separatedFiles.security.*;
import com.awesomeshot5051.separatedFiles.session.*;
import com.awesomeshot5051.separatedFiles.systemConfiguration.database.*;
import com.awesomeshot5051.separatedFiles.systemConfiguration.folderManagement.*;
import com.awesomeshot5051.separatedFiles.userManagement.*;
import javafx.application.*;
import javafx.geometry.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.util.*;

public class StandardDashboard implements MainScreen.DashboardScreen {

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

        Button viewContentButton = new Button("View Content");
        viewContentButton.setOnAction(e -> System.out.println("Standard: View Content clicked."));
        buttons.add(viewContentButton);

        Button manageFilesButton = new Button("Manage Files and Folders");
        manageFilesButton.setOnAction(e -> ManageFiles.launchFolderScanner());
        buttons.add(manageFilesButton);

        Button vault = accessVault();
        buttons.add(vault);

        Button codeCrackerButton = new Button("Code Cracker");
        codeCrackerButton.setOnAction(e -> new BruteForceCodeCracker().start(Main.getStage()));
        buttons.add(codeCrackerButton);

        Button numberGameButton = new Button("Number Game");
        numberGameButton.setOnAction(e -> new NumberGame());
        buttons.add(numberGameButton);
        Button requestDeletionButton = requestDeletion();

        buttons.add(requestDeletionButton);

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

    private Button requestDeletion() {
        Button requestDeletionButton = new Button("Request Deletion");
        requestDeletionButton.setOnAction(e -> {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            FXAlertStyler.style(confirm);
            confirm.setTitle("Account Deletion");
            confirm.setHeaderText("Request deletion of your account?");
            confirm.setContentText("Would you like to request deletion from an admin?");


            ButtonType yes = new ButtonType("Yes");
            ButtonType no = new ButtonType("No", ButtonBar.ButtonData.CANCEL_CLOSE);
            confirm.getButtonTypes().setAll(yes, no);

            confirm.showAndWait().ifPresent(response -> {
                if (response == yes) {
                    UserList adminList = DatabaseInteraction.getAdmins(); // Call stored procedure

                    String currentUsername = SessionManager.getUsername(); // Or wherever you store the current user
                    String requestMsg = currentUsername + " has requested their account be deleted.";

                    if (adminList != null) {
                        for (User admin : adminList) {
                            MessageHandler.sendMessage(admin, requestMsg);
                        }
                    }

                    Alert sent = new Alert(Alert.AlertType.INFORMATION);
                    FXAlertStyler.style(sent);
                    sent.setTitle("Request Sent");
                    sent.setHeaderText(null);
                    sent.setContentText("Your deletion request has been sent.");
                    sent.showAndWait();
                }
            });
        });
        return requestDeletionButton;
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
                MessageHandler.sendMessage(SessionManager.getUser(), SessionManager.getOriginalAdminUser().getUsername() + " tried to access your vault while in switched-user mode! Please report this to the admin If they are the admin, please disregard this message, as it was a test of User Access Control");
                alert.showAndWait();
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

    @Override
    public Button getLogoutButton() {
        Button logoutButton = new Button("Logout");
        logoutButton.setOnAction(e -> {
            if (SessionManager.isSwitchedUser()) {
                SessionManager.revertToAdmin();
            } else {
                Main.getStage().close();
                new Main().loginScreen();
            }
        });
        logoutButton.getStyleClass().add("button");
        return logoutButton;
    }
}
