package com.awesomeshot5051.separatedFiles.dashboards;

import com.awesomeshot5051.Main;
import com.awesomeshot5051.separatedFiles.MainScreen;
import com.awesomeshot5051.separatedFiles.session.SessionManager;
import com.awesomeshot5051.separatedFiles.userManagement.CreateUser;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

public class DefaultDashboard implements MainScreen.DashboardScreen {

    @Override
    public Parent getView() {
        VBox root = new VBox(10);
        root.setPadding(new Insets(20));

        Label welcome = new Label("Welcome, Default User!");
        Button createUserButton = new Button("Create Users(1 Allowed)");
        createUserButton.setOnAction(e -> {
            new CreateUser(Main.getStage());
        });

        Button logoutButton = getLogoutButton();

        root.getChildren().addAll(welcome, createUserButton, logoutButton);
        return root;
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
        return logoutButton;
    }
}
