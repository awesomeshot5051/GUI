package com.awesomeshot5051.separatedFiles.dashboards;

import com.awesomeshot5051.*;
import com.awesomeshot5051.separatedFiles.*;
import com.awesomeshot5051.separatedFiles.adminAccess.*;
import com.awesomeshot5051.separatedFiles.logs.*;
import com.awesomeshot5051.separatedFiles.personalInfo.*;
import com.awesomeshot5051.separatedFiles.session.*;
import com.awesomeshot5051.separatedFiles.systemConfiguration.database.*;
import com.awesomeshot5051.separatedFiles.systemConfiguration.folderManagement.*;
import com.awesomeshot5051.separatedFiles.userManagement.*;
import javafx.geometry.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;

public class SuperAdminDashboard implements MainScreen.DashboardScreen {

    @Override
    public Parent getView() {
        VBox root = new VBox(10);
        root.setPadding(new Insets(20));

        Label welcome = new Label("Welcome, " + SessionManager.getName());

        Button manageUsersButton = new Button("Manage Users");
        manageUsersButton.setOnAction(e -> new ManageUsers(Main.getStage()));

        Button changeUsername = new Button("Change Username");
        changeUsername.setOnAction(e -> new infoChangeGUI().showChangeUsernameGUI());

        Button changePassword = new Button("Change Password");
        changePassword.setOnAction(e -> new infoChangeGUI().showChangePasswordGUI());

        Button createUsersButton = new Button("Create Users");
        createUsersButton.setOnAction(e -> new CreateUser(Main.getStage()));

        Button viewLogsButton = new Button("View Logs");
        viewLogsButton.setOnAction(e -> new LogViewer().readLog());

        Button dbCommandsButton = new Button("Execute and Interact with the Database");
        dbCommandsButton.setOnAction(e -> new DatabaseGUI());


        Button systemConfigButton = new Button("System Configurations");
        systemConfigButton.setOnAction(e -> System.out.println("SuperAdmin: System Configurations clicked."));

        Button manageFilesButton = new Button("Manage Files and Folders");
        manageFilesButton.setOnAction(e -> ManageFiles.launchFolderScanner());

        // Switch User Button (Delegates to AdminUserSwitcher)
        Button switchUserButton = getSwitchUserButton();


        // Logout or Exit Switch Mode Button
        Button logoutButton = getLogoutButton();

        root.getChildren().addAll(
                welcome, changeUsername, changePassword,
                manageUsersButton, createUsersButton,
                viewLogsButton, dbCommandsButton, manageFilesButton,
                systemConfigButton,
                switchUserButton, logoutButton
        );

        root.requestLayout();
        return root;
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

    public Button getLogoutButton() {
        Button logoutButton = new Button(SessionManager.isSwitchedUser() ? "Exit User Mode" : "Logout");
        logoutButton.setOnAction(e -> {
            if (SessionManager.isSwitchedUser()) {
                SessionManager.revertToAdmin(); // Go back to original SuperAdmin session
                Main.getStage().setScene(new Scene(new SuperAdminDashboard().getView())); // Reload dashboard
            } else {
                Main.getStage().close(); // Close application
                new Main().loginScreen();
            }
        });
        return logoutButton;
    }
}
