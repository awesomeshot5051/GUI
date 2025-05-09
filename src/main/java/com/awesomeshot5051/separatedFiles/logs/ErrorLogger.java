package com.awesomeshot5051.separatedFiles.logs;

import javax.swing.*;
import java.io.*;
import java.nio.file.*;
import java.time.*;
import java.time.format.*;
import java.util.*;
import java.util.concurrent.atomic.*;

/**
 * Enhanced error logging facility with humorous messages and system integration.
 * Logs errors to the user's home directory under .javaLoginGUI/logs.
 */
public class ErrorLogger {

    private static final String LOG_DIR = System.getProperty("user.home") + File.separator + ".javaLoginGUI" + File.separator + "logs";
    private static final String ERROR_LOG_FILENAME = "error.log";
    public static final Path ERROR_LOG_PATH = Paths.get(LOG_DIR, ERROR_LOG_FILENAME);
    private static final AtomicBoolean initialized = new AtomicBoolean(false);

    // Humorous error remarks
    private static final String[] REMARKS = {
            "Whoops my bad. \nIt seems I made a mistake in my code. \nOr something else happened. \nEh, either way, here's what happened",
            "Oh no! Something went wrong. \nHere's the scoop on the error:",
            "Yikes! An error occurred. \nLet's see what went wrong:",
            "Oops! Looks like I tripped over a bug. \nHere's the error report:",
            "Well, this is embarrassing. \nAn error happened. \nHere's the details:",
            "You should have seen the other guy!\nHere's the play-by-play:",
            "Houston, we have a problem. \nError details below:",
            "Unexpected plot twist! \nHere's what the error looks like:",
            "That wasn't supposed to happen... \nError details:",
            "Computer says no. \nSpecifically, it says:"
    };

    /**
     * Initializes the error logger by ensuring the log directory exists.
     */
    public static void initialize() {
        if (initialized.compareAndSet(false, true)) {
            try {
                Files.createDirectories(Paths.get(LOG_DIR));
                System.out.println("Error logger initialized. Logs will be written to: " + ERROR_LOG_PATH);
            } catch (IOException e) {
                System.err.println("Failed to create log directory: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    /**
     * Logs an error with a timestamp and a randomly selected humorous remark.
     *
     * @param throwable The exception to log
     * @return 0 if successful, -1 if there was an error
     */
    public static int logError(Throwable throwable) {
        initialize();

        Random random = new Random();
        int remarkIndex = random.nextInt(REMARKS.length);
        String selectedRemark = REMARKS[remarkIndex];

        try {
            Files.createDirectories(ERROR_LOG_PATH.getParent());

            try (PrintWriter pw = new PrintWriter(new FileWriter(ERROR_LOG_PATH.toString(), true))) {
                // Add timestamp
                LocalDateTime now = LocalDateTime.now();
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                pw.println("\n=== " + now.format(formatter) + " ===");

                // Add random remark
                pw.println(selectedRemark);

                // Add stack trace
                throwable.printStackTrace(pw);
                pw.println("\n--------------------------------------------\n");
                return 0;
            }
        } catch (IOException e) {
            System.err.println("Error writing to log file: " + e.getMessage());
            e.printStackTrace();
            return -1;
        }
    }

    /**
     * Opens the log viewer to display error logs
     * This has been moved to LogViewer class.
     *
     * @param option 0 to open the error log, any other value to exit with error code -1
     */
    public static void checkErrorLog(int option) {
        if (option == 0) {
            // Launch the log viewer
            new LogViewer().showLogSelectionDialog();
        } else {
            System.exit(-1);
        }
    }

    /**
     * Returns the full path to the error log file.
     *
     * @return Path to error log file
     */
    public static Path getErrorLogPath() {
        initialize();
        return ERROR_LOG_PATH;
    }

    /**
     * Helper method to show an error dialog and log the exception.
     *
     * @param title     Dialog title
     * @param message   Error message to display
     * @param throwable Exception to log
     */
    public static void showErrorDialog(String title, String message, Throwable throwable) {
        logError(throwable);

        JOptionPane.showMessageDialog(null,
                message + "\n\nError details have been logged to:\n" + ERROR_LOG_PATH,
                title, JOptionPane.ERROR_MESSAGE);
    }

    /**
     * Helper method to handle and log exceptions with a simplified interface
     *
     * @param throwable  The exception to handle
     * @param showDialog Whether to show an error dialog
     */
    public static void handleException(Throwable throwable, boolean showDialog) {
        logError(throwable);

        if (showDialog) {
            JOptionPane.showMessageDialog(null,
                    "An error occurred: " + throwable.getMessage() +
                            "\n\nError details have been logged to:\n" + ERROR_LOG_PATH,
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}