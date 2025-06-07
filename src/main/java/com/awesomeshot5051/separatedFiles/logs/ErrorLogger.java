package com.awesomeshot5051.separatedFiles.logs;

import com.awesomeshot5051.*;
import javafx.scene.control.*;

import javax.swing.*;
import java.io.*;
import java.nio.file.*;
import java.text.*;
import java.time.*;
import java.time.format.*;
import java.util.*;
import java.util.concurrent.atomic.*;
import java.util.logging.*;
import java.util.logging.Formatter;

/**
 * Enhanced error logging facility with humorous messages and system integration.
 * Logs errors to the user's home directory under .javaLoginGUI/logs.
 */
public class ErrorLogger {

    private static final String LOG_DIR = System.getProperty("user.home") + File.separator + ".javaLoginGUI" + File.separator + "logs";
    private static final String ERROR_LOG_FILENAME = "error.log";
    public static final Path ERROR_LOG_PATH = Paths.get(LOG_DIR, ERROR_LOG_FILENAME);
    private static final AtomicBoolean initialized = new AtomicBoolean(false);
    private static final CustomErrorLogger customErrorLogger = new CustomErrorLogger();

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

    public ErrorLogger() {
        makeLogger();
    }

    /**
     * Sets up the error logger with appropriate handlers and formatters
     */
    private void setupLogger() {
        try {
            // Ensure logs directory exists
            Files.createDirectories(ERROR_LOG_PATH.getParent());

            // Remove default console logging
            customErrorLogger.setUseParentHandlers(false);

            // Prevent duplicate handlers
            if (customErrorLogger.getHandlers().length == 0) {
                FileHandler fileHandler = new FileHandler(ERROR_LOG_PATH.toString(), true);
                fileHandler.setFormatter(new CustomErrorFormatter());
                customErrorLogger.addHandler(fileHandler);
            }
        } catch (IOException e) {
            handleException("Failed to initialize error logger", e);
        }
    }

    /**
     * Creates and returns a configured logger instance
     *
     * @return The configured logger instance
     */
    public Logger makeLogger() {
        setupLogger();
        return customErrorLogger;
    }

    /**
     * Initializes the error logger by ensuring the log directory exists.
     */
    public void initialize() {
        if (initialized.compareAndSet(false, true)) {
            try {
                Files.createDirectories(Paths.get(LOG_DIR));
                System.out.println("Error logger initialized. Logs will be written to: " + ERROR_LOG_PATH);
            } catch (IOException e) {
                handleException("Failed to initialize error logger", e);
            }
        }
    }

    /**
     * Logs an error with a timestamp and a randomly selected humorous remark.
     *
     * @param throwable The exception to log
     */
    public void logError(Throwable throwable) {
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
            }
        } catch (IOException e) {
            Main.getErrorLogger().handleException("Failed to log error", e);
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
    public Path getErrorLogPath() {
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
    public void showErrorDialog(String title, String message, Throwable throwable) {
        logError(throwable);

        JOptionPane.showMessageDialog(null,
                message + "\n\nError details have been logged to:\n" + ERROR_LOG_PATH,
                title, JOptionPane.ERROR_MESSAGE);
    }

    /**
     * Helper method to handle and log exceptions with a simplified interface
     *
     * @param throwable The exception to handle
     */
    public void handleException(String message, Throwable throwable) {
        logError(throwable);
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(message);
        alert.setContentText(throwable.getMessage() +
                "\n\nError details have been logged to:\n" + ERROR_LOG_PATH);
        alert.showAndWait();
    }

    /**
     * Custom error logger class that enhances error logging with specific formatting
     */
    private static class CustomErrorLogger extends Logger {
        protected CustomErrorLogger() {
            super("errorLogger", null);
        }

        @Override
        public void severe(String msg) {
            super.severe("#### FATAL ERROR ####\n" + msg); // #### FATAL ERROR #### symbol
        }
    }

    /**
     * Custom formatter for error log entries
     */
    private static class CustomErrorFormatter extends Formatter {
        private final SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy hh:mm:ss a");

        @Override
        public String format(LogRecord record) {
            StringBuilder sb = new StringBuilder();
            Date date = new Date(record.getMillis());

            // Format: [LEVEL] [Timestamp] Message
            sb.append("[").append(record.getLevel()).append("] ");
            sb.append("[").append(dateFormat.format(date)).append("] ");
            sb.append(formatMessage(record)).append("\n");

            // Add exception details if present
            if (record.getThrown() != null) {
                try {
                    StringWriter sw = new StringWriter();
                    PrintWriter pw = new PrintWriter(sw);
                    record.getThrown().printStackTrace(pw);
                    pw.close();
                    sb.append(sw);
                } catch (Exception ex) {
                    sb.append("Failed to print stack trace: ").append(ex.getMessage()).append("\n");
                }
            }

            sb.append("--------------------------------------------\n");
            return sb.toString();
        }
    }
}