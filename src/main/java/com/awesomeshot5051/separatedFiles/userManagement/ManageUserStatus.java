package com.awesomeshot5051.separatedFiles.userManagement;

import com.awesomeshot5051.separatedFiles.session.*;
import javafx.scene.control.*;

import java.sql.*;

public class ManageUserStatus {

    private final Connection connection;

    public ManageUserStatus() {
        this.connection = SessionManager.getConnection();
    }

    public void setUserStatus(User user, String status) {
        String sql = "CALL manageUserStatus(?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, user.getUsername());
            stmt.setString(2, user.getName());
            stmt.setString(2, status);
            stmt.execute();
            showAlert("Success", "User status changed to " + status + " successfully!");
        } catch (SQLException e) {
            e.printStackTrace(); // Proper error handling
            showAlert("Error", "Failed to change user status.\n" + e.getMessage());
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
