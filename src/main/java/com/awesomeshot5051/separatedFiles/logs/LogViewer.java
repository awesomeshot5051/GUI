package com.awesomeshot5051.separatedFiles.logs;

import javafx.application.*;
import javafx.geometry.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.*;

import java.io.*;

public class LogViewer {

    private static final String LOG_FILE_PATH = "D:\\GUI\\src\\main\\resources\\logs\\log.txt";

    public void readLog() {
        // Create a new stage for displaying the logs
        Stage stage = new Stage();
        stage.setTitle("Log File");

        // Create the UI components
        TextArea textArea = new TextArea();
        textArea.setEditable(false);

        Button exitButton = new Button("Exit");
        exitButton.setOnAction(e -> stage.close());

        Button clearButton = new Button("Clear");
        clearButton.setOnAction(e -> new LogClearer().clearLogs(stage));

        // Load the logs file into the TextArea and scroll to the bottom
        loadLog(textArea);

        // Create the layout for the buttons
        HBox buttonBox = new HBox(10, clearButton, exitButton);
        buttonBox.setAlignment(Pos.CENTER);

        // Set the buttons at the bottom using BorderPane
        BorderPane root = new BorderPane();
        root.setCenter(textArea);  // The logs content takes up most of the space
        root.setBottom(buttonBox); // The buttons are placed at the bottom

        // Set up the scene and show the stage
        Scene scene = new Scene(root, 800, 600);
        stage.setScene(scene);
        stage.show();
    }

    private void loadLog(TextArea textArea) {
        // Read the logs file and append its contents to the TextArea
        new Thread(() -> {
            try (BufferedReader reader = new BufferedReader(new FileReader(LOG_FILE_PATH))) {
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
                Platform.runLater(() -> textArea.setText("Log file not found."));
            }
        }).start();
    }
}
