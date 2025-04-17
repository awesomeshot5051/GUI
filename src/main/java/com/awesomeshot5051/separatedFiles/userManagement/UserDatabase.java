package com.awesomeshot5051.separatedFiles.userManagement;

import java.sql.*;

public class UserDatabase {

    private Connection connection;

    public UserDatabase(Connection connection) {
        this.connection = connection;
    }

    // Fetch all users from the database
    public ResultSet getAllUsers() throws SQLException {
        String query = "SELECT name, username, `group`, status FROM users";
        PreparedStatement statement = connection.prepareStatement(query);
        return statement.executeQuery();
    }

    // Update the user group in the database
    public boolean updateUserGroup(String username, String newGroup) {
        String query = "UPDATE users SET `group` = ? WHERE username = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, newGroup);
            statement.setString(2, username);
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
