package com.awesomeshot5051.separatedFiles.systemConfiguration.database;

import com.awesomeshot5051.Main;
import com.awesomeshot5051.separatedFiles.session.SessionManager;
import com.awesomeshot5051.separatedFiles.userManagement.User;
import com.awesomeshot5051.separatedFiles.userManagement.UserList;

import java.sql.*;

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
//                System.out.println("User " + user.getUsername() + " updated successfully.");
                user.setModified(false);
            } catch (SQLException e) {
                Main.getLogger().severe("Failed to update user: " + user.getUsername());
            }
        }
    }

    public void deleteUser(User user, String currentUser) throws SQLException, ClassNotFoundException {
        try (PreparedStatement stmt = connection.prepareStatement("CALL DeleteUser(?, ?, ?)")) {
            stmt.setString(1, user.getUsername());
            stmt.setString(2, user.getName());
            stmt.setString(3, currentUser);
            stmt.executeUpdate();
            Main.getLogger().info("User " + user.getUsername() + " deleted successfully.");
        }

    }

    public static UserList getAdmins() {
        UserList admins = new UserList();
        try (CallableStatement cs = SessionManager.getConnection().prepareCall("{ call getAdmins()}")) {
            try (ResultSet rs = cs.executeQuery()) {
                while (rs.next()) {
                    admins.add(new User(rs.getString(1), rs.getString(2), rs.getString(3), rs.getString(4)));
                }
            }
        } catch (SQLException ex) {
            Main.getErrorLogger().handleException("Error getting admins for user", ex);
        }
        return admins.isEmpty() ? null : admins;
    }
}
