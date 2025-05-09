package com.awesomeshot5051.separatedFiles.logs;

import java.io.*;
import java.nio.file.*;
import java.text.*;
import java.util.*;
import java.util.logging.*;
import java.util.logging.Formatter;

public class logger {
    // Updated to use user-local directory for portability
    public static final Path LOG_FILE_PATH = Paths.get(
            System.getProperty("user.home"), ".javaLoginGUI", "logs", "log.txt"
    );
    private static final CustomLogger customLogger = new CustomLogger();

    private static void setupLogger() {
        try {
            // Ensure logs directory exists
            Files.createDirectories(LOG_FILE_PATH.getParent());

            // Remove default console logging
            customLogger.setUseParentHandlers(false);

            // Prevent duplicate handlers
            if (customLogger.getHandlers().length == 0) {
                FileHandler fileHandler = new FileHandler(LOG_FILE_PATH.toString(), true);
                fileHandler.setFormatter(new CustomFormatter());
                customLogger.addHandler(fileHandler);
            }
        } catch (IOException e) {
            System.err.println("Failed to initialize logger: " + e.getMessage());
        }
    }

    public Logger makeLogger() {
        setupLogger();
        return customLogger;
    }

    public static Logger getLogger() {
        return customLogger;
    }

    // --- Custom Logger Class ---
    private static class CustomLogger extends Logger {
        protected CustomLogger() {
            super("logger", null);
        }

        @Override
        public void info(String msg) {
            super.info("ℹ " + msg); // ℹ Information symbol
        }

        @Override
        public void warning(String msg) {
            super.warning("⚠ " + msg); // ⚠ Warning symbol
        }
    }

    // --- Custom Formatter Class ---
    private static class CustomFormatter extends Formatter {
        private final SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy hh:mm:ss a");

        @Override
        public String format(LogRecord record) {
            String timeStamp = dateFormat.format(new Date(record.getMillis()));
            String level = record.getLevel().getName();
            return String.format("%s: %s at %s%n", level, record.getMessage(), timeStamp);
        }
    }
}
