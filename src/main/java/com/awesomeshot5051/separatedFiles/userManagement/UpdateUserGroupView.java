package com.awesomeshot5051.separatedFiles.userManagement;

import javafx.event.*;
import javafx.geometry.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.*;

public class UpdateUserGroupView {

    private Stage primaryStage;
    private ManageUsers manageUsers;

    public UpdateUserGroupView(Stage primaryStage, ManageUsers manageUsers) {
        this.primaryStage = primaryStage;
        this.manageUsers = manageUsers;
    }

    public void show() {
        // Create the main layout
        BorderPane root = new BorderPane();

        // Create a panel to update the user group
        VBox userGroupPanel = new VBox(10);
        userGroupPanel.setAlignment(Pos.CENTER);

        // Dummy user data (replace this with actual data from the database)
        String[] users = {"User1", "User2", "User3"};
        for (String user : users) {
            Label usernameLabel = new Label(user);
            ComboBox<String> groupComboBox = new ComboBox<>();
            groupComboBox.getItems().addAll("Standard", "Admin", "SuperAdmin");
            userGroupPanel.getChildren().addAll(usernameLabel, groupComboBox);
        }

        // Add the user group panel to the center of the layout
        root.setCenter(userGroupPanel);

        // Create the button panel
        VBox buttonPanel = new VBox(10);
        buttonPanel.setAlignment(Pos.CENTER);
        Button updateButton = new Button("Update");
        Button exitButton = new Button("Exit");

        // Add actions for the buttons
        updateButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                // Perform the update operation (you'll need to write the update logic here)
                System.out.println("Update clicked");
            }
        });

        exitButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                manageUsers.exitUserManagement();
            }
        });

        buttonPanel.getChildren().addAll(updateButton, exitButton);
        root.setBottom(buttonPanel);

        // Set the scene and show the stage
        Scene scene = new Scene(root, 600, 400);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Update User IGroup");
        primaryStage.show();
    }
}
