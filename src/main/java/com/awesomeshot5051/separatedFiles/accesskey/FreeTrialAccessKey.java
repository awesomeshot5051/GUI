package com.awesomeshot5051.separatedFiles.accesskey;

import com.awesomeshot5051.separatedFiles.security.*;
import com.awesomeshot5051.separatedFiles.session.*;
import com.sun.jna.platform.win32.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.stage.*;

import java.awt.*;
import java.awt.datatransfer.*;
import java.nio.file.*;
import java.sql.*;

public class FreeTrialAccessKey {

    private static final String REGISTRY_PATH = "Software\\.javaLoginGUI";
    private static final String REGISTRY_KEY = "trialRedeemed";
    private static final String TRIAL_KEY_FILENAME = "accesskey.key";
    private final Connection connection;

    public FreeTrialAccessKey() {
        this.connection = SessionManager.getConnection();
    }

    public void redeemTrial() {
        try {
            if (hasRedeemedTrial()) {
                showInfoPopup("Trial Already Redeemed", "You have already redeemed your free trial.");
                return;
            }

            // Generate key
            String tempKey = generateTempAccessKey();

            // Write to temp file
            Path tempFile = Files.createTempFile("temp-accesskey-", ".tmp");
            Files.writeString(tempFile, tempKey);

            // Encrypt and store
            Path vaultPath = Paths.get(System.getProperty("user.home"),
                    ".javaLoginGUI", "users", SessionManager.getUsername(), ".vault");
            Files.createDirectories(vaultPath);
            new FileEncryption().encryptFile(tempFile, TRIAL_KEY_FILENAME);
            Files.deleteIfExists(tempFile);

            // Set registry flag
            markTrialRedeemed();

            // Show popup with copy functionality
            showKeyPopup(tempKey);
            CallableStatement statement = connection.prepareCall("Call createTemporaryAccessKey(?)");
            statement.setString(1, tempKey);
            statement.execute();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean hasRedeemedTrial() {
        return Advapi32Util.registryValueExists(
                WinReg.HKEY_CURRENT_USER, REGISTRY_PATH, REGISTRY_KEY
        ) && Advapi32Util.registryGetStringValue(
                WinReg.HKEY_CURRENT_USER, REGISTRY_PATH, REGISTRY_KEY
        ).equalsIgnoreCase("true");
    }

    private void markTrialRedeemed() {
        Advapi32Util.registryCreateKey(WinReg.HKEY_CURRENT_USER, REGISTRY_PATH);
        Advapi32Util.registrySetStringValue(
                WinReg.HKEY_CURRENT_USER,
                REGISTRY_PATH,
                REGISTRY_KEY,
                "true"
        );
    }

    private String generateTempAccessKey() {
        return "temp-" +
                generateHexBlock(8) + "-" +
                generateHexBlock(8) + "-" +
                generateHexBlock(8) + "-" +
                generateHexBlock(8);
    }

    private String generateHexBlock(int length) {
        StringBuilder hex = new StringBuilder();
        for (int i = 0; i < length; i++) {
            int digit = (int) (Math.random() * 16);
            hex.append(Integer.toHexString(digit));
        }
        return hex.toString();
    }

    private void copyToClipboard(String text) {
        StringSelection selection = new StringSelection(text);
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(selection, null);
    }

    private void showKeyPopup(String tempKey) {
        Stage popup = new Stage();
        popup.initModality(Modality.APPLICATION_MODAL);
        popup.setTitle("Your Free Trial Access Key");

        Label keyLabel = new Label(tempKey);
        Button copyAndCloseBtn = new Button("Copy and Close");
        copyAndCloseBtn.setOnAction(e -> {
            copyToClipboard(tempKey);
            popup.close();
        });

        VBox layout = new VBox(15, new Label("Here is your free trial key.\nIt will be valid for 7 days:"), keyLabel, copyAndCloseBtn);
        layout.setStyle("-fx-padding: 20; -fx-alignment: center;");
        popup.setScene(new Scene(layout));
        popup.showAndWait();
    }

    private void showInfoPopup(String title, String message) {
        Stage popup = new Stage();
        popup.initModality(Modality.APPLICATION_MODAL);
        popup.setTitle(title);
        VBox layout = new VBox(10, new Label(message), new Button("OK"));
        ((Button) layout.getChildren().get(1)).setOnAction(e -> popup.close());
        layout.setStyle("-fx-padding: 20; -fx-alignment: center;");
        popup.setScene(new Scene(layout));
        popup.showAndWait();
    }
}
