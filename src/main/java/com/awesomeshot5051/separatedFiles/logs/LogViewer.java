package com.awesomeshot5051.separatedFiles.logs;

import javafx.application.*;
import javafx.geometry.*;
import javafx.geometry.Insets;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.layout.*;
import javafx.stage.*;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.nio.file.*;

public class LogViewer {

    private static final String LOG_FILE_PATH = logger.LOG_FILE_PATH.toString();
    private static final String ERROR_LOG_FILE_PATH = ErrorLogger.ERROR_LOG_PATH.toString();

    /**
     * Shows a dialog allowing the user to choose between viewing regular logs or error logs
     */
    public void showLogSelectionDialog() {
        // Create a custom dialog using Swing for selecting log type
        JDialog dialog = new JDialog();
        dialog.setTitle("Select Log Type");
        dialog.setSize(400, 150);
        dialog.setLocationRelativeTo(null);
        dialog.setModal(true);
        dialog.setResizable(false);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());

        JLabel label = new JLabel("Select which log you would like to view:");
        label.setHorizontalAlignment(SwingConstants.CENTER);
        panel.add(label, BorderLayout.NORTH);

        JPanel buttonPanel = new JPanel();

        JButton regularLogButton = new JButton("Application Logs");
        regularLogButton.addActionListener(e -> {
            dialog.dispose();
            openRegularLogFile();
        });

        JButton errorLogButton = new JButton("Error Logs");
        errorLogButton.addActionListener(e -> {
            dialog.dispose();
            openErrorLogFile();
        });

        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(e -> dialog.dispose());

        buttonPanel.add(regularLogButton);
        buttonPanel.add(errorLogButton);
        buttonPanel.add(cancelButton);

        panel.add(buttonPanel, BorderLayout.CENTER);

        dialog.add(panel);
        dialog.setVisible(true);
    }

    /**
     * Opens the regular application log file in a JavaFX window
     */
    private void openRegularLogFile() {
        readLog(LOG_FILE_PATH, "Application Log");
    }

    /**
     * Opens the error log file in a JavaFX window
     */
    private void openErrorLogFile() {
        Path errorLogPath = ErrorLogger.ERROR_LOG_PATH;

        // Check if error log exists, create it if it doesn't
        if (!Files.exists(errorLogPath)) {
            try {
                Files.createDirectories(errorLogPath.getParent());
                Files.createFile(errorLogPath);
            } catch (IOException e) {
                JOptionPane.showMessageDialog(null,
                        "Could not create error log file: " + e.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
        }

        readLog(ERROR_LOG_FILE_PATH, "Error Log");
    }

    /**
     * Read and display the specified log file in a JavaFX window
     *
     * @param logFilePath The path to the log file
     * @param title       The title for the window
     */
    public void readLog(String logFilePath, String title) {
        // Create a new stage for displaying the logs
        Stage stage = new Stage();
        stage.setTitle(title);

        // Create the UI components
        TextArea textArea = new TextArea();
        textArea.setEditable(false);
        textArea.setStyle("-fx-font-family: 'Consolas', 'Courier New', monospace;");

        Button exitButton = new Button("Exit");
        exitButton.setOnAction(e -> stage.close());

        Button clearButton = new Button("Clear");
        clearButton.setOnAction(e -> clearLog(logFilePath, textArea, stage));

        Button refreshButton = new Button("Refresh");
        refreshButton.setOnAction(e -> loadLog(logFilePath, textArea));

        // Load the logs file into the TextArea and scroll to the bottom
        loadLog(logFilePath, textArea);

        // Create the layout for the buttons
        HBox buttonBox = new HBox(10, refreshButton, clearButton, exitButton);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.setPadding(new Insets(10, 0, 10, 0));

        // Set the buttons at the bottom using BorderPane
        BorderPane root = new BorderPane();
        root.setCenter(textArea);  // The logs content takes up most of the space
        root.setBottom(buttonBox); // The buttons are placed at the bottom

        // Set up the scene and show the stage
        Scene scene = new Scene(root, 800, 600);
        stage.setScene(scene);
        stage.show();
    }

    /**
     * Clears the specified log file and updates the text area
     */
    private void clearLog(String logFilePath, TextArea textArea, Stage stage) {
        try {
            // Truncate the file
            Files.write(Path.of(logFilePath), new byte[0]);

            // Update the text area
            textArea.clear();

            JOptionPane.showMessageDialog(null,
                    "Log file has been cleared.",
                    "Success", JOptionPane.INFORMATION_MESSAGE);

        } catch (IOException e) {
            JOptionPane.showMessageDialog(null,
                    "Failed to clear log file: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Loads the content of the specified log file into the text area
     */
    private void loadLog(String logFilePath, TextArea textArea) {
        // Read the logs file and append its contents to the TextArea
        new Thread(() -> {
            try (BufferedReader reader = new BufferedReader(new FileReader(logFilePath))) {
                String line;
                StringBuilder logContent = new StringBuilder();
                while ((line = reader.readLine()) != null) {
                    logContent.append(line).append("\n");
                }
                final String log = logContent.toString();
                // Update UI on the JavaFX Application Thread
                Platform.runLater(() -> {
                    textArea.setText(log);  // Set the logs content
                    textArea.positionCaret(textArea.getLength());  // Move the caret to the end
                    textArea.setScrollTop(Double.MAX_VALUE);  // Scroll to the bottom
                });
            } catch (IOException e) {
                Platform.runLater(() -> {
                    textArea.setText("Log file not found or empty.");

                    // Check if the file exists, if not, try to create it
                    if (!Files.exists(Path.of(logFilePath))) {
                        try {
                            Files.createDirectories(Path.of(logFilePath).getParent());
                            Files.createFile(Path.of(logFilePath));
                            textArea.setText("Created a new empty log file.");
                        } catch (IOException createError) {
                            textArea.setText("Failed to create log file: " + createError.getMessage());
                        }
                    }
                });
            }
        }).start();
    }
}