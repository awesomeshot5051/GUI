package com.awesomeshot5051.separatedFiles.security;

import com.awesomeshot5051.*;
import com.awesomeshot5051.separatedFiles.*;
import com.awesomeshot5051.separatedFiles.session.*;

import java.nio.charset.*;
import java.security.*;
import java.sql.*;
import java.util.*;

public class PublicKeyManager {
    private static final int SALT_LENGTH = 16;
    private final Connection connection;
    private final String username;

    public PublicKeyManager() {
        this.connection = SessionManager.getConnection();
        this.username = SessionManager.getUsername();
    }

    public void rotatePublicKey() {
        PasswordHasher hasher = new PasswordHasher();
        String hashedPassword = hasher.getPassword();
        try {
            // Step 1: Generate random salt
            String salt = hasher.generateRandomSalt();

            // Step 2: Create the public key hash
            String publicKey = hash(username + hashedPassword + salt);

            // Step 3: Store (or update) the public key and salt in the database
            try (PreparedStatement stmt = connection.prepareStatement(
                    "call setPublicKey(?,?,?,?)")) {
                stmt.setString(1, username);
                stmt.setString(2, SessionManager.getName());
                stmt.setString(3, publicKey);
                stmt.setString(4, salt);
                stmt.executeUpdate();
            }
        } catch (Exception e) {
            Main.getErrorLogger().handleException("Failed to rotate public key for user: " + username, e);
            throw new RuntimeException("Failed to rotate public key for user: " + username, e);
        }
    }

    /**
     * Fetches both public_key and public_salt in one shot.
     */
    public PublicKeyData getPublicKey() throws SQLException {
        String sql = "{ call getPK(?,?) }";
        try (CallableStatement cstmt = connection.prepareCall(sql)) {
            cstmt.setString(1, username);
            cstmt.setString(2, SessionManager.getName());
            boolean hasResult = cstmt.execute();
            while (!hasResult && cstmt.getUpdateCount() != -1) {
                hasResult = cstmt.getMoreResults();
            }
            if (hasResult) {
                try (ResultSet rs = cstmt.getResultSet()) {
                    if (rs.next()) {
                        return new PublicKeyData(
                                rs.getString("public_key"),
                                rs.getString("public_salt")
                        );
                    }
                }
            }
        }
        return null;  // or throw new SQLException("No key row returned");
    }

    private String hash(String input) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hashBytes = digest.digest(input.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(hashBytes);
    }
}