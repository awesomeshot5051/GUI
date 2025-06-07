package com.awesomeshot5051.separatedFiles.dashboards;

import com.awesomeshot5051.*;
import com.awesomeshot5051.separatedFiles.*;
import com.awesomeshot5051.separatedFiles.accesskey.*;
import com.awesomeshot5051.separatedFiles.adminAccess.*;
import com.awesomeshot5051.separatedFiles.extraStuff.*;
import com.awesomeshot5051.separatedFiles.logs.*;
import com.awesomeshot5051.separatedFiles.personalInfo.*;
import com.awesomeshot5051.separatedFiles.security.*;
import com.awesomeshot5051.separatedFiles.session.*;
import com.awesomeshot5051.separatedFiles.systemConfiguration.folderManagement.*;
import com.awesomeshot5051.separatedFiles.userManagement.*;
import javafx.geometry.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.util.*;

public class AdminDashboard implements MainScreen.DashboardScreen {

    @Override
    public Parent getView() {
        // Main container
        VBox root = new VBox(15);
        root.setPadding(new Insets(20));
        root.setAlignment(Pos.CENTER); // Center the entire content

        // Welcome header
        Label welcome = new Label("Welcome, " + SessionManager.getName());
        welcome.getStyleClass().add("dashboard-header");

        // Create a scrollable area for the buttons
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        // Create a grid layout for the buttons (2 columns)
        GridPane buttonGrid = new GridPane();
        buttonGrid.setHgap(10);
        buttonGrid.setVgap(10);
        buttonGrid.setPadding(new Insets(5));
        buttonGrid.setAlignment(Pos.CENTER); // Center the grid content

        // Set column constraints to ensure both columns have equal width
        ColumnConstraints column1 = new ColumnConstraints();
        column1.setPercentWidth(50);
        column1.setHalignment(HPos.CENTER); // Center horizontally within each cell

        ColumnConstraints column2 = new ColumnConstraints();
        column2.setPercentWidth(50);
        column2.setHalignment(HPos.CENTER); // Center horizontally within each cell

        buttonGrid.getColumnConstraints().addAll(column1, column2);

        // Create all dashboard buttons
        List<Button> dashboardButtons = createDashboardButtons();

        // Arrange buttons in 2 columns
        int row = 0;
        int col = 0;
        for (Button button : dashboardButtons) {
            button.setMaxWidth(Double.MAX_VALUE);
            buttonGrid.add(button, col, row);
            GridPane.setFillWidth(button, true);

            // Move to next column or row
            col++;
            if (col > 1) {
                col = 0;
                row++;
            }
        }

        scrollPane.setContent(buttonGrid);

        // Add logout button at the bottom
        Button logoutButton = getLogoutButton();
        logoutButton.setMaxWidth(300); // Limit width for logout button

        // Add all elements to the root container
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

        Button manageFilesButton = new Button("Manage Files and Folders");
        manageFilesButton.setOnAction(e -> ManageFiles.launchFolderScanner());
        buttons.add(manageFilesButton);

        Button vault = new Button("Vault");
        vault.setOnAction(e -> {
            if (SessionManager.isAccessKeyValid()) {
                new VaultManagementScreen().VaultManagementMainGUI();
            } else {
                new AccessKeyVerification().AccessKeyVerificationWindow();
            }
        });

        buttons.add(vault);

        // Switch user button
        Button switchUserButton = getSwitchUserButton();
        buttons.add(switchUserButton);

        // Extra fun features
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
                alert.setTitle("Already Switched");
                alert.setHeaderText(null);
                alert.setContentText("You're already logged in as someone else! Logout to switch to a different user.");
                alert.showAndWait();
            });
        }
        return switchUserButton;
    }

    @Override
    public Button getLogoutButton() {
        Button logoutButton = new Button(SessionManager.isSwitchedUser() ? "Exit User Mode" : "Logout");
        logoutButton.setOnAction(e -> {
            if (SessionManager.isSwitchedUser()) {
                SessionManager.revertToAdmin(); // Go back to original Admin session
                Main.getStage().setScene(new Scene(new AdminDashboard().getView())); // Reload dashboard
            } else {
                Main.getStage().close(); // Close application
                new Main().loginScreen();
            }
        });
        return logoutButton;
    }
}