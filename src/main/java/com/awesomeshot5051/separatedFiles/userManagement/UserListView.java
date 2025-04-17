package com.awesomeshot5051.separatedFiles.userManagement;

import com.awesomeshot5051.*;
import com.awesomeshot5051.separatedFiles.*;
import com.awesomeshot5051.separatedFiles.group.*;
import com.awesomeshot5051.separatedFiles.session.*;
import com.awesomeshot5051.separatedFiles.systemConfiguration.database.*;
import javafx.collections.*;
import javafx.geometry.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.control.cell.*;
import javafx.scene.layout.*;
import javafx.stage.*;

import java.sql.*;
import java.util.*;

public class UserListView {
    private final Stage primaryStage;
    private final TableView<User> userTable = new TableView<>();
    private final Connection connection;
    private final String currentUser; // Stores the logged-in username

    public UserListView(Stage primaryStage) {
        this.primaryStage = primaryStage;
        this.connection = Main.getConnection();
        this.currentUser = SessionManager.getUsername();
    }

    public void show() {
        BorderPane root = new BorderPane();

        // Create table columns
        TableColumn<User, String> nameColumn = new TableColumn<>("Name");
        nameColumn.setCellValueFactory(cellData -> cellData.getValue().nameProperty());

        TableColumn<User, String> usernameColumn = new TableColumn<>("Username");
        usernameColumn.setCellValueFactory(cellData -> cellData.getValue().usernameProperty());

        TableColumn<User, String> groupColumn = new TableColumn<>("Group");
        groupColumn.setCellValueFactory(cellData -> cellData.getValue().groupProperty());
        if (!(SessionManager.getGroupType() instanceof SuperAdminIGroup)) {
            groupColumn.setCellFactory(ComboBoxTableCell.forTableColumn("Default", "Standard", "Admin"));
        } else
            groupColumn.setCellFactory(ComboBoxTableCell.forTableColumn("Default", "Standard", "Admin", "SuperAdmin"));
        // When the user edits the group column, just update the User object in memory
        groupColumn.setOnEditCommit(event -> {
            User user = event.getRowValue(); // Get the user being updated
            try {
                user.setGroup(event.getNewValue());
                user.setModified(true);
                // Update the group in the User object
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });

        TableColumn<User, String> statusColumn = new TableColumn<>("Status");
        statusColumn.setCellValueFactory(cellData -> cellData.getValue().statusProperty());
        statusColumn.setCellFactory(ComboBoxTableCell.forTableColumn("Enabled", "Disabled"));
        statusColumn.setOnEditCommit(event -> {
            User user = event.getRowValue();
            user.setStatus(event.getNewValue());
            user.setModified(true); // ✅ Mark modified when status is changed
        });


        TableColumn<User, Void> deleteColumn = getUserVoidTableColumn();

        // Add columns to the table
        userTable.getColumns().addAll(Arrays.asList(nameColumn, usernameColumn, groupColumn, statusColumn, deleteColumn));

        userTable.setEditable(true);

        // Load user data
        loadUsersFromDatabase();

        // Add the table to the center of the UI
        root.setCenter(userTable);

        // Button Panel
        HBox buttonPanel = new HBox(10);
        buttonPanel.setAlignment(Pos.CENTER);
        Button updateDatabaseButton = new Button("Update Database");
        Button exitButton = new Button("Exit");

        // Set button actions
        updateDatabaseButton.setOnAction(event -> {
            // Update all users in the database
            for (User user : userTable.getItems()) {
                new DatabaseInteraction().updateUser(user); // Update the database using the in-memory User object
            }
        });
        exitButton.setOnAction(event -> exitUserManagement());

        buttonPanel.getChildren().addAll(updateDatabaseButton, exitButton);
        root.setBottom(buttonPanel);

        // Set the scene and show the stage
        Scene scene = new Scene(root, 800, 600);
        primaryStage.setScene(scene);
        primaryStage.setTitle("User List");
        primaryStage.show();
    }

    private TableColumn<User, Void> getUserVoidTableColumn() {
        TableColumn<User, Void> deleteColumn = new TableColumn<>("Actions");
        deleteColumn.setCellFactory(param -> new TableCell<>() {
            private final Button deleteButton = new Button("Delete");

            {
                deleteButton.setOnAction(event -> {
                    User user = getTableView().getItems().get(getIndex());

                    try {
                        // Call the DatabaseInteraction deleteUser method
                        new DatabaseInteraction().deleteUser(user, currentUser);

                        // Refresh the table
                        loadUsersFromDatabase();
                    } catch (Exception e) {
                        // Handle any exceptions (optional)
                        e.printStackTrace();
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(deleteButton);
                }
            }
        });
        return deleteColumn;
    }

    public void loadUsersFromDatabase() {
        ObservableList<User> userList = FXCollections.observableArrayList();

        try (PreparedStatement statement = connection.prepareStatement("SELECT name, username, `group`, status FROM users");
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                userList.add(new User(
                        resultSet.getString("name"),
                        resultSet.getString("username"),
                        resultSet.getString("group"),
                        resultSet.getString("status")
                ));
            }

            userTable.setItems(userList);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void exitUserManagement() {
        primaryStage.close();
        new MainScreen(SessionManager.getGroupType(), SessionManager.getStatus(), SessionManager.getUsername(), SessionManager.getName(), SessionManager.getConnection(), Main.getStage());
    }

}
