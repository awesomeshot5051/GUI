package com.awesomeshot5051.separatedFiles.personalInfo;

import com.awesomeshot5051.*;
import com.awesomeshot5051.separatedFiles.session.*;
import javafx.scene.control.*;

import java.sql.*;

public class ChangeLoginInfo {
    private final Connection connection;

    public ChangeLoginInfo() {
        this.connection = SessionManager.getConnection();
    }

    public ChangeLoginInfo(Connection connection) {
        this.connection = connection;
    }

    public void changePassword(String name, String username, String newPassword) {
        String sql = "CALL changePassword(?, ?, ?)";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, username);
            stmt.setString(2, name);
            stmt.setString(3, newPassword);
            stmt.execute();
            showAlert("Success", "Password changed successfully!");
        } catch (SQLException e) {
            Main.getErrorLogger().handleException("Error changing password", e);
            showAlert("Error", "Failed to change password.\n" + e.getMessage());
        }
    }

    public void changeUsername(String salt, String newUsername) {
        String sql = "CALL ChangeUsername(?, ?, ?)";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, salt);
            stmt.setString(2, newUsername);
            stmt.setString(3, SessionManager.getUsername());
            stmt.execute();
            showAlert("Success", "Username changed successfully!");
        } catch (SQLException e) {
            Main.getErrorLogger().handleException("Error changing username", e);
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
