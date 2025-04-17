package com.awesomeshot5051.separatedFiles.userValidation;

import com.awesomeshot5051.*;
import com.awesomeshot5051.separatedFiles.session.*;

import java.sql.*;

public class StatusChecker {
    private final Connection connection;
    private final String username;
    private final String status;

    public StatusChecker(String username) {
        this.connection = Main.getConnection();
        this.username = username;
        this.status = getStatus();
    }

    public StatusChecker(String username, String password) {
        this.connection = Main.getConnection();
        this.username = username;
        this.status = getStatus(password);
    }


    private String getStatus() {
        String query = "call getUserStatus(?,?)";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, username);
            statement.setString(2, SessionManager.getName());
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getString("Status");
            }
            return null;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private String getStatus(String password) {
        String query = "call getUserStatus(?,?)";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, username);
            statement.setString(2, password);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getString("Status");
            }
            return null;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }


    public boolean isDisabled() {
        return status.equals("Disabled");
    }
}
