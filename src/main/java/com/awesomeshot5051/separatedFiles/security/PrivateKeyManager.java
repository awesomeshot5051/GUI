package com.awesomeshot5051.separatedFiles.security;

import com.awesomeshot5051.separatedFiles.*;
import com.awesomeshot5051.separatedFiles.session.*;

import java.io.*;
import java.nio.charset.*;
import java.nio.file.*;
import java.security.*;
import java.util.*;

public class PrivateKeyManager {
    private final Path privateKeyPath;

    public PrivateKeyManager() {
        SecurePathManager scp = new SecurePathManager();
        this.privateKeyPath = scp.getPrivateKeyPath();
        try {
            scp.ensureAllDirectoriesExist();
        } catch (IOException e) {
            throw new RuntimeException("Failed to set up secure directories", e);
        }
    }

    public String loadPrivateKey() throws IOException {
        try {
            return Files.readString(privateKeyPath, StandardCharsets.UTF_8);
        } catch (NoSuchFileException e) {
            return generateAndStorePrivateKey();
        }
    }

    public String generateAndStorePrivateKey() throws IOException {
        String appUsername = SessionManager.getUsername();
        String passwordHash = new PasswordHasher().getPassword();
        String salt = UUID.randomUUID().toString();

        String privateKey = hash(appUsername + passwordHash + salt);
        Files.writeString(privateKeyPath, privateKey, StandardCharsets.UTF_8, StandardOpenOption.CREATE_NEW);
        return privateKey;
    }

    private String hash(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (Exception e) {
            throw new RuntimeException("Hashing failed", e);
        }
    }
}
