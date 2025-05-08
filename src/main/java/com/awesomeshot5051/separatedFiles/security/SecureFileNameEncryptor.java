package com.awesomeshot5051.separatedFiles.security;

import com.google.crypto.tink.*;
import com.google.crypto.tink.daead.*;

import java.io.*;
import java.nio.charset.*;
import java.nio.file.*;
import java.nio.file.attribute.*;
import java.security.*;
import java.util.*;

/**
 * Secure filename encryption using AES-SIV (DeterministicAead).
 */
public class SecureFileNameEncryptor {


    protected final Path KEY_DIR;
    protected final Path KEY_FILE;
    private static final byte[] AD = "filename-lock".getBytes(StandardCharsets.UTF_8);

    private final DeterministicAead daead;

    static {
        // Register all AEAD/DAEAD Tink primitives
        try {
            DeterministicAeadConfig.register();
        } catch (GeneralSecurityException e) {
            throw new RuntimeException(e);
        }
    }

    public SecureFileNameEncryptor() throws Exception {
        SecurePathManager scp = new SecurePathManager();
        scp.ensureAllDirectoriesExist();
        this.KEY_DIR = scp.getVaultKeysDir();
        this.KEY_FILE = KEY_DIR.resolve("filename_key.json");
        Files.createDirectories(KEY_DIR);

        KeysetHandle handle;
        if (Files.exists(KEY_FILE)) {
            try (InputStream in = Files.newInputStream(KEY_FILE)) {
                String keysetJson = new String(in.readAllBytes(), StandardCharsets.UTF_8);
                handle = TinkJsonProtoKeysetFormat.parseKeyset(keysetJson, InsecureSecretKeyAccess.get());
            }
        } else {
            // Build parameters for AES-SIV (64-byte key, TINK variant)
            AesSivParameters params = AesSivParameters.builder()
                    .setKeySizeBytes(64)
                    .setVariant(AesSivParameters.Variant.TINK)
                    .build();

            // Generate a new keyset with AES-SIV primary
            handle = KeysetHandle.newBuilder()
                    .addEntry(
                            KeysetHandle.generateEntryFromParameters(params)
                                    .withRandomId()
                                    .makePrimary()
                    )
                    .build();

            // Persist in JSON format
            try (OutputStream out = Files.newOutputStream(KEY_FILE, StandardOpenOption.CREATE_NEW)) {
                String serializedKeyset = TinkJsonProtoKeysetFormat.serializeKeyset(handle, InsecureSecretKeyAccess.get());
                out.write(serializedKeyset.getBytes(StandardCharsets.UTF_8));

                // Set file permissions if the file system supports POSIX permissions
                try {
                    if (Files.getFileStore(KEY_FILE).supportsFileAttributeView(PosixFileAttributeView.class)) {
                        Files.setPosixFilePermissions(KEY_FILE, EnumSet.of(
                                PosixFilePermission.OWNER_READ, PosixFilePermission.OWNER_WRITE
                        ));
                    }
                } catch (IOException e) {
                    // Log the error but continue execution
                    System.err.println("Warning: Could not set file permissions: " + e.getMessage());
                }
            }
        }

        // Get the DeterministicAead primitive via the non-deprecated API
        this.daead = handle.getPrimitive(RegistryConfiguration.get(), DeterministicAead.class);
    }

    /**
     * Deterministically encrypt the filename (including extension), URL-safe Base64 output.
     */
    public String encryptFileName(String originalName) throws GeneralSecurityException {
        byte[] ct = daead.encryptDeterministically(
                originalName.getBytes(StandardCharsets.UTF_8),
                AD
        );
        return Base64.getUrlEncoder().withoutPadding().encodeToString(ct);
    }

    /**
     * Decrypt a filename back to its original form.
     */
    public String decryptFileName(String encryptedName) throws GeneralSecurityException {
        byte[] ct = Base64.getUrlDecoder().decode(encryptedName);
        byte[] pt = daead.decryptDeterministically(ct, AD);
        return new String(pt, StandardCharsets.UTF_8);
    }
}