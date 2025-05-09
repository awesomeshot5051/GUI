package com.awesomeshot5051.separatedFiles.security;

import javafx.animation.*;
import javafx.application.*;
import javafx.concurrent.*;
import javafx.geometry.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.*;
import javafx.stage.*;
import javafx.util.*;

import java.security.*;
import java.util.concurrent.atomic.*;

public class BruteForceCodeCracker extends Application {

    private static final String CHARSET = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789!@#$%^&*()-_=+[]{}|;:,.<>?/";
    private TextField targetField;
    private HBox animatedDisplayBox;
    private TextArea logArea;
    private Label attemptsLabel;
    private Label timeLabel;
    private ProgressBar progressBar;
    private Button startButton;
    private Button stopButton;
    private AtomicBoolean isRunning = new AtomicBoolean(false);
    private BruteForceService bruteForceService;
    private final SecureRandom random = new SecureRandom();
    private Timeline animationTimeline;
    private Text[] animatedChars;
    private String currentAttempt = "";

    // Animation speed controls
    private double baseAnimationSpeed = 100; // ms between updates
    private double minAnimationSpeed = 20;   // fastest animation
    private double speedFactor = 1.0;        // adjusts based on length

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Brute Force Code Cracker");

        // Create UI components
        VBox mainLayout = createMainLayout();

        // Set up the scene
        Scene scene = new Scene(mainLayout, 600, 550);
        scene.getStylesheets().add(getClass().getResource("/styles/bruteforce.css").toExternalForm());
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private VBox createMainLayout() {
        // Target field section
        Label targetLabel = new Label("Enter target string to crack:");
        targetField = new TextField();
        targetField.setPromptText("Enter password to crack");

        // Animated display section
        Label displayLabel = new Label("Cracking in progress:");
        animatedDisplayBox = new HBox(2);
        animatedDisplayBox.setAlignment(Pos.CENTER);
        animatedDisplayBox.setPadding(new Insets(15));
        animatedDisplayBox.setMinHeight(80);
        animatedDisplayBox.getStyleClass().add("animated-display");

        // Initial placeholder
        Text placeholder = new Text("[ Enter a target string and press Start ]");
        placeholder.setFont(Font.font("Courier New", FontWeight.BOLD, 18));
        animatedDisplayBox.getChildren().add(placeholder);

        // Progress section
        progressBar = new ProgressBar(0);
        progressBar.setMaxWidth(Double.MAX_VALUE);

        attemptsLabel = new Label("Attempts: 0");
        timeLabel = new Label("Time: 0s");

        GridPane progressGrid = new GridPane();
        progressGrid.setHgap(10);
        progressGrid.setVgap(10);
        progressGrid.add(new Label("Progress:"), 0, 0);
        progressGrid.add(progressBar, 1, 0);
        progressGrid.add(attemptsLabel, 0, 1);
        progressGrid.add(timeLabel, 1, 1);
        progressGrid.getColumnConstraints().add(new ColumnConstraints(100));
        progressGrid.getColumnConstraints().add(new ColumnConstraints(300));

        // Log area
        Label logLabel = new Label("Cracking log:");
        logArea = new TextArea();
        logArea.setEditable(false);
        logArea.setWrapText(true);

        // Buttons
        startButton = new Button("Start Cracking");
        stopButton = new Button("Stop");
        stopButton.setDisable(true);

        HBox buttonBox = new HBox(10, startButton, stopButton);
        buttonBox.setAlignment(Pos.CENTER);

        // Set button actions
        startButton.setOnAction(e -> startBruteForce());
        stopButton.setOnAction(e -> stopBruteForce());

        // Main layout
        VBox mainLayout = new VBox(10,
                targetLabel, targetField,
                new Separator(),
                displayLabel, animatedDisplayBox,
                new Separator(),
                progressGrid,
                new Separator(),
                logLabel, logArea,
                buttonBox
        );
        mainLayout.setPadding(new Insets(15));
        VBox.setVgrow(logArea, Priority.ALWAYS);

        return mainLayout;
    }

    private void startBruteForce() {
        String target = targetField.getText().trim();
        if (target.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Error", "Please enter a target string to crack.");
            return;
        }

        // Check if too long (to prevent unreasonable cracking times)
        if (target.length() > 8) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION,
                    "The target is " + target.length() + " characters long. Brute forcing may take an extremely long time.\n" +
                            "Are you sure you want to proceed?",
                    ButtonType.YES, ButtonType.NO);
            alert.setTitle("Confirmation");
            alert.setHeaderText("Long Target Warning");

