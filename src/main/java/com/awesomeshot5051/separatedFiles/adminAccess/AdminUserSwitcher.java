package com.awesomeshot5051.separatedFiles.adminAccess;

import com.awesomeshot5051.*;
import com.awesomeshot5051.separatedFiles.*;
import com.awesomeshot5051.separatedFiles.session.*;
import javafx.geometry.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.*;

import java.sql.*;
import java.util.*;

public class AdminUserSwitcher {

    private final Connection connection = Main.getConnection();

    public void showUserSwitchGUI() {
        Stage stage = new Stage();
        stage.setTitle("Authenticate Admin");

        Label passLabel = new Label("Enter Admin Password:");
        PasswordField passwordField = new PasswordField();
        Button submitButton = new Button("Authenticate");

        submitButton.setOnAction(e -> {
            String adminPassword = passwordField.getText();
            if (authenticateAdmin(SessionManager.getUsername(), adminPassword)) {
                stage.close();
                showUserSelectionGUI();
            } else {
                showAlert("Incorrect password!");
                Main.getLogger().warning("Invalid login by " + SessionManager.getUsername());
            }
        });

        VBox layout = new VBox(10, passLabel, passwordField, submitButton);
        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new Insets(20));

        stage.setScene(new Scene(layout, 300, 200));
        stage.show();
    }

    private void showUserSelectionGUI() {
        Stage stage = new Stage();
        stage.setTitle("Select User");

        Label userLabel = new Label("Choose a user to switch to:");
        ComboBox<String> userDropdown = new ComboBox<>();
        Button switchButton = new Button("Switch User");

        loadUserList(userDropdown);

        switchButton.setOnAction(e -> {
            String selectedUser = userDropdown.getValue();
            if (selectedUser != null) {
                try {
                    SessionManager.switchUser(selectedUser);
                } catch (SQLException ex) {
                    throw new RuntimeException(ex);
                }
                stage.close();
            } else {
                showAlert("Please select a user.");
            }
        });

        VBox layout = new VBox(10, userLabel, userDropdown, switchButton);
        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new Insets(20));

        stage.setScene(new Scene(layout, 300, 200));
        stage.show();
    }

    @SuppressWarnings("CallToPrintStackTrace")
    private boolean authenticateAdmin(String adminUsername, String password) {
        PasswordHasher hasher = new PasswordHasher(password);
        String hashedPassword = hasher.getUnsaltedHashedPassword() + hasher.getSalt(adminUsername);

        try (PreparedStatement stmt = connection.prepareStatement("SELECT password FROM users WHERE username = ?")) {
            stmt.setString(1, adminUsername);
            ResultSet rs = stmt.executeQuery();
            return rs.next() && rs.getString("password").equals(hashedPassword);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @SuppressWarnings("CallToPrintStackTrace")
    private void loadUserList(ComboBox<String> userDropdown) {
        try (PreparedStatement stmt = connection.prepareStatement("SELECT username FROM users")) {
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                if (!Objects.equals(rs.getString("username"), SessionManager.getUsername())) {
                    userDropdown.getItems().add(rs.getString("username"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
