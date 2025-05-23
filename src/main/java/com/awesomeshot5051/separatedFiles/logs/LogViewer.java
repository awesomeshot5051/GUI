package com.awesomeshot5051.separatedFiles.logs;

import com.awesomeshot5051.*;
import javafx.application.*;
import javafx.geometry.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.*;

import java.io.*;
import java.nio.file.*;

public class LogViewer {

    private static final String LOG_FILE_PATH = logger.LOG_FILE_PATH.toString();
    private static final String ERROR_LOG_FILE_PATH = ErrorLogger.ERROR_LOG_PATH.toString();

    /**
     * Shows a dialog allowing the user to choose between viewing regular logs or error logs
     */
    public void showLogSelectionDialog() {
        showLogSelectionDialog(Main.getStage());
    }

    /**
     * Shows a dialog allowing the user to choose between viewing regular logs or error logs
     *
     * @param parentStage The parent stage to use for the dialog
     */
    public void showLogSelectionDialog(Stage parentStage) {
        // Create a custom JavaFX dialog for selecting log type
        Stage dialog = new Stage();
        dialog.setTitle("Select Log Type");
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initOwner(parentStage);
        dialog.setResizable(false);

        VBox root = new VBox(10);
        root.setPadding(new Insets(20));
        root.setAlignment(Pos.CENTER);

        Label label = new Label("Select which log you would like to view:");
        label.setStyle("-fx-font-size: 14px;");

        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER);

        Button regularLogButton = new Button("Application Logs");
        regularLogButton.setPrefWidth(150);
        regularLogButton.setOnAction(e -> {
            dialog.close();
            openRegularLogFile();
        });

        Button errorLogButton = new Button("Error Logs");
        errorLogButton.setPrefWidth(150);
        errorLogButton.setOnAction(e -> {
            dialog.close();
            openErrorLogFile();
        });

        Button cancelButton = new Button("Cancel");
        cancelButton.setPrefWidth(100);
        cancelButton.setOnAction(e -> dialog.close());

        buttonBox.getChildren().addAll(regularLogButton, errorLogButton, cancelButton);
        root.getChildren().addAll(label, buttonBox);

        Scene scene = new Scene(root, 500, 150);
        dialog.setScene(scene);
        dialog.show();
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
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText(null);
                alert.setContentText("Could not create error log file: " + e.getMessage());
                alert.showAndWait();
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
        clearButton.setOnAction(e -> clearLog(logFilePath, stage));

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
    private void clearLog(String logFilePath, Stage stage) {
        new LogClearer(logFilePath).clearLogs(stage);
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