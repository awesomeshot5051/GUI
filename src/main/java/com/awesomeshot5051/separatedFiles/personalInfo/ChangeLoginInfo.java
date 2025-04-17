package com.awesomeshot5051.separatedFiles.personalInfo;

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

    public void changePassword(String oldPassword, String newPassword) {
        String sql = "CALL changePassword(?, ?, ?)";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, oldPassword);
            stmt.setString(2, SessionManager.getName());
            stmt.setString(3, newPassword);
            stmt.execute();
            showAlert("Success", "Password changed successfully!");
        } catch (SQLException e) {
            e.printStackTrace(); // Handle errors properly
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
            e.printStackTrace(); // Handle errors properly
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
