package com.awesomeshot5051.separatedFiles;

import com.awesomeshot5051.*;
import com.awesomeshot5051.separatedFiles.session.*;

import javax.swing.*;
import java.nio.charset.*;
import java.security.*;
import java.sql.*;
import java.util.*;

public class PasswordHasher {
    public String password;
    public String hashedPassword;
    private final Connection connection;


    public PasswordHasher(String password) {
        this.password = password;
        hashedPassword = hashPassword(password);
        this.connection = Main.getConnection();
    }

    public PasswordHasher() {
        this.connection = SessionManager.getConnection();
    }

    public String getHashedPassword() {
        return hashedPassword;
    }

    public String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hashedPassword = md.digest(password.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hashedPassword) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public String hashPassword() {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hashedPassword = md.digest(password.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hashedPassword) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }


    public String getPassword() {
        String username = SessionManager.getUsername();
        String name = SessionManager.getName();
        String query = "SELECT password FROM users WHERE username = ? AND name = ?";
        assert connection != null;
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, username);
            statement.setString(2, name);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                password = resultSet.getString("password");
            } else {
                // If no result is found, you can throw an exception or return a default group
                throw new SQLException("No password found for user: " + username);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return password;
    }

    public String generateRandomSalt() {
        SecureRandom random = new SecureRandom();
        byte[] saltBytes = new byte[16]; // 16 bytes = 128 bits
        random.nextBytes(saltBytes);
        StringBuilder sb = new StringBuilder();
        for (byte b : saltBytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    public String getUnsaltedHashedPassword() {
        return hashedPassword;
    }

    private String salt;

    public String generateSaltedHashedPassword() {
        salt = generateRandomSalt();
        return hashPassword(password) + salt;
    }

    public String getSalt() {
        return salt;
    }

    public String getSalt(String username) {
        String getSaltQuery = "SELECT salt FROM users WHERE username=?";
        String existingSalt = null;
        try (
                PreparedStatement statement = connection.prepareStatement(getSaltQuery)
        ) {
            statement.setString(1, username);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                existingSalt = resultSet.getString("salt");
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(
                    null,
                    "An error occured\nError code " + Arrays.toString(e.getStackTrace()),
                    "Failed",
                    JOptionPane.ERROR_MESSAGE
            );
        }
        return existingSalt;
    }

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        String password = in.nextLine();
        System.out.println(new PasswordHasher(password).hashPassword(password));
    }
}
