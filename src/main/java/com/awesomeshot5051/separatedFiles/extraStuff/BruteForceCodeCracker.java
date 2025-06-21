package com.awesomeshot5051.separatedFiles.extraStuff;

import com.awesomeshot5051.*;
import com.awesomeshot5051.separatedFiles.*;
import com.awesomeshot5051.separatedFiles.Styler.*;
import com.awesomeshot5051.separatedFiles.session.*;
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
import java.util.*;
import java.util.concurrent.atomic.*;

public class BruteForceCodeCracker extends Application {

    private static final String CHARSET = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789!@#$%^&*()-_=+[]{}|;:,.<>?/ ";
    private TextField targetField;
    private HBox animatedDisplayBox;
    private TextArea logArea;
    private Label attemptsLabel;
    private Label timeLabel;
    private ProgressBar progressBar;
    private Button startButton;
    private Button stopButton;
    private final AtomicBoolean isRunning = new AtomicBoolean(false);
    private BruteForceService bruteForceService;
    private final SecureRandom random = new SecureRandom();
    private Timeline animationTimeline;
    private Text[] animatedChars;
    private String currentAttempt = "";

    private double minAnimationSpeed = 20;   // fastest animation
    private double speedFactor = 1.0;        // adjusts based on length

    public BruteForceCodeCracker() {
        start(Main.getStage());
    }

