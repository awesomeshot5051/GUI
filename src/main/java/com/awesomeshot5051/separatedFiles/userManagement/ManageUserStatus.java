package com.awesomeshot5051.separatedFiles.userManagement;

import com.awesomeshot5051.*;
import com.awesomeshot5051.separatedFiles.session.*;

import java.sql.*;

public class ManageUserStatus {

    private final Connection connection;

    public ManageUserStatus() {
        this.connection = (SessionManager.getConnection() != null) ? SessionManager.getConnection() : Main.getConnection();

    }

    public void setUserStatus(User user, String status) {
        String sql = "CALL manageUserStatus(?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, user.getUsername());
            stmt.setString(2, user.getName());
            stmt.setString(3, status);
            stmt.execute();
            Main.getLogger().info("Successfully changed " + user.getUsername() + " status changed to " + status + " successfully!");
        } catch (SQLException e) {
            Main.getLogger().severe("Error Failed to change user status.\n" + e.getMessage());
        }
    }

}
