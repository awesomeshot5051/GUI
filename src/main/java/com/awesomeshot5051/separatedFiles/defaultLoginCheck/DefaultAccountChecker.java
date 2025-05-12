package com.awesomeshot5051.separatedFiles.defaultLoginCheck;

import com.awesomeshot5051.*;
import com.awesomeshot5051.separatedFiles.userManagement.*;
import javafx.scene.control.*;

import java.io.*;
import java.sql.*;

public class DefaultAccountChecker {

    private static final String FLAG_FILE_PATH = System.getProperty("user.home") + "/.javaLoginGUI/default_account_used.flag";

    public static boolean hasUsedDefaultAccount() {
        return new File(FLAG_FILE_PATH).exists();
    }

    public static void markDefaultAccountUsed() {
        File file = new File(FLAG_FILE_PATH);
        final boolean worked = file.getParentFile().mkdirs(); // Create directory if it doesn't exist
        try (FileWriter writer = new FileWriter(file)) {
            writer.write("true");
        } catch (IOException e) {
            Main.getErrorLogger().handleException("Error marking default account as used", e);
        }
    }

    public static void checkAndNotifyIfDefaultNeeded() throws SQLException {
        if (!hasUsedDefaultAccount()) {
            // Enable the default account
            new ManageUserStatus().setUserStatus(new User("default", "admin", "default", "Disabled"), "Enabled");

            // Show popup
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Default Account Enabled");
            alert.setHeaderText(null);
            alert.setContentText("The default account has been enabled.\nUsername: admin\nPassword: admin");
            alert.showAndWait();
        }
    }
}