            alert.showAndWait().ifPresent(response -> {
                if (response == ButtonType.YES) {
                    initiateBruteForce(target);
                }
            });
        } else {
            initiateBruteForce(target);
        }
    }

    private void initiateBruteForce(String target) {
        // Update UI
        logArea.clear();
        progressBar.setProgress(0);
        attemptsLabel.setText("Attempts: 0");
        timeLabel.setText("Time: 0s");

        // Set up the animated display
        setupAnimatedDisplay(target.length());

        startButton.setDisable(true);
        stopButton.setDisable(false);
        targetField.setDisable(true);

        isRunning.set(true);

        // Create and start the brute force service
        bruteForceService = new BruteForceService(target);

        // Set up bindings and listeners
        progressBar.progressProperty().bind(bruteForceService.progressProperty());

        bruteForceService.messageProperty().addListener((obs, oldMsg, newMsg) -> {
            if (newMsg != null && newMsg.startsWith("Trying: ")) {
                // Extract the current attempt
                int endOfAttempt = newMsg.indexOf(" (");
                if (endOfAttempt > 8) { // "Trying: ".length() = 8
                    currentAttempt = newMsg.substring(8, endOfAttempt);
                }
            }
            Platform.runLater(() -> logArea.appendText(newMsg + "\n"));
        });

        bruteForceService.setOnSucceeded(event -> {
            BruteForceResult result = bruteForceService.getValue();
            handleBruteForceCompleted(result);
        });

        bruteForceService.setOnFailed(event -> {
            Throwable exception = bruteForceService.getException();
            handleBruteForceError(exception);
        });

        bruteForceService.setOnCancelled(event -> {
            handleBruteForceCancelled();
        });

        // Start animation
        startAnimation();

        logArea.appendText("Starting brute force attack on: " + target + "\n");
        logArea.appendText("Character set size: " + CHARSET.length() + "\n");
        logArea.appendText("Estimated max attempts: " + Math.pow(CHARSET.length(), target.length()) + "\n");
        logArea.appendText("----------------------------------------\n");

        bruteForceService.start();
    }

    private void setupAnimatedDisplay(int length) {
        animatedDisplayBox.getChildren().clear();
        animatedChars = new Text[length];

        // Adjust animation speed based on length
        speedFactor = Math.max(0.2, 1.0 - (length * 0.15)); // Reduces speed for longer passwords

        for (int i = 0; i < length; i++) {
            Text charText = new Text("?");
            charText.setFont(Font.font("Courier New", FontWeight.BOLD, 24));
            charText.getStyleClass().add("animated-char");
            animatedChars[i] = charText;

            // Add a small spacer after each character for better visibility
            HBox charBox = new HBox(charText);
            charBox.setAlignment(Pos.CENTER);
            charBox.setMinWidth(30);
            animatedDisplayBox.getChildren().add(charBox);
        }
    }

    private void startAnimation() {
        if (animationTimeline != null) {
            animationTimeline.stop();
        }

        // Create a new timeline for the animation
        animationTimeline = new Timeline(
                new KeyFrame(Duration.millis(baseAnimationSpeed * speedFactor), event -> {
                    // Update each character with random values except for known correct positions
                    for (int i = 0; i < animatedChars.length; i++) {
                        // If we have a current attempt and this position matches it,
                        // sometimes show the correct character
                        if (i < currentAttempt.length()) {
                            // 30% chance to show the actual current attempt character
                            if (random.nextDouble() < 0.3) {
                                animatedChars[i].setText(String.valueOf(currentAttempt.charAt(i)));
                                continue;
                            }
                        }

                        // Otherwise show a random character
                        char randomChar = CHARSET.charAt(random.nextInt(CHARSET.length()));
                        animatedChars[i].setText(String.valueOf(randomChar));
                    }
                })
        );
        animationTimeline.setCycleCount(Animation.INDEFINITE);
        animationTimeline.play();
    }

    private void stopAnimation() {
        if (animationTimeline != null) {
            animationTimeline.stop();
        }
    }

    private void displayCrackedPassword(String password) {
        stopAnimation();
        animatedDisplayBox.getChildren().clear();

        for (int i = 0; i < password.length(); i++) {
            Text charText = new Text(String.valueOf(password.charAt(i)));
            charText.setFont(Font.font("Courier New", FontWeight.BOLD, 24));
            charText.getStyleClass().add("cracked-char");

            HBox charBox = new HBox(charText);
            charBox.setAlignment(Pos.CENTER);
            charBox.setMinWidth(30);
            animatedDisplayBox.getChildren().add(charBox);
        }
    }

    private void stopBruteForce() {
        if (bruteForceService != null && bruteForceService.isRunning()) {
            isRunning.set(false);
            bruteForceService.cancel();
            stopAnimation();
        }
    }

    private void handleBruteForceCompleted(BruteForceResult result) {
        // Update UI
        progressBar.progressProperty().unbind();
        progressBar.setProgress(1.0);

        startButton.setDisable(false);
        stopButton.setDisable(true);
        targetField.setDisable(false);

        // Stop and update the animated display
        stopAnimation();
        displayCrackedPassword(result.crackedPassword);

        // Display results
        attemptsLabel.setText("Attempts: " + result.attempts);
        timeLabel.setText("Time: " + formatDuration(result.durationMillis));

        logArea.appendText("----------------------------------------\n");
        logArea.appendText("🎉 SUCCESS! Password cracked: " + result.crackedPassword + "\n");
        logArea.appendText("Total attempts: " + result.attempts + "\n");
        logArea.appendText("Time taken: " + formatDuration(result.durationMillis) + "\n");

        // Calculate and display statistics
        double attemptsPerSecond = result.attempts / (result.durationMillis / 1000.0);
        logArea.appendText("Speed: " + String.format("%.2f", attemptsPerSecond) + " attempts/second\n");

        // Show success dialog
        showAlert(Alert.AlertType.INFORMATION, "Success",
                "Password cracked: " + result.crackedPassword + "\n" +
                        "Attempts: " + result.attempts + "\n" +
                        "Time: " + formatDuration(result.durationMillis));
    }

    private void handleBruteForceError(Throwable exception) {
        // Update UI
        progressBar.progressProperty().unbind();
        startButton.setDisable(false);
        stopButton.setDisable(true);
        targetField.setDisable(false);

        // Stop animation
        stopAnimation();

        // Log error
        logArea.appendText("ERROR: " + exception.getMessage() + "\n");

        // Show error dialog
        showAlert(Alert.AlertType.ERROR, "Error", "An error occurred: " + exception.getMessage());
    }

    private void handleBruteForceCancelled() {
        // Update UI
        progressBar.progressProperty().unbind();
        startButton.setDisable(false);
        stopButton.setDisable(true);
        targetField.setDisable(false);

        // Stop animation
        stopAnimation();

        // Reset display
        animatedDisplayBox.getChildren().clear();
        Text cancelled = new Text("[ Cracking cancelled ]");
        cancelled.setFont(Font.font("Courier New", FontWeight.BOLD, 18));
        animatedDisplayBox.getChildren().add(cancelled);

        // Log cancellation
        logArea.appendText("Brute force attack cancelled by user.\n");
    }

    private String formatDuration(long millis) {
        java.time.Duration duration = java.time.Duration.ofMillis(millis);
        long hours = duration.toHours();
        long minutes = duration.toMinutesPart();
        long seconds = duration.toSecondsPart();
        long millisPart = duration.toMillisPart();

        if (hours > 0) {
            return String.format("%d:%02d:%02d.%03d", hours, minutes, seconds, millisPart);
        } else if (minutes > 0) {
            return String.format("%d:%02d.%03d", minutes, seconds, millisPart);
        } else {
            return String.format("%d.%03d seconds", seconds, millisPart);
        }
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    /**
     * Service for running the brute force attack on a background thread
     */
    private class BruteForceService extends Service<BruteForceResult> {
        private final String targetPassword;

        public BruteForceService(String targetPassword) {
            this.targetPassword = targetPassword;
        }

        @Override
        protected Task<BruteForceResult> createTask() {
            return new Task<>() {
                @Override
                protected BruteForceResult call() throws Exception {
                    long startTime = System.currentTimeMillis();
                    long attempts = 0;
                    long lastUpdateTime = startTime;
                    long lastUpdateAttempts = 0;

                    // Get target length for optimized generation
                    int targetLength = targetPassword.length();

                    // Iterative approach - start with shorter lengths and increase
                    for (int length = 1; length <= targetLength; length++) {
                        // Update progress when moving to a new length
                        updateProgress(length - 1, targetLength);
                        updateMessage("Trying passwords of length " + length + "...");

                        // Generate current state
                        int[] indices = new int[length];
                        char[] current = new char[length];

                        // Initialize with first character
                        for (int i = 0; i < length; i++) {
                            indices[i] = 0;
                            current[i] = CHARSET.charAt(0);
                        }

                        boolean complete = false;

                        // Start permutation loop
                        while (!complete && isRunning.get()) {
                            attempts++;

                            // Convert current indices to string
                            for (int i = 0; i < length; i++) {
                                current[i] = CHARSET.charAt(indices[i]);
                            }
                            String attempt = new String(current);

                            // Check attempt periodically (not every iteration to improve performance)
                            if (attempts % 5000 == 0) {
                                // Check if we found the password
                                if (attempt.equals(targetPassword)) {
                                    long endTime = System.currentTimeMillis();
                                    return new BruteForceResult(
                                            attempt,
                                            attempts,
                                            endTime - startTime
                                    );
                                }

                                // Update progress and statistics (every ~5k attempts)
                                long currentTime = System.currentTimeMillis();
                                if (currentTime - lastUpdateTime > 200) { // Update every 200ms
                                    // Calculate attempts per second
                                    double attemptsPerSecond = (attempts - lastUpdateAttempts) /
                                            ((currentTime - lastUpdateTime) / 1000.0);

                                    // Update the UI with progress info
                                    updateMessage(String.format("Trying: %s (%,d attempts, %.2f/sec)",
                                            attempt, attempts, attemptsPerSecond));

                                    // Update the attempts label
                                    long finalAttempts = attempts;
                                    Platform.runLater(() -> {
                                        attemptsLabel.setText(String.format("Attempts: %,d", finalAttempts));
                                        timeLabel.setText("Time: " + formatDuration(currentTime - startTime));
                                    });

                                    // Reset update timers
                                    lastUpdateTime = currentTime;
                                    lastUpdateAttempts = attempts;

                                    // Check if task was cancelled
                                    if (isCancelled()) {
                                        updateMessage("Task cancelled");
                                        return null;
                                    }
                                }
                            }

                            // Move to next permutation
                            int pos = length - 1;
                            while (pos >= 0) {
                                indices[pos]++;
                                if (indices[pos] < CHARSET.length()) {
                                    break;
                                }
                                indices[pos] = 0;
                                pos--;
                            }

                            // If we've tried all permutations of this length, move to next length
                            if (pos < 0) {
                                complete = true;
                            }
                        }

                        // If we've found the target or the task was cancelled, break out
                        if (!isRunning.get()) {
                            return null;
                        }

                        // If we're still running but haven't found it yet, check if this was the target length
                        if (length == targetLength) {
                            // One last try with the exact length
                            for (int i = 0; i < length; i++) {
                                indices[i] = 0;
                                current[i] = CHARSET.charAt(0);
                            }

                            complete = false;
                            while (!complete && isRunning.get()) {
                                attempts++;

                                // Convert current indices to string
                                for (int i = 0; i < length; i++) {
                                    current[i] = CHARSET.charAt(indices[i]);
                                }
                                String attempt = new String(current);

                                // Check if we found the password (check every attempt at target length)
                                if (attempt.equals(targetPassword)) {
                                    long endTime = System.currentTimeMillis();
                                    return new BruteForceResult(
                                            attempt,
                                            attempts,
                                            endTime - startTime
                                    );
                                }

                                // Update progress and UI more frequently at target length
                                if (attempts % 1000 == 0) {
                                    long currentTime = System.currentTimeMillis();
                                    if (currentTime - lastUpdateTime > 100) { // More frequent updates
                                        updateMessage(String.format("Trying: %s (%,d attempts)",
                                                attempt, attempts));

                                        long finalAttempts1 = attempts;
                                        Platform.runLater(() -> {
                                            attemptsLabel.setText(String.format("Attempts: %,d", finalAttempts1));
                                            timeLabel.setText("Time: " + formatDuration(currentTime - startTime));
                                            // Gradually increase progress at target length
                                            double progress = Math.min(0.99, (double) finalAttempts1 /
                                                    Math.min(10000000, Math.pow(CHARSET.length(), targetLength)));
                                            updateProgress(targetLength - 1 + progress, targetLength);
                                        });

                                        lastUpdateTime = currentTime;

                                        if (isCancelled()) {
                                            updateMessage("Task cancelled");
                                            return null;
                                        }
                                    }
                                }

                                // Move to next permutation
                                int pos = length - 1;
                                while (pos >= 0) {
                                    indices[pos]++;
                                    if (indices[pos] < CHARSET.length()) {
                                        break;
                                    }
                                    indices[pos] = 0;
                                    pos--;
                                }

                                // If we've tried all permutations, break
                                if (pos < 0) {
                                    complete = true;
                                }
                            }
                        }
                    }

                    // If we get here, we've somehow exhausted all possibilities without finding the target
                    throw new RuntimeException("Password not found in character set after " + attempts + " attempts");
                }
            };
        }
    }

    /**
     * Class to hold the result of a brute force operation
     */
    private static class BruteForceResult {
        final String crackedPassword;
        final long attempts;
        final long durationMillis;

        public BruteForceResult(String crackedPassword, long attempts, long durationMillis) {
            this.crackedPassword = crackedPassword;
            this.attempts = attempts;
            this.durationMillis = durationMillis;
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}