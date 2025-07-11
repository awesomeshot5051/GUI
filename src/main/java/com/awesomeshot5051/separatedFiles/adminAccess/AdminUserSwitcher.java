package com.awesomeshot5051.separatedFiles.adminAccess;

import com.awesomeshot5051.*;
import com.awesomeshot5051.separatedFiles.*;
import com.awesomeshot5051.separatedFiles.session.*;
import javafx.geometry.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.image.*;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import javafx.stage.*;

import java.sql.*;
import java.util.*;

public class AdminUserSwitcher {

    private final Connection connection = Main.getConnection();

    public void showUserSwitchGUI() {
        Stage stage = new Stage();
        Image icon = IconFinder.findIcon();
        stage.getIcons().add(icon);
        stage.setTitle("Authenticate Admin");

        Label passLabel = new Label("Enter Admin Password:");
        PasswordField passwordField = new PasswordField();
        Button submitButton = new Button("Authenticate");
        passwordField.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER || e.getSource() == submitButton) {
                if (authenticateAdmin(SessionManager.getUsername(), passwordField.getText())) {
                    stage.close();
                    showUserSelectionGUI();
                } else {
                    showAlert("Incorrect password!");
                    Main.getLogger().warning("Invalid login by " + SessionManager.getUsername());
                }
            }
        });

        submitButton.setOnAction(e -> passwordField.fireEvent(
                new KeyEvent(KeyEvent.KEY_PRESSED, "", "", KeyCode.ENTER, false, false, false, false)
        ));


        VBox layout = new VBox(10, passLabel, passwordField, submitButton);
        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new Insets(20));

        Scene scene = new Scene(layout, 300, 200);
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/styles/Styles.css")).toExternalForm());

        stage.setScene(scene);
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

        Scene scene = new Scene(layout, 300, 200);
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/styles/Styles.css")).toExternalForm());

        stage.setScene(scene);
        stage.show();
    }

    private boolean authenticateAdmin(String adminUsername, String password) {
        PasswordHasher hasher = new PasswordHasher(password);
        String hashedPassword = hasher.getUnsaltedHashedPassword() + hasher.getSalt(adminUsername);

        try (PreparedStatement stmt = connection.prepareStatement("SELECT password FROM users WHERE username = ?")) {
            stmt.setString(1, adminUsername);
            ResultSet rs = stmt.executeQuery();
            return rs.next() && rs.getString("password").equals(hashedPassword);
        } catch (SQLException e) {
            Main.getErrorLogger().handleException("Error finding Admin Password for user", e);
        }
        return false;
    }

    private void loadUserList(ComboBox<String> userDropdown) {
        try (PreparedStatement stmt = connection.prepareStatement("SELECT username FROM users")) {
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                if (!Objects.equals(rs.getString("username"), SessionManager.getUsername())) {
                    userDropdown.getItems().add(rs.getString("username"));
                }
            }
        } catch (SQLException e) {
            Main.getErrorLogger().handleException("Error loading user list", e);
        }
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/styles/Styles.css")).toExternalForm());
        dialogPane.getStyleClass().add("form-container");

        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
