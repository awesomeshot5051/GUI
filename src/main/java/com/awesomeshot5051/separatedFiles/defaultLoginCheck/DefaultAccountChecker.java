package com.awesomeshot5051.separatedFiles.defaultLoginCheck;

import com.awesomeshot5051.separatedFiles.userManagement.ManageUserStatus;
import com.awesomeshot5051.separatedFiles.userManagement.User;
import javafx.scene.control.Alert;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.SQLException;

public class DefaultAccountChecker {

    private static final String FLAG_FILE_PATH = System.getProperty("user.home") + "/.javaLoginGUI/default_account_used.flag";

    public static boolean hasUsedDefaultAccount() {
        return new File(FLAG_FILE_PATH).exists();
    }

    public static void markDefaultAccountUsed() {
        File file = new File(FLAG_FILE_PATH);
        file.getParentFile().mkdirs(); // Create directory if it doesn't exist
        try (FileWriter writer = new FileWriter(file)) {
            writer.write("true");
        } catch (IOException e) {
            e.printStackTrace(); // Or handle however you like
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
