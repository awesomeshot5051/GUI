package com.awesomeshot5051.separatedFiles.dashboards;

import com.awesomeshot5051.*;
import com.awesomeshot5051.separatedFiles.*;
import com.awesomeshot5051.separatedFiles.session.*;
import javafx.geometry.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import static com.awesomeshot5051.separatedFiles.systemConfiguration.folderManagement.ManageFiles.*;

public class StandardDashboard implements MainScreen.DashboardScreen {

    @Override
    public Parent getView() {
        VBox root = new VBox(10);
        root.setPadding(new Insets(20));

        Label welcome = new Label("Welcome, Standard User!");
        Button viewContentButton = new Button("View Content");
        viewContentButton.setOnAction(e -> System.out.println("Standard: View Content clicked."));

        Button manageFilesButton = new Button("Manage Files and Folders");
        manageFilesButton.setOnAction(e -> launchFolderScanner());

        Button logoutButton = new Button("Logout");
        logoutButton.setOnAction(e -> {
            if (SessionManager.isSwitchedUser()) {
                SessionManager.revertToAdmin();
            } else {
                Main.getStage().close(); // Assuming getInstance() provides a singleton
                new Main().loginScreen();
            }
        });

        root.getChildren().addAll(welcome, viewContentButton, logoutButton);
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
