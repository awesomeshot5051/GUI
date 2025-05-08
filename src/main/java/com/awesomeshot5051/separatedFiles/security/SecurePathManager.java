package com.awesomeshot5051.separatedFiles.security;

import com.awesomeshot5051.separatedFiles.session.*;

import java.io.*;
import java.nio.file.*;

public class SecurePathManager {
    protected final Path BASE_DIR;
    protected final Path VAULT_DIR;
    protected final Path VAULT_KEYS;
    protected final Path PRIVATE_KEY;

    public SecurePathManager() {
        this.BASE_DIR = Paths.get(
                System.getProperty("user.home"),
                ".javaLoginGUI",
                "users",
                SessionManager.getUsername()
        );
        this.VAULT_DIR = BASE_DIR.resolve(".vault");
        this.VAULT_KEYS = BASE_DIR.resolve(".vault_keys");
        this.PRIVATE_KEY = BASE_DIR.resolve("private.key");
    }


    public Path getBaseDir() {
        return BASE_DIR;
    }

    public Path getVaultDir() {
        return VAULT_DIR;
    }

    public Path getVaultKeysDir() {
        return VAULT_KEYS;
    }

    public Path getPrivateKeyPath() {
        return PRIVATE_KEY;
    }

    /**
     * Ensure all needed dirs exist
     **/
    public void ensureAllDirectoriesExist() throws IOException {
        Files.createDirectories(BASE_DIR);
        Files.createDirectories(VAULT_DIR);
        Files.createDirectories(VAULT_KEYS);
    }
}
