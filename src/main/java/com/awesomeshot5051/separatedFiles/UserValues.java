package com.awesomeshot5051.separatedFiles;

import com.awesomeshot5051.*;
import com.awesomeshot5051.separatedFiles.group.*;

import java.sql.*;

public class UserValues {

    private final IGroup IGroupType;
    private final String status;
    private final String username;
    private final String name;
    private final Connection connection;
    private String password;

    public UserValues(String username, String password) throws SQLException {
        this.connection = Main.getConnection();
        this.IGroupType = getGroup(username);
        this.status = getStatus(username);
        this.username = username;
        this.name = getName(username, password);
    }


    private IGroup getGroup(String username) throws SQLException {
        String groupName;
        String query = "SELECT `group` FROM users WHERE username = ?";
        assert connection != null;

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, username);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                groupName = resultSet.getString("group");
            } else {
                // If no result is found, you can throw an exception or return a default group
                throw new SQLException("No group found for user: " + username);
            }
        }

        // Return the appropriate IGroup instance based on the groupName retrieved from the database
        // You can throw an exception or return a default group for unknown group names
        return switch (groupName) {
            case "Admin" -> new AdminIGroup();
            case "Standard" -> new StandardIGroup();
            case "SuperAdmin" -> new SuperAdminIGroup();
            case "Default" -> new DefaultIGroup();
            default -> throw new SQLException("Unknown group: " + groupName);
        };
    }

    public String getStatus(String username) {
        String query = "SELECT status FROM users WHERE username = ?";
        String Status = "";
        try {
            assert connection != null;
            try (PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setString(1, username);
                ResultSet resultSet = statement.executeQuery();

                while (resultSet.next()) {
                    Status = resultSet.getString("status");
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return Status;
    }

    private String getName(String username, String password)
            throws SQLException {
        String user = "";
        String query =
                "SELECT name FROM users WHERE username = ? AND password=?";
        assert connection != null;
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, username);
            statement.setString(2, password);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                user = resultSet.getString("name");
            }
        }
        return user;
    }

    public IGroup getGroupType() {
        return IGroupType;
    }

    public String getStatus() {
        return status;
    }

    public String getName() {
        return name;
    }

    public String getUsername() {
        return username;
    }

}
