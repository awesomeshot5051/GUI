package com.awesomeshot5051.separatedFiles.dashboards;

import com.awesomeshot5051.*;
import com.awesomeshot5051.separatedFiles.*;
import com.awesomeshot5051.separatedFiles.Styler.*;
import com.awesomeshot5051.separatedFiles.adminAccess.*;
import com.awesomeshot5051.separatedFiles.extraStuff.*;
import com.awesomeshot5051.separatedFiles.logs.*;
import com.awesomeshot5051.separatedFiles.personalInfo.*;
import com.awesomeshot5051.separatedFiles.security.*;
import com.awesomeshot5051.separatedFiles.session.*;
import com.awesomeshot5051.separatedFiles.systemConfiguration.*;
import com.awesomeshot5051.separatedFiles.systemConfiguration.database.*;
import com.awesomeshot5051.separatedFiles.systemConfiguration.folderManagement.*;
import com.awesomeshot5051.separatedFiles.userManagement.*;
import javafx.geometry.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.util.*;

public class SuperAdminDashboard implements MainScreen.DashboardScreen {

    @Override
    public Parent getView() {
        VBox root = new VBox(15);
        root.setPadding(new Insets(20));
        root.setAlignment(Pos.CENTER);
        root.setFillWidth(true);

        // Add style class "root" for background styling
        root.getStyleClass().add("root");

        root.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/styles/dashboard.css")).toExternalForm());

        Label welcome = new Label("Welcome, " + SessionManager.getName());
        welcome.getStyleClass().add("dashboard-header");

        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        // Add style class to ScrollPane to enable CSS targeting
        scrollPane.getStyleClass().add("scroll-pane");

        GridPane buttonGrid = new GridPane();
        buttonGrid.setHgap(10);
        buttonGrid.setVgap(10);
        buttonGrid.setPadding(new Insets(5));
        buttonGrid.setAlignment(Pos.CENTER);

        // Add style class "grid-pane" for background styling
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

        scrollPane.setContent(buttonGrid);

        Button logoutButton = getLogoutButton();
        logoutButton.setMaxWidth(300);
        logoutButton.getStyleClass().add("button");

        root.getChildren().addAll(welcome, scrollPane, logoutButton);

        return root;
    }

    private List<Button> createDashboardButtons() {
        List<Button> buttons = new ArrayList<>();

        // Personal Information buttons
        Button changeUsername = new Button("Change Username");
        changeUsername.setOnAction(e -> new infoChangeGUI().showChangeUsernameGUI());
        buttons.add(changeUsername);

        Button changePassword = new Button("Change Password");
        changePassword.setOnAction(e -> new infoChangeGUI().showChangePasswordGUI());
        buttons.add(changePassword);

        // User Management buttons
        Button manageUsersButton = new Button("Manage Users");
        manageUsersButton.setOnAction(e -> new ManageUsers(Main.getStage()));
        buttons.add(manageUsersButton);

        Button createUsersButton = new Button("Create Users");
        createUsersButton.setOnAction(e -> new CreateUser(Main.getStage()));
        buttons.add(createUsersButton);

        // System buttons
        Button viewLogsButton = new Button("View Logs");
        viewLogsButton.setOnAction(e -> new LogViewer().showLogSelectionDialog());
        buttons.add(viewLogsButton);

        Button dbCommandsButton = new Button("Execute and Interact with the Database");
        dbCommandsButton.setOnAction(e -> new DatabaseGUI());
        buttons.add(dbCommandsButton);

        Button systemConfigButton = new Button("System Configurations");
        systemConfigButton.setOnAction(e -> new SystemConfigurationGUI().SystemConfigurationMainGUI());
        buttons.add(systemConfigButton);

        Button manageFilesButton = new Button("Manage Files and Folders");
        manageFilesButton.setOnAction(e -> ManageFiles.launchFolderScanner());
        buttons.add(manageFilesButton);

        Button vault = new Button("Vault");
        vault.setOnAction(e -> new VaultManagementScreen().VaultManagementMainGUI());
        buttons.add(vault);


        // Switch user button
        Button switchUserButton = getSwitchUserButton();
        buttons.add(switchUserButton);

        Button codeCrackerButton = new Button("Code Cracker");
        codeCrackerButton.setOnAction(e ->
                new BruteForceCodeCracker().start(Main.getStage())
        );
        buttons.add(codeCrackerButton);

        Button numberGameButton = new Button("Number Game");
        numberGameButton.setOnAction(e ->
                new NumberGame()
        );
        buttons.add(numberGameButton);
        return buttons;
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
        return switchUserButton;
    }

    public Button getLogoutButton() {
        Button logoutButton = new Button(SessionManager.isSwitchedUser() ? "Exit User Mode" : "Logout");
        logoutButton.setOnAction(e -> {
            if (SessionManager.isSwitchedUser()) {
                SessionManager.revertToAdmin();
                Main.getStage().setScene(new Scene(new SuperAdminDashboard().getView()));
            } else {
                // Use the reusable method properly
                new Main().loginScreen(); // This is now safe because it uses Main.getStage()
            }
        });
        logoutButton.getStyleClass().add("button");
        return logoutButton;
    }

}