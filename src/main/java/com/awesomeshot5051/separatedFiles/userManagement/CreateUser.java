package com.awesomeshot5051.separatedFiles.userManagement;

import com.awesomeshot5051.Main;
import com.awesomeshot5051.separatedFiles.PasswordHasher;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class CreateUser {
    private final Connection connection;

    public CreateUser(Stage primaryStage) {
        this.connection = Main.getConnection();
        showCreateUserDialog(primaryStage);
    }

    private void showCreateUserDialog(Stage parentStage) {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initOwner(parentStage);
        dialog.setTitle("Create New User");

        GridPane grid = new GridPane();
        grid.setPadding(new Insets(10));
        grid.setVgap(10);
        grid.setHgap(10);

        Label usernameLabel = new Label("Username:");
        TextField usernameField = new TextField();

        Label passwordLabel = new Label("Password:");
        PasswordField passwordField = new PasswordField();
        TextField passwordTextField = new TextField();
        passwordTextField.setManaged(false);
        passwordTextField.setVisible(false);

        CheckBox showPasswordCheckbox = new CheckBox("Show Password");
        showPasswordCheckbox.setOnAction(e -> {
            if (showPasswordCheckbox.isSelected()) {
                passwordTextField.setText(passwordField.getText());
                passwordTextField.setVisible(true);
                passwordTextField.setManaged(true);
                passwordField.setVisible(false);
                passwordField.setManaged(false);
            } else {
                passwordField.setText(passwordTextField.getText());
                passwordField.setVisible(true);
                passwordField.setManaged(true);
                passwordTextField.setVisible(false);
                passwordTextField.setManaged(false);
            }
        });

        Label nameLabel = new Label("Full Name:");
        TextField nameField = new TextField();

        Label groupLabel = new Label("Group Type:");
        ComboBox<String> groupComboBox = new ComboBox<>();
        groupComboBox.getItems().addAll("Standard", "Admin", "SuperAdmin");
        groupComboBox.setValue("Standard");

        Label statusLabel = new Label("Status:");
        ComboBox<String> statusComboBox = new ComboBox<>();
        statusComboBox.getItems().addAll("Enabled", "Disabled");
        statusComboBox.setValue("Enabled");

        Button createButton = new Button("Create User");
        createButton.setOnAction(e -> {
            String username = usernameField.getText().trim();
            String fullName = nameField.getText().trim();
            String groupType = groupComboBox.getValue();
            String status = statusComboBox.getValue();
            String password = showPasswordCheckbox.isSelected() ? passwordTextField.getText() : passwordField.getText();

            if (username.isEmpty() || fullName.isEmpty() || password.isEmpty()) {
                showAlert(Alert.AlertType.ERROR, "Missing Fields", "All fields must be filled out.");
                return;
            }

            createUserInDatabase(username, fullName, groupType, status, password);
            dialog.close();
        });

        Button cancelButton = new Button("Cancel");
        cancelButton.setOnAction(e -> dialog.close());

        grid.add(usernameLabel, 0, 0);
        grid.add(usernameField, 1, 0);
        grid.add(passwordLabel, 0, 1);
        grid.add(passwordField, 1, 1);
        grid.add(passwordTextField, 1, 1);
        grid.add(showPasswordCheckbox, 1, 2);
        grid.add(nameLabel, 0, 3);
        grid.add(nameField, 1, 3);
        grid.add(groupLabel, 0, 4);
        grid.add(groupComboBox, 1, 4);
        grid.add(statusLabel, 0, 5);
        grid.add(statusComboBox, 1, 5);
        grid.add(createButton, 0, 6);
        grid.add(cancelButton, 1, 6);

        Scene scene = new Scene(grid, 350, 350);
        dialog.setScene(scene);
        dialog.showAndWait();
    }


    private void createUserInDatabase(String name, String username, String groupType, String status, String password) {
        PasswordHasher hasher = new PasswordHasher(password);
        String saltedHashedPassword = hasher.generateSaltedHashedPassword(); // Generates both salt & hash
        String salt = hasher.getSalt(); // Retrieves the generated salt

        try (PreparedStatement stmt = connection.prepareStatement("CALL createUser(?, ?, ?, ?, ?, ?, ?, ?)")) {
            stmt.setString(1, name);                   // p_name
            stmt.setString(2, username);               // p_username
            stmt.setString(3, saltedHashedPassword);   // p_password
            stmt.setString(4, groupType);              // p_group
            stmt.setString(5, status);                 // p_status
            stmt.setString(6, salt);                   // p_salt
            stmt.setString(7, null);                   // p_expiration_time (assuming null is acceptable)
            stmt.setInt(8, 90);                        // p_expires_after_days (set to 90 days, adjust as needed)

            stmt.executeUpdate();
            showAlert(Alert.AlertType.INFORMATION, "Success", "User created successfully.");
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to create user: " + e.getMessage());
        }
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);

        // Create a scrollable, non-editable TextArea for the message
        TextArea textArea = new TextArea(message);
        textArea.setEditable(false);
        textArea.setWrapText(true);

        textArea.setMaxWidth(Double.MAX_VALUE);
        textArea.setMaxHeight(Double.MAX_VALUE);
        GridPane.setVgrow(textArea, Priority.ALWAYS);
        GridPane.setHgrow(textArea, Priority.ALWAYS);

        // Put the TextArea in a GridPane for scaling
        GridPane content = new GridPane();
        content.setMaxWidth(Double.MAX_VALUE);
        content.add(textArea, 0, 0);

        alert.getDialogPane().setContent(content);
        alert.getDialogPane().setPrefSize(600, 400); // Optional: set default size

        alert.showAndWait();
    }


}
