package com.awesomeshot5051.separatedFiles.accesskey;

import com.awesomeshot5051.*;
import com.awesomeshot5051.separatedFiles.*;
import com.awesomeshot5051.separatedFiles.Styler.*;
import com.awesomeshot5051.separatedFiles.security.*;
import com.awesomeshot5051.separatedFiles.session.*;
import javafx.geometry.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.*;

import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.*;
import java.sql.*;
import java.time.*;
import java.util.*;

public class AccessKeyVerification {
    private final Stage stage;
    private final Connection connection;

    public AccessKeyVerification() {
        this.stage = new Stage();
        this.connection = SessionManager.getConnection();
    }


    public void accessKeyExists() {
        if (SessionManager.getGroupType().getGroupName().equalsIgnoreCase("superadmin")) {
            SessionManager.getUser().setAccessKeyValid(true);
            return;
        }

        try {
            // Step 1: Construct vault paths
            String userHome = System.getProperty("user.home");
            Path vaultDir = Paths.get(userHome, ".javaLoginGUI", "users", SessionManager.getUsername(), ".vault");
            Path accessKeyDir = vaultDir.resolve(".accessKey");
            Files.createDirectories(accessKeyDir); // Ensure .accessKey folder exists

            Path decryptedTemp = Files.createTempFile("decrypted-accesskey", ".tmp");
            String originalFileName = "accesskey.key";

            FileEncryption encryption = new FileEncryption();
            String encryptedFileName = encryption.getEncryptedFileName(originalFileName);

            Path sourceEncryptedFile = vaultDir.resolve(encryptedFileName);
            Path targetEncryptedFile = accessKeyDir.resolve(encryptedFileName);

            // Step 2: If file is in .accessKey but not yet in .vault, move it
            if (Files.exists(sourceEncryptedFile) && !Files.exists(targetEncryptedFile)) {
                Files.move(sourceEncryptedFile, targetEncryptedFile);
            }

            // Step 3: Attempt decryption
            boolean success = encryption.decryptFile(encryptedFileName, decryptedTemp);
            if (!success) {
                SessionManager.getUser().setAccessKeyValid(false);
                Files.deleteIfExists(decryptedTemp);
                return;
            }

            // Step 4: Read and validate key
            String key = Files.readString(decryptedTemp).trim();
            Files.deleteIfExists(decryptedTemp);

            if (key.startsWith("temp-")) {
                if (checkTemporaryAccessKeyExpiration(targetEncryptedFile)) {
                    CallableStatement statement = connection.prepareCall("call removeAccessKey(?)");
                    statement.setString(1, key);
                    statement.execute();
                }
            }

            boolean valid = isValid(key);
            SessionManager.getUser().setAccessKeyValid(valid);

        } catch (Exception e) {
            Main.getErrorLogger().silentlyHandle(e);
            SessionManager.getUser().setAccessKeyValid(false);
        }
    }


    public boolean checkTemporaryAccessKeyExpiration(Path keyFilePath) {
        try {
            // Read creation time
            BasicFileAttributes attrs = Files.readAttributes(keyFilePath, BasicFileAttributes.class);
            FileTime creationTime = attrs.creationTime();
            Instant createdInstant = creationTime.toInstant();
            Instant now = Instant.now();

            // Check if older than 7 days
            if (Duration.between(createdInstant, now).toDays() >= 7) {
                Files.deleteIfExists(keyFilePath);
                SessionManager.getUser().setAccessKeyValid(false);

                Alert alert = new Alert(Alert.AlertType.WARNING);
                FXAlertStyler.style(alert);
                alert.setTitle("Access Key Expired");
                alert.setHeaderText("Temporary Access Expired");
                alert.setContentText("Your access key has expired. Please enter a new one.");
                alert.showAndWait();
                return true;
            }
        } catch (IOException e) {
            Main.getErrorLogger().silentlyHandle(e);
            // You can also show a user-friendly alert here
        }
        return false;
    }


    public void writeAccessKey(String accessKey) {
        try {
            // Step 1: Determine vault paths
            String userHome = System.getProperty("user.home");
            Path vaultDir = Paths.get(userHome, ".javaLoginGUI", "users", SessionManager.getUsername(), ".vault");
            Path accessKeyDir = vaultDir.resolve(".accessKey");
            Files.createDirectories(accessKeyDir); // Ensure .accessKey folder exists

            // Step 2: Create temporary plaintext access key file
            Path tempFile = Files.createTempFile("accesskey", ".tmp");
            Files.writeString(tempFile, accessKey);

            // Step 3: Encrypt the file
            FileEncryption encryption = new FileEncryption();
            String encryptedFileName = encryption.getEncryptedFileName("accesskey.key");
            boolean success = encryption.encryptFile(tempFile, "accesskey.key");
            Files.deleteIfExists(tempFile);

            if (!success) {
                throw new RuntimeException("Encryption failed.");
            }

            // Step 4: Move encrypted file to .accessKey folder
            Path sourceEncryptedFile = vaultDir.resolve(encryptedFileName);
            Path targetEncryptedFile = accessKeyDir.resolve(encryptedFileName);
            Files.move(sourceEncryptedFile, targetEncryptedFile);

            // Step 5: Update session
            SessionManager.getUser().setAccessKeyValid(true);
            SessionManager.setAccessKeyValid(true);

        } catch (Exception e) {
            Main.getErrorLogger().handleException("Failed to write and encrypt access key.", e);
            throw new RuntimeException("Failed to write and encrypt access key.", e);
        }
    }


