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
import java.util.*;

public class LogViewer {

    private static final String LOG_FILE_PATH = logger.LOG_FILE_PATH.toString();
    private static final String ERROR_LOG_FILE_PATH = ErrorLogger.ERROR_LOG_PATH.toString();

    public void showLogSelectionDialog() {
        showLogSelectionDialog(Main.getStage());
    }

    public void showLogSelectionDialog(Stage parentStage) {
        Stage dialog = new Stage();
        dialog.setTitle("Select Log Type");
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initOwner(parentStage);
        dialog.setResizable(false);

        VBox root = new VBox(10);
        root.setPadding(new Insets(20));
        root.setAlignment(Pos.CENTER);

        Label label = new Label("Select which log you would like to view:");

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
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/styles/logs.css")).toExternalForm());
        dialog.setScene(scene);
        dialog.show();
    }

    private void openRegularLogFile() {
        readLog(LOG_FILE_PATH, "Application Log");
    }

    private void openErrorLogFile() {
        Path errorLogPath = ErrorLogger.ERROR_LOG_PATH;

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

    public void readLog(String logFilePath, String title) {
        Stage stage = new Stage();
        stage.setTitle(title);

        TextArea textArea = new TextArea();
        textArea.setEditable(false);
        textArea.setStyle("-fx-font-family: 'Consolas', 'Courier New', monospace;");

        Button exitButton = new Button("Exit");
        exitButton.setOnAction(e -> stage.close());

        Button clearButton = new Button("Clear");
        clearButton.setOnAction(e -> clearLog(logFilePath, stage));

        Button refreshButton = new Button("Refresh");
        refreshButton.setOnAction(e -> loadLog(logFilePath, textArea));

        loadLog(logFilePath, textArea);

        HBox buttonBox = new HBox(10, refreshButton, clearButton, exitButton);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.setPadding(new Insets(10, 0, 10, 0));

        BorderPane root = new BorderPane();
        root.setCenter(textArea);
        root.setBottom(buttonBox);

        Scene scene = new Scene(root, 800, 600);
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/styles/logs.css")).toExternalForm());
        stage.setScene(scene);
        stage.show();
    }

    private void clearLog(String logFilePath, Stage stage) {
        new LogClearer(logFilePath).clearLogs(stage);
    }

    private void loadLog(String logFilePath, TextArea textArea) {
        new Thread(() -> {
            try (BufferedReader reader = new BufferedReader(new FileReader(logFilePath))) {
                String line;
                StringBuilder logContent = new StringBuilder();
                while ((line = reader.readLine()) != null) {
                    logContent.append(line).append("\n");
                }
                final String log = logContent.toString();
                Platform.runLater(() -> {
                    textArea.setText(log);
                    textArea.positionCaret(textArea.getLength());
                    textArea.setScrollTop(Double.MAX_VALUE);
                });
            } catch (IOException e) {
                Platform.runLater(() -> {
                    textArea.setText("Log file not found or empty.");
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
