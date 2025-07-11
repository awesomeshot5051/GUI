package com.awesomeshot5051.separatedFiles.userManagement;

import com.awesomeshot5051.*;
import com.awesomeshot5051.separatedFiles.*;
import com.awesomeshot5051.separatedFiles.defaultLoginCheck.*;
import com.awesomeshot5051.separatedFiles.group.*;
import com.awesomeshot5051.separatedFiles.session.*;
import javafx.geometry.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.*;

import java.sql.*;
import java.util.*;

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
        grid.setPadding(new Insets(20));
        grid.setVgap(10);
        grid.setHgap(10);
        grid.setAlignment(Pos.CENTER);
        grid.getStyleClass().add("form-container");

        Label usernameLabel = new Label("Username:");
        TextField usernameField = new TextField();
        usernameField.getStyleClass().add("text-field");

        Label passwordLabel = new Label("Password:");
        PasswordField passwordField = new PasswordField();
        passwordField.getStyleClass().add("text-field");

        TextField passwordTextField = new TextField();
        passwordTextField.getStyleClass().add("text-field");
        passwordTextField.setManaged(false);
        passwordTextField.setVisible(false);

        CheckBox showPasswordCheckbox = new CheckBox("Show Password");
        showPasswordCheckbox.getStyleClass().add("checkbox");
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
        nameField.getStyleClass().add("text-field");

        Label groupLabel = new Label("Group Type:");
        ComboBox<String> groupComboBox = new ComboBox<>();
        groupComboBox.getStyleClass().add("combo-box");

        // Populate group options
        try {
            String query = "SELECT COUNT(*) as count FROM users WHERE `group` = 'SuperAdmin'";
            try (PreparedStatement stmt = connection.prepareStatement(query)) {
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    int superAdminCount = rs.getInt("count");
                    if (superAdminCount > 0) {
                        groupComboBox.getItems().addAll("Standard", "Admin");
                    } else {
                        groupComboBox.getItems().addAll("Standard", "Admin", "SuperAdmin");
                    }
                }
            }
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Error checking SuperAdmin existence: " + e.getMessage());
            groupComboBox.getItems().addAll("Standard", "Admin"); // Safe fallback
        }

        groupComboBox.setValue("Standard");

        Label statusLabel = new Label("Status:");
        ComboBox<String> statusComboBox = new ComboBox<>();
        statusComboBox.getStyleClass().add("combo-box");
        statusComboBox.getItems().addAll("Enabled", "Disabled");
        statusComboBox.setValue("Enabled");

        Button createButton = new Button("Create User");
        createButton.getStyleClass().add("button");

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

            boolean success = createUserInDatabase(fullName, username, groupType, status, password);

            if (success) {
                dialog.close();
                if (SessionManager.getGroupType() instanceof DefaultIGroup) {
                    new ManageUserStatus().setUserStatus(SessionManager.getUser(), "Disabled");
                    DefaultAccountChecker.markDefaultAccountUsed();
                    Main.getStage().close();
                    new Main().loginScreen();
                }
            }
        });

        Button cancelButton = new Button("Cancel");
        cancelButton.getStyleClass().add("button-cancel");
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

        Scene scene = new Scene(grid, 380, 380);
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/styles/Styles.css")).toExternalForm());
        dialog.setScene(scene);
        dialog.showAndWait();
    }


    private boolean createUserInDatabase(String name, String username, String groupType, String status, String password) {
        PasswordHasher hasher = new PasswordHasher();
        String salt = hasher.generateRandomSalt();
        String saltedHashedPassword = hasher.hashPassword(password) + salt;

        try (PreparedStatement stmt = connection.prepareStatement("CALL createUser(?, ?, ?, ?, ?, ?, ?, ?)")) {
            stmt.setString(1, name);
            stmt.setString(2, username);
            stmt.setString(3, saltedHashedPassword);
            stmt.setString(4, groupType);
            stmt.setString(5, status);
            stmt.setString(6, salt);
            stmt.setString(7, null);
            stmt.setInt(8, 90);

            stmt.executeUpdate();
            if (SessionManager.getGroupType() instanceof DefaultIGroup) {
                showAlert(Alert.AlertType.INFORMATION, "Success", "User created successfully.\nYou will be logged out and this account will be disabled\nUse your new credentials to log in again.");
            } else showAlert(Alert.AlertType.INFORMATION, "Success", "User created successfully.");
            return true;
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to create user: " + e.getMessage());
            return false;
        }
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);

        TextArea textArea = new TextArea(message);
        textArea.setEditable(false);
        textArea.setWrapText(true);
        textArea.setMaxWidth(Double.MAX_VALUE);
        textArea.setMaxHeight(Double.MAX_VALUE);
        GridPane.setVgrow(textArea, Priority.ALWAYS);
        GridPane.setHgrow(textArea, Priority.ALWAYS);

        GridPane content = new GridPane();
        content.setMaxWidth(Double.MAX_VALUE);
        content.add(textArea, 0, 0);

        alert.getDialogPane().setContent(content);
        alert.getDialogPane().setPrefSize(600, 400);
        alert.showAndWait();
    }
}
