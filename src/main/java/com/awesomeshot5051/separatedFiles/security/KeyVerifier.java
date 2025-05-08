package com.awesomeshot5051.separatedFiles.security;

import com.awesomeshot5051.separatedFiles.*;
import com.awesomeshot5051.separatedFiles.session.*;

import java.nio.charset.*;
import java.security.*;
import java.sql.*;
import java.util.*;

public class KeyVerifier {
    private final String passwordHash;
    private final String databaseSalt;
    private final Connection connection;

    public KeyVerifier(String passwordHash, String databaseSalt) {
        this.connection = SessionManager.getConnection();
        PasswordHasher hasher = new PasswordHasher();
        this.passwordHash = hasher.getPassword();
        this.databaseSalt = hasher.getSalt(SessionManager.getUsername());
    }

    public boolean verifyKeys() {
        try {
            PublicKeyManager pm = new PublicKeyManager();
            PublicKeyData data = pm.getPublicKey();

            // if no data, rotate once and retry
            if (data == null || data.publicKey() == null || data.publicSalt() == null) {
                pm.rotatePublicKey();
                data = pm.getPublicKey();
                if (data == null) {
                    System.err.println("No public key data found for user.");
                    return false;
                }
            }

            String publicKeyFromDb = data.publicKey();
            String publicSalt = data.publicSalt();

            // 2. Reconstruct the expected public key
            String expectedPublicKey = hash(
                    SessionManager.getUsername() + passwordHash + publicSalt
            );

            // 3. Compare
            return expectedPublicKey.equals(publicKeyFromDb);

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }


    private String hash(String input) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(hash);
    }
}
