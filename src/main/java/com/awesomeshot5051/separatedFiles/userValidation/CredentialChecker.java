package com.awesomeshot5051.separatedFiles.userValidation;

import com.awesomeshot5051.Main;
import com.awesomeshot5051.separatedFiles.PasswordHasher;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class CredentialChecker {

    private final Connection connection;
    private String username;
    private String password;

    public CredentialChecker() {
        this.connection = Main.getConnection();
    }

    // Check if credentials (username & hashed password) match in the database
    private boolean checkCredentials(String username, String hashedPassword)
            throws SQLException {
        String query = "SELECT * FROM users WHERE username = ? AND password = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, username);
            statement.setString(2, hashedPassword);
            ResultSet resultSet = statement.executeQuery();
            return resultSet.next();
        }
    }

    public boolean validateCredentials(String username, String hashedPassword) throws SQLException {
        PasswordHasher passwordHasher = new PasswordHasher(hashedPassword);
        return checkCredentials(username, passwordHasher.getFullyHashedPassword(username));
    }

}