    @Override
    public void start(Stage primaryStage) {

        primaryStage.setTitle("Brute Force Code Cracker");

        // Create UI components
        VBox mainLayout = createMainLayout();

        // Set up the scene
        Scene scene = new Scene(mainLayout, 600, 600);
        Main.getStage().setHeight(scene.getHeight());
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/styles/AnimatedStyles.css")).toExternalForm());
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private VBox createMainLayout() {
        // Target field section
        Label targetLabel = new Label("Enter target string to crack:");
        targetField = new PasswordField();
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


        // Set button actions
        startButton.setOnAction(e -> startBruteForce());
        stopButton.setOnAction(e -> stopBruteForce());

        Button exitButton = new Button("Exit");
        exitButton.setOnAction(e ->
                new MainScreen(SessionManager.getGroupType(), SessionManager.getStatus(), SessionManager.getUsername(), SessionManager.getName(), SessionManager.getConnection(), Main.getStage()).getDashboardScreen()
        );
        HBox buttonBox = new HBox(10, startButton, stopButton, exitButton);
        buttonBox.setAlignment(Pos.CENTER);
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
        if (target.length() > 50) {
            showAlert(Alert.AlertType.ERROR, "Too long!", "Please enter a target string less than 51 characters in length.");
            return;
        } else if (target.length() < 8) {
            showAlert(Alert.AlertType.ERROR, "Too short!", "Please enter a target string greater than 8 characters in length.");
            return;
        }

        // Check if too long (to prevent unreasonable cracking times)
        if (target.length() > 8) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION,
                    "The target is " + target.length() + " characters long. Brute forcing may take an extremely long time.\n" +
                            "Are you sure you want to proceed?",
                    ButtonType.YES, ButtonType.NO);
            FXAlertStyler.style(alert);
            alert.setTitle("Confirmation");
            alert.setHeaderText("Long Target Warning");

            alert.showAndWait().ifPresent(response -> {
                if (response == ButtonType.YES) {
                    if (target.length() > 10) {
                        Alert alert1 = new Alert(Alert.AlertType.INFORMATION, "Since the target is longer than 10 characters, it might go off screen. To see the whole target, please maximize the window");
                        FXAlertStyler.style(alert1);
                        alert1.setTitle("Maximize the Screen to see the whole target");
                        alert1.setHeaderText("Long Target Warning");
                        alert1.showAndWait();
                    }
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
        setupAnimatedDisplay(8);

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
        Platform.runLater(() -> {
            logArea.appendText("Starting brute force attack on: " + target + "\n");
            logArea.appendText("Character set size: " + CHARSET.length() + "\n");
            logArea.appendText("Estimated max attempts: " + Math.pow(CHARSET.length(), target.length()) + "\n");
            logArea.appendText("----------------------------------------\n");
        });

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
// Reset any styling classes from previous runs
        animatedDisplayBox.getStyleClass().removeAll("result-fast", "result-medium", "result-slow");
        // Make sure the default animation styling is applied
        if (!animatedDisplayBox.getStyleClass().contains("animated-display")) {
            animatedDisplayBox.getStyleClass().add("animated-display");
        }

        // Create a new timeline for the animation
        // Animation speed controls
        // ms between updates
        double baseAnimationSpeed = 100;
        animationTimeline = new Timeline(
                new KeyFrame(Duration.millis(baseAnimationSpeed * speedFactor), event -> {
                    // Update each character with random values except for known correct positions
                    for (int i = 0; i < animatedChars.length; i++) {
                        // Check if this position has been locked (correct character found)
                        boolean isLocked = i < currentAttempt.length() &&
                                i < bruteForceService.getTargetPassword().length() &&
                                currentAttempt.charAt(i) == bruteForceService.getTargetPassword().charAt(i);

                        if (isLocked) {
                            // Keep locked positions showing their correct character
                            animatedChars[i].setText(String.valueOf(currentAttempt.charAt(i)));
                        } else {
                            // Always show random characters at unlocked positions (100% of the time)
                            char randomChar = CHARSET.charAt(random.nextInt(CHARSET.length()));
                            animatedChars[i].setText(String.valueOf(randomChar));
                        }
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

    private void displayCrackedPassword(String password, long attempts, long durationMillis) {
        stopAnimation();
        animatedDisplayBox.getChildren().clear();

        // Set color based on duration or attempt thresholds
        animatedDisplayBox.getStyleClass().removeAll("result-fast", "result-medium", "result-slow");

        if (durationMillis < 5000 || attempts < 600) {
            animatedDisplayBox.getStyleClass().add("result-fast");
        } else if (durationMillis < 10000 || attempts < 1000) {
            animatedDisplayBox.getStyleClass().add("result-medium");
        } else {
            animatedDisplayBox.getStyleClass().add("result-slow");
        }

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
        displayCrackedPassword(result.crackedPassword(), result.attempts(), result.durationMillis());

        // Display results
        attemptsLabel.setText("Attempts: " + result.attempts);
        timeLabel.setText("Time: " + formatDuration(result.durationMillis));
        Platform.runLater(() -> {
            logArea.appendText("----------------------------------------\n");
            logArea.appendText("🎉 SUCCESS! Password cracked: " + result.crackedPassword + "\n");
            logArea.appendText("Total attempts: " + result.attempts + "\n");
            logArea.appendText("Time taken: " + formatDuration(result.durationMillis) + "\n");

            // Calculate and display statistics
            double attemptsPerSecond = result.attempts / (result.durationMillis / 1000.0);
            logArea.appendText("Speed: " + String.format("%.2f", attemptsPerSecond) + " attempts/second\n");
        });
        String strength = "";
        if (result.durationMillis < 5000 || result.attempts < 600) {
            strength = "Weak";
        } else if (result.durationMillis < 10000 || result.attempts < 1000) {
            strength = "Medium";
        } else {
            strength = "Strong";
        }
        // Show success dialog
        Alert cracked = new Alert(Alert.AlertType.INFORMATION,
                "Password cracked: " + result.crackedPassword + "\n" +
                        "Attempts: " + result.attempts + "\n" +
                        "Time: " + formatDuration(result.durationMillis) + "\n Your password Strength is: " + strength);
        FXAlertStyler.style(cracked);
        cracked.setTitle("Success");
        cracked.setHeaderText("Password cracked");
        cracked.getDialogPane().setPrefWidth(450);
        cracked.getDialogPane().setPrefHeight(250);
        cracked.showAndWait();
    }

    private void handleBruteForceError(Throwable exception) {
        // Update UI
        progressBar.progressProperty().unbind();
        startButton.setDisable(false);
        stopButton.setDisable(true);
        targetField.setDisable(false);

        // Stop animation
        stopAnimation();
        Platform.runLater(() -> {
            // Log error
            logArea.appendText("ERROR: " + exception.getMessage() + "\n");
        });

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

        Platform.runLater(() -> {
            // Log cancellation
            logArea.appendText("Brute force attack cancelled by user.\n");
        });
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
        FXAlertStyler.style(alert);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    public double getMinAnimationSpeed() {
        return minAnimationSpeed;
    }

    public void setMinAnimationSpeed(double minAnimationSpeed) {
        this.minAnimationSpeed = minAnimationSpeed;
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

                    int maxLength = 50;
                    int currentLength = 8;  // Initialize with the target password length
                    updateProgress(currentLength - 1, maxLength);
                    updateMessage("Trying passwords of length " + currentLength + "...");
                    // Initialize arrays for maximum possible length to preserve state
                    int[] indices = new int[maxLength];
                    char[] current = new char[maxLength];
                    boolean[] locked = new boolean[maxLength];  // Array to track locked characters

                    // Initialize with first character
                    for (int i = 0; i < maxLength; i++) {
                        indices[i] = 0;
                        current[i] = CHARSET.charAt(0);
                        locked[i] = false;  // Initially, all characters are not locked
                    }

                    while (currentLength <= maxLength && isRunning.get()) {
                        updateMessage("Extending search to length " + currentLength + "...");
                        int finalCurrentLength1 = currentLength;
                        Platform.runLater(() -> {
                            logArea.appendText("Trying passwords of length " + finalCurrentLength1 + "...\n");
                        });
                        boolean allCurrentLengthLocked = false;

                        while (!allCurrentLengthLocked && isRunning.get()) {
                            attempts++;

                            // For unlocked positions, always choose completely random characters
                            // instead of sequential iteration
                            for (int i = 0; i < currentLength; i++) {
                                if (!locked[i]) {  // Only change the character if it's not locked
                                    // 100% of the time use random characters instead of sequential indices
                                    current[i] = CHARSET.charAt(random.nextInt(CHARSET.length()));
                                }
                            }

                            // Convert current array to string, but only up to the current length
                            String attempt = new String(current, 0, currentLength);

                            // Check if the attempt matches the target password
                            if (attempt.equals(targetPassword)) {
                                long endTime = System.currentTimeMillis();
                                return new BruteForceResult(attempt, attempts, endTime - startTime);
                            }

                            // Update UI more frequently (every 50ms or 1000 attempts)
                            if (attempts % 1000 == 0 || (System.currentTimeMillis() - lastUpdateTime > 50)) {
                                long currentTime = System.currentTimeMillis();
                                double attemptsPerSecond = (attempts - lastUpdateAttempts) /
                                        ((currentTime - lastUpdateTime) / 1000.0);

                                Platform.runLater(() -> currentAttempt = attempt);

                                updateMessage(String.format("Trying: %s (%,d attempts, %.2f/sec)",
                                        attempt, attempts, attemptsPerSecond));

                                long finalAttempts = attempts;
                                Platform.runLater(() -> {
                                    attemptsLabel.setText(String.format("Attempts: %,d", finalAttempts));
                                    timeLabel.setText("Time: " + formatDuration(currentTime - startTime));
                                });

                                double progress = (currentLength - 8) + (double) attempts / Math.pow(CHARSET.length(), currentLength);
                                updateProgress(progress, maxLength - 8);

                                lastUpdateTime = currentTime;
                                lastUpdateAttempts = attempts;

                                if (isCancelled()) {
                                    updateMessage("Task cancelled");
                                    return null;
                                }
                            }

                            // If we've found the correct character, lock it
                            for (int i = 0; i < currentLength; i++) {
                                if (i < targetPassword.length() && !locked[i] && current[i] == targetPassword.charAt(i)) {
                                    locked[i] = true;  // Lock the correct character
                                    updateMessage("Character at position " + (i + 1) + " locked: " + current[i]);
                                }
                            }

                            // Check if all characters for this length are locked
                            allCurrentLengthLocked = true;
                            for (int i = 0; i < currentLength; i++) {
                                if (i < targetPassword.length() && !locked[i]) {
                                    allCurrentLengthLocked = false;
                                    break;
                                }
                            }

                            // Introduce a small delay to simulate realistic guessing behavior
                            try {
                                //noinspection BusyWait
                                Thread.sleep(10);  // Sleep for 10 milliseconds to slow down the brute force
                            } catch (InterruptedException e) {
                                // Handle interruption
                            }
                        }

                        // If all characters are locked but we haven't found the password yet,
                        // increase the length and continue
                        currentLength++;

                        // Update the animated display for the new length
                        if (currentLength <= maxLength && allCurrentLengthLocked) {
                            int finalCurrentLength = currentLength;
                            Platform.runLater(() -> setupAnimatedDisplay(finalCurrentLength));
                        }
                    }

                    // If we've reached max length and still haven't found the password
                    if (currentLength > maxLength) {
                        throw new RuntimeException("Password not found after trying all lengths up to 21 characters");
                    }

                    throw new RuntimeException("Password not found in character set after " + attempts + " attempts");
                }
            };
        }

        public CharSequence getTargetPassword() {
            return targetPassword;
        }
    }


    /**
     * Class to hold the result of a brute force operation
     */
    private record BruteForceResult(String crackedPassword, long attempts, long durationMillis) {
    }

    public static void main(String[] args) {
        launch(args);
    }
}