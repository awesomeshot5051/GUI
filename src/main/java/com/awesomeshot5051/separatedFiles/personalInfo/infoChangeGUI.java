package com.awesomeshot5051.separatedFiles.personalInfo;

import com.awesomeshot5051.Main;
import com.awesomeshot5051.separatedFiles.PasswordHasher;
import com.awesomeshot5051.separatedFiles.session.SessionManager;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class infoChangeGUI {

    private final Connection connection = Main.getConnection();

    // GUI for changing the username
    public void showChangeUsernameGUI() {
        Stage stage = new Stage();
        stage.setTitle("Change Username");

        Label newUserLabel = new Label("New Username:");
        TextField newUsernameField = new TextField();

        Label passwordLabel = new Label("Password");
        PasswordField passwordField = new PasswordField();

        Button submitButton = getButton(passwordField, newUsernameField, stage);

        VBox layout = new VBox(10, newUserLabel, newUsernameField, passwordLabel, passwordField, submitButton);
        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new Insets(20));

        stage.setScene(new Scene(layout, 300, 200));
        stage.show();
    }

    private Button getButton(TextField passwordField, TextField newUsernameField, Stage stage) {
        Button submitButton = new Button("Submit");

        submitButton.setOnAction(e -> {
            String newUsername = newUsernameField.getText().trim();
            if (!newUsername.isEmpty()) {
                new ChangeLoginInfo().changeUsername(new PasswordHasher(passwordField.getText()).getSalt(SessionManager.getUsername()), newUsernameField.getText());
                stage.close();
            } else {
                showAlert("Error", "Username cannot be empty!");
            }
        });
        return submitButton;
    }

    // GUI for changing the password
    public void showChangePasswordGUI() {
        Stage stage = new Stage();
        stage.setTitle("Change Password");

        Label oldPassLabel = new Label("Old Password:");
        PasswordField oldPassField = new PasswordField();
        Label newPassLabel = new Label("New Password:");
        PasswordField newPassField = new PasswordField();
        Button submitButton = createSubmitButton(oldPassField, newPassField, stage);

        VBox layout = new VBox(10, oldPassLabel, oldPassField, newPassLabel, newPassField, submitButton);
        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new Insets(20));

        stage.setScene(new Scene(layout, 300, 250));
        stage.show();
    }

    private Button createSubmitButton(PasswordField oldPassField, PasswordField newPassField, Stage stage) {
        Button submitButton = new Button("Submit");

        submitButton.setOnAction(e -> {
            String oldPassword = oldPassField.getText();
            String newPassword = newPassField.getText();

            if (newPassword.equals(oldPassword)) {
                showAlert("Error", "New password cannot be the same as the old password.");
                return;
            }

            String username = SessionManager.getUsername();
            if (SessionManager.getConnection() == null) {
                PasswordHasher newPasswordHasher = new PasswordHasher(newPassword);
                new ChangeLoginInfo(Main.getConnection()).changePassword(new PasswordHasher(oldPassword).getHashedPassword(), newPasswordHasher.hashPassword() + newPasswordHasher.getSalt(username));
                stage.close();
            } else {
                if (validateOldPassword(username, oldPassword)) {
                    new ChangeLoginInfo().changePassword(oldPassword, newPassword);
                    stage.close();
                } else {
                    showAlert("Error", "Old password is incorrect!");
                }
            }
        });

        return submitButton;
    }

    // Validate the old password before allowing a change
    private boolean validateOldPassword(String username, String oldPassword) {
        PasswordHasher hasher = new PasswordHasher(oldPassword);
        String hashedPassword = hasher.getUnsaltedHashedPassword() + hasher.getSalt(username);

        try (PreparedStatement stmt = connection.prepareStatement("SELECT password FROM users WHERE username = ?")) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getString("password").equals(hashedPassword);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to change Password.\n" + e.getMessage());

        }
        return false;
    }

    // Display alert box
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
