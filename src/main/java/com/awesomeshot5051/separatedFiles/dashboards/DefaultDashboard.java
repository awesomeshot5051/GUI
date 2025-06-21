package com.awesomeshot5051.separatedFiles.dashboards;

import com.awesomeshot5051.*;
import com.awesomeshot5051.separatedFiles.*;
import com.awesomeshot5051.separatedFiles.session.*;
import com.awesomeshot5051.separatedFiles.userManagement.*;
import javafx.geometry.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.util.*;

public class DefaultDashboard implements MainScreen.DashboardScreen {

    @Override
    public Parent getView() {
        VBox root = new VBox(20);
        root.setPadding(new Insets(30));
        root.setAlignment(Pos.CENTER);
        root.getStyleClass().add("root");

        root.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/styles/dashboard.css")).toExternalForm());


        Label welcome = new Label("Welcome, Default User!");
        welcome.getStyleClass().add("dashboard-header");

        Button createUserButton = new Button("Create Users (1 Allowed)");
        createUserButton.setOnAction(e -> new CreateUser(Main.getStage()));
        createUserButton.getStyleClass().add("button");

        Button logoutButton = getLogoutButton();
        logoutButton.setMaxWidth(250);
        logoutButton.getStyleClass().add("button");

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
        logoutButton.getStyleClass().add("button");
        return logoutButton;
    }
}
