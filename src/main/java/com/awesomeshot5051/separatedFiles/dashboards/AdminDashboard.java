package com.awesomeshot5051.separatedFiles.dashboards;

import com.awesomeshot5051.*;
import com.awesomeshot5051.separatedFiles.*;
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

public class AdminDashboard implements MainScreen.DashboardScreen {

    @Override
    public Parent getView() {
        VBox root = new VBox(10);
        root.setPadding(new Insets(20));

        Label welcome = new Label("Welcome, Admin User!");


        Button changeUsername = new Button("Change Username");
        changeUsername.setOnAction(e -> new infoChangeGUI().showChangeUsernameGUI());

        Button changePassword = new Button("Change Password");
        changePassword.setOnAction(e -> new infoChangeGUI().showChangePasswordGUI());

        Button createUsersButton = new Button("Create Users");
        createUsersButton.setOnAction(e -> new CreateUser(Main.getStage()));

        Button manageFilesButton = new Button("Manage Files and Folders");
        manageFilesButton.setOnAction(e -> ManageFiles.launchFolderScanner());

        Button vault = new Button("Vault");
        vault.setOnAction(e -> new VaultManagementScreen().VaultManagementMainGUI());

        Button manageUsersButton = new Button("Manage Users");
        manageUsersButton.setOnAction(e -> new ManageUsers(Main.getStage()));

        Button viewLogsButton = new Button("View Logs");
        viewLogsButton.setOnAction(e -> new LogViewer().readLog());

        Button logoutButton = getLogoutButton();

        root.getChildren().addAll(welcome, changeUsername, createUsersButton, changePassword, manageUsersButton, viewLogsButton, manageFilesButton, vault, logoutButton);
        return root;
    }

    @Override
    public Button getLogoutButton() {
        Button logoutButton = new Button("Logout");
        logoutButton.setOnAction(e -> {
            if (SessionManager.isSwitchedUser()) {
                SessionManager.revertToAdmin();
            } else {
                Main.getStage().close(); // Assuming getInstance() provides a singleton
                new Main().loginScreen();
            }
        });
        return logoutButton;
    }
}
