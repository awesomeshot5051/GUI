package com.awesomeshot5051.separatedFiles.logs;

import com.awesomeshot5051.*;
import com.awesomeshot5051.separatedFiles.group.*;
import com.awesomeshot5051.separatedFiles.session.*;
import com.awesomeshot5051.separatedFiles.userManagement.*;
import javafx.scene.control.*;
import javafx.scene.control.Alert.*;
import javafx.stage.*;

import java.io.*;

public class LogClearer {

    private final String LOG_FILE_PATH;

    public LogClearer(String logFilePath) {
        this.LOG_FILE_PATH = logFilePath;
    }

    public void clearLogs(Stage stage) {
        Alert confirmDialog = new Alert(AlertType.CONFIRMATION);
        confirmDialog.setTitle("Clear Logs");
        confirmDialog.setHeaderText("Are you sure you want to clear the logs?");
        confirmDialog.setContentText("This action cannot be undone.\n(If you are not Super Admin your account will be disabled)");

        confirmDialog.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                // Confirm clear, show another dialog explaining why logs are important
                Alert infoDialog = new Alert(AlertType.INFORMATION);
                infoDialog.setTitle("Log Information");
                infoDialog.setHeaderText("Log files are important for troubleshooting and auditing.");
                infoDialog.setContentText("Proceed with clearing logs after understanding the importance.");
                infoDialog.showAndWait().ifPresent(infoResponse -> {
                    if (infoResponse == ButtonType.OK) {
                        if (SessionManager.getGroupType() instanceof SuperAdminIGroup) {
                            // Perform the clearing of the logs
                            performLogClear(stage);
                        } else {
                            Main.getLogger().severe(SessionManager.getUsername() + " tried to clear the logs! Their account will be disabled!");
                            Alert warningDialog = new Alert(AlertType.WARNING);
                            warningDialog.setTitle("You've performed an unauthorized action!");
                            warningDialog.setContentText("Your account will be disabled and you will be logged out!\nYou were warned.\nIf you believe this is a mistake or want your account re-enabled\n contact your super admin!");
                            new ManageUserStatus().setUserStatus(SessionManager.getUser(), "Disabled");
                            warningDialog.showAndWait();
                        }
                        if (SessionManager.isSwitchedUser()) {
                            stage.close();
                            SessionManager.revertToAdmin();
                        }
                    }
                });
            }
        });
    }

    private void performLogClear(Stage stage) {
        try {
            // Open the file in write mode, effectively clearing its contents
            try (PrintWriter writer = new PrintWriter(new FileWriter(this.LOG_FILE_PATH))) {
                // File is cleared
            }

            // Optionally, write some logging entry about clearing (this can be done on actual implementation)
            writeLog("Log files cleared");

            Alert successDialog = new Alert(AlertType.INFORMATION);
            successDialog.setTitle("Success");
            successDialog.setHeaderText("Log files cleared!");
            successDialog.showAndWait();

            // Reload the logs viewer to show the cleared logs
            new LogViewer().showLogSelectionDialog();
            stage.close(); // Close the current logs window

        } catch (IOException e) {
            Alert errorDialog = new Alert(AlertType.ERROR);
            errorDialog.setTitle("Error");
            errorDialog.setHeaderText("Error clearing the logs");
            errorDialog.setContentText("An error occurred: " + e.getMessage());
            errorDialog.showAndWait();
        }
    }

    private void writeLog(String message) {
        // Implement this function to write to a secondary logs if needed
        // For example, writing to a separate logs file or recording the clear operation
        System.out.println(message);  // Just for illustration
    }
}
