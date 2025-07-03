package com.awesomeshot5051.separatedFiles.Messages;

import com.awesomeshot5051.Main;
import com.awesomeshot5051.separatedFiles.Styler.FXAlertStyler;
import com.awesomeshot5051.separatedFiles.session.SessionManager;
import com.awesomeshot5051.separatedFiles.userManagement.User;
import javafx.application.Platform;
import javafx.scene.control.Alert;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;

public class MessageHandler {

    private static final String BASE_PATH = System.getProperty("user.home") + File.separator +
            ".javaLoginGUI" + File.separator + "users" + File.separator;

    /**
     * Sends a message to a target user.
     *
     * @param user    The target user.
     * @param message The message content.
     */
    public static void sendMessage(User user, String message) {
        File messageFile = new File(BASE_PATH + user.getUsername() + File.separator + "message.temp");
        messageFile.getParentFile().mkdirs();// Ensure directories exist
        try (FileWriter writer = new FileWriter(messageFile)) {
            writer.write(message);
        } catch (IOException e) {
            Main.getErrorLogger().handleException("Failed to send message to user: " + user.getUsername(), e);
        }
    }

    /**
     * Checks if the currently logged-in user has a message, and if so, displays it.
     * Call this method after a successful login.
     */
    public static void checkForMessage() {
        String username = SessionManager.getUsername();
        File messageFile = new File(BASE_PATH + username + File.separator + "message.temp");

        if (messageFile.exists()) {
            try {
                String message = Files.readString(messageFile.toPath());
                showAlert("You have an unread message:\n\n" + message);
                Files.delete(messageFile.toPath()); // Remove after reading
            } catch (IOException e) {
                Main.getErrorLogger().handleException("Failed to read or delete message for " + username, e);
            }
        }
    }

    private static void showAlert(String message) {
        Platform.runLater(() -> {
            Alert alert = new javafx.scene.control.Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Message for You");
            FXAlertStyler.style(alert);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }
}
