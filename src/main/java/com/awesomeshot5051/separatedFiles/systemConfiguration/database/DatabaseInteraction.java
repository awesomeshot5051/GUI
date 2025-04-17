package com.awesomeshot5051.separatedFiles.systemConfiguration.database;

import com.awesomeshot5051.Main;
import com.awesomeshot5051.separatedFiles.session.SessionManager;
import com.awesomeshot5051.separatedFiles.userManagement.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class DatabaseInteraction {
    private final Connection connection;

    public DatabaseInteraction() {
        this.connection = SessionManager.getConnection();
    }


    public void updateUser(User user) {
        if ("SuperAdmin".equals(user.getGroup().getGroupName())) {
            return; // Skip SuperAdmin
        }
        if (user.isModified()) {
            try (PreparedStatement stmt = connection.prepareStatement("CALL UpdateUser(?, ?, ?, ?)")) {
                stmt.setString(1, user.getUsername());
                stmt.setString(2, user.getName());
                stmt.setString(3, user.getGroup().getGroupName());
                stmt.setString(4, user.statusProperty().get());
                stmt.executeUpdate();
                System.out.println("User " + user.getUsername() + " updated successfully.");
                user.setModified(false);
            } catch (SQLException e) {
                Main.getLogger().severe("Failed to update user: " + user.getUsername());
                e.printStackTrace();
            }
        }
    }

    public void deleteUser(User user, String currentUser) {
        try (PreparedStatement stmt = connection.prepareStatement("CALL DeleteUser(?, ?, ?)")) {
            stmt.setString(1, user.getUsername());
            stmt.setString(2, user.getName());
            stmt.setString(3, currentUser);
            stmt.executeUpdate();
            Main.getLogger().info("User " + user.getUsername() + " deleted successfully.");
        } catch (SQLException e) {
            Main.getLogger().severe("Failed to delete user: " + user.getUsername());
            e.printStackTrace();
        }
    }
}