    public void AccessKeyVerificationWindow() {
        // Setup layout
        VBox layout = new VBox(10);
        layout.setPadding(new Insets(20));
//        layout.setStyle("-fx-background-color: #f4f4f4;");

        Label promptLabel = new Label("Enter your access key:");
        TextField accessKeyField = new TextField();
        accessKeyField.setPromptText("XXXX-XXXX-XXXX-XXXX");

        Button verifyButton = new Button("Verify");
        final boolean[] isAccessKeyValid = {false};

        verifyButton.setOnAction(e -> {
            try {
                if (isValid(accessKeyField.getText())) {
                    isAccessKeyValid[0] = true;
                    stage.close();
                    new VaultManagementScreen().VaultManagementMainGUI();
                } else {
                    Alert alert = new Alert(Alert.AlertType.WARNING);
                    FXAlertStyler.style(alert);
                    alert.setTitle("Access Key Verification");
                    alert.setHeaderText("Access Key Invalid");
                    alert.setContentText("Your access key has either expired, or doesn't exist. \nPlease enter a different one.\nIf you believe this is a mistake, \nplease open an issue on the GitHub.");
                    alert.showAndWait();
                }
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
        });
        Button redeemFreeTrial = new Button("Redeem Free Trial");
        FreeTrialAccessKey freeTrialAccessKey = new FreeTrialAccessKey();
        if (!freeTrialAccessKey.hasRedeemedTrial()) {
            redeemFreeTrial.setOnAction(e -> {
                freeTrialAccessKey.redeemTrial();
            });
        } else {
            redeemFreeTrial.setDisable(true);
//            redeemFreeTrial.setStyle("-fx-background-color: #d3d3d3;");
            redeemFreeTrial.setText("Free Trial Redeemed");
        }
        layout.getChildren().addAll(promptLabel, accessKeyField, redeemFreeTrial, verifyButton);

        Scene scene = new Scene(layout, 315, 250);
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/styles/Styles.css")).toExternalForm());
        stage.setScene(scene);
        stage.setTitle("Access Key Verification");
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.getIcons().add(IconFinder.findIcon());
        // Blocks until closed
        stage.showAndWait();

        if (isAccessKeyValid[0]) {
            writeAccessKey(accessKeyField.getText());
            new VaultManagementScreen().VaultManagementMainGUI();
        } else {
            new MainScreen(SessionManager.getGroupType(), SessionManager.getStatus(), SessionManager.getUsername(), SessionManager.getName(), SessionManager.getConnection(), Main.getStage());
        }
    }

    private boolean isValid(String accessKey) throws SQLException {
        String key = accessKey.trim();

        // Pattern 1: XXXX-XXXX-XXXX-XXXX (permanent key)
        String permanentPattern = "^[A-Z0-9]{4}(-[A-Z0-9]{4}){3}$";

        // Pattern 2: temp-xxxxxxxx-xxxxxxxx-xxxxxxxx-xxxxxxxx (temporary key)
        String temporaryPattern = "^temp-([a-f0-9]{8}-){3}[a-f0-9]{8}$";

        if (key.matches(permanentPattern) || key.matches(temporaryPattern)) {
            return keyExistsInDB(key);
        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            FXAlertStyler.style(alert);
            alert.setTitle("Invalid Access Key");
            alert.setHeaderText("Access Key Format Error");
            alert.setContentText(
                    """
                            Supported formats:
                            
                            • XXXX-XXXX-XXXX-XXXX (uppercase letters and digits)
                            • temp-xxxxxxxx-xxxxxxxx-xxxxxxxx-xxxxxxxx (temporary key, lowercase hex)"""
            );
            alert.showAndWait();
            return false;
        }
    }

    public boolean keyExistsInDB(String key) throws SQLException {
        try {
            CallableStatement statement = connection.prepareCall("{ call checkAccessKeyExists(?, ?) }");

            statement.setString(1, key); // Input parameter
            statement.registerOutParameter(2, java.sql.Types.BOOLEAN); // Output parameter

            statement.execute();
            if (statement.getBoolean(2)) {
                CallableStatement statement2 = connection.prepareCall("{ call createAccessKeyUserLink(?, ?, ?) }");
                statement2.setString(1, SessionManager.getUsername());
                statement2.setString(2, SessionManager.getName());
                statement2.setString(3, key);
                statement2.execute();
            }
            return statement.getBoolean(2);
        } catch (SQLException e) {
            Main.getErrorLogger().handleException("Error in checkAccessKeyExists", e);
        }
        return false;
    }

}
