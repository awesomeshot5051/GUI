package com.awesomeshot5051.separatedFiles.extraStuff;

import com.awesomeshot5051.*;
import com.awesomeshot5051.separatedFiles.*;
import com.awesomeshot5051.separatedFiles.session.*;
import javafx.application.*;
import javafx.geometry.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.*;

import java.util.*;

public class NumberGame extends Application {

    private final Stage primaryStage;
    private int low, high, num;
    private TextArea thinkingTextArea;
    private TextField inputField;
    private Thread guessingThread;
    private Thread animationThread;

    public NumberGame() {
        this.primaryStage = Main.getStage();
        start(primaryStage);
    }

    @Override
    public void start(Stage primaryStage) {

        showMainMenu();
    }

    private void showMainMenu() {
        VBox menuBox = new VBox(10);
        menuBox.setPadding(new Insets(20));
        menuBox.getStyleClass().add("menu-box");

        Label header = new Label("Game Type");
        header.getStyleClass().add("header");

        Button computerGuessBtn = new Button("Computer Guesses");
        computerGuessBtn.setOnAction(e -> showComputerGuessScene());

        Button userGuessBtn = new Button("User Guesses");
        userGuessBtn.setOnAction(e -> userGuesses());

        Button userVsUserBtn = new Button("User VS User");
        userVsUserBtn.setOnAction(e -> userVsUser());

        Button exitBtn = new Button("Exit");
        exitBtn.setOnAction(e ->
                new MainScreen(SessionManager.getGroupType(), SessionManager.getStatus(), SessionManager.getUsername(), SessionManager.getName(), SessionManager.getConnection(), Main.getStage()).getDashboardScreen()
        );

        menuBox.getChildren().addAll(header, computerGuessBtn, userGuessBtn, userVsUserBtn, exitBtn);

        Scene scene = new Scene(menuBox, 300, 250);
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/styles/style.css")).toExternalForm());

        primaryStage.setScene(scene);
        primaryStage.setTitle("Number Game");
        primaryStage.show();
    }

    private void showComputerGuessScene() {
        low = 1;
        high = 100;

        VBox layout = new VBox(10);
        layout.setPadding(new Insets(15));
        layout.getStyleClass().add("game-box");

        HBox inputBox = new HBox(10);
        Label prompt = new Label("Enter a number (1–100):");
        inputField = new TextField();
        Button submitBtn = new Button("Submit");
        inputBox.getChildren().addAll(prompt, inputField, submitBtn);

        thinkingTextArea = new TextArea();
        thinkingTextArea.setEditable(false);
        thinkingTextArea.setWrapText(true);
        thinkingTextArea.setPrefHeight(200);
        VBox.setVgrow(thinkingTextArea, Priority.ALWAYS);
        thinkingTextArea.getStyleClass().add("thinking-area");


        Button backBtn = new Button("Back");
        backBtn.setOnAction(e -> showMainMenu());

        layout.getChildren().addAll(inputBox, thinkingTextArea, backBtn);

        submitBtn.setOnAction(e -> startThinkingAnimation());

        Scene scene = new Scene(layout, 400, 300);
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/styles/style.css")).toExternalForm());

        primaryStage.setScene(scene);
    }

    private void startThinkingAnimation() {
        try {
            int userNum = Integer.parseInt(inputField.getText());
            Random random = new Random();
            num = random.nextInt(low, high + 1);
                    guessingThread = new Thread(() -> {
                long realStart = System.currentTimeMillis();
                int guesses = 0;

                try {
                    while (num != userNum) {
                        if (num > userNum) {
                            high = num - 1;
                        } else {
                            low = num + 1;
                        }

                        num = random.nextInt(high - low + 1) + low;
                        guesses++;
                        // Simulate animation delay, not performance
////                        noinspection BusyWait
//                        Thread.sleep(1000);
                    }
                } catch (IllegalArgumentException e) {
                    Platform.runLater(() -> showAlert("Computer failed to guess the number. You win!"));
                    return;
                }

                long realEnd = System.currentTimeMillis();
                long timeElapsed = (realEnd - realStart) / 1000;

                final String result = "Computer guessed your number in " + guesses + " attempts.\n"
                        + "Time elapsed: " + timeElapsed + " seconds.\nYour number was: " + num;

                Platform.runLater(() -> showAlert(result));
            });


            animationThread = new Thread(() -> {
                String thinking = "thinking";
                while (!Thread.currentThread().isInterrupted()) {
                    String finalThinking = thinking;
                    Platform.runLater(() -> thinkingTextArea.setText(finalThinking));
                    thinking = thinking.substring(1) + thinking.charAt(0);
//                    try {
//                        //noinspection BusyWait
//                        Thread.sleep(300);
//                    } catch (InterruptedException e) {
//                        break;
//                    }
                }
            });

            inputField.setDisable(true);
            guessingThread.start();
            animationThread.start();

        } catch (NumberFormatException e) {
            showAlert("Please enter a valid number.");
        }
    }

    private void showAlert(String message) {
        if (guessingThread != null) guessingThread.interrupt();
        if (animationThread != null) animationThread.interrupt();

        Alert alert = new Alert(Alert.AlertType.INFORMATION, message, ButtonType.OK);
        alert.setTitle("Result");
        alert.setHeaderText(null);
        alert.showAndWait();
        showMainMenu();
    }

    private void userGuesses() {
        Random random = new Random();
        int target = random.nextInt(101);
        int invalidAttempts = 3;
        int attempts = 0;
        do {
            TextInputDialog dialog = new TextInputDialog();
            dialog.setTitle("Guess the number (0-100):");
            dialog.setHeaderText(attempts == 0 ? "Guess the Number" : "Guess the Number:\n(Attempt " + (attempts + 1) + " of 10)");
            Optional<String> input = dialog.showAndWait();

            if (input.isEmpty()) {
                showMainMenu();
                break;
            }

            try {
                int guess = Integer.parseInt(input.get());
                if (guess < 0 || guess > 100) {
                    throw new NumberOutOfRangeException("Number must be between 0 and 100");
                }

                if (guess > target) {
                    showTempMessage("Too high! Try again.");
                    attempts++;
                } else if (guess < target) {
                    showTempMessage("Too low! Try again.");
                    attempts++;
                } else {
                    showAlert("Congratulations! You guessed it.");
                    break;
                }
                if (attempts >= 10) {
                    showAlert("You have exceeded the maximum number of attempts. Computer Wins.");
                    break;
                }
            } catch (NumberFormatException e) {
                invalidAttempts--;
                if (invalidAttempts > 0) {
                    showTempMessage("Invalid input! Please enter a number.\n" + invalidAttempts + " invalid attempts remaining.\nIf you run out of invalid attempts, \nyou will get an attempt penalty.");
                } else {
                    showTempMessage("Invalid input. An attempt will be added");
                    attempts++;
                }

            } catch (NumberOutOfRangeException e) {
                invalidAttempts--;
                if (invalidAttempts > 0) {
                    showTempMessage("Invalid input! " + e.getMessage() + "." + "\n" + invalidAttempts + " invalid attempts remaining.\nIf you run out of invalid attempts, \nyou will get an attempt penalty.");
                } else {
                    showTempMessage("Invalid input. An attempt will be added");
                    attempts++;
                }
            }
        } while (attempts < 11);
    }

    private void userVsUser() {
        int target;
        TextInputDialog setterDialog = new TextInputDialog();
        setterDialog.setHeaderText("Player 1: Enter a number between 0 and 100");
        Optional<String> input = setterDialog.showAndWait();

        if (input.isEmpty()) {
            showMainMenu();
            return;
        }

        try {
            target = Integer.parseInt(input.get());
            if (target < 0 || target > 100) {
                showAlert("Number must be between 0 and 100.");
                userVsUser();
                return;
            }
        } catch (NumberFormatException e) {
            showAlert("Invalid number.");
            userVsUser();
            return;
        }

        while (true) {
            TextInputDialog guessDialog = new TextInputDialog();
            guessDialog.setHeaderText("Player 2: Guess the number:");
            Optional<String> guessInput = guessDialog.showAndWait();

            if (guessInput.isEmpty()) {
                showMainMenu();
                return;
            }

            try {
                int guess = Integer.parseInt(guessInput.get());

                if (guess > target) {
                    showTempMessage("Too high!");
                } else if (guess < target) {
                    showTempMessage("Too low!");
                } else {
                    showAlert("Correct! Player 2 wins!");
                    return;
                }
            } catch (NumberFormatException e) {
                showTempMessage("Invalid number.");
            }
        }
    }

    private void showTempMessage(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, message, ButtonType.OK);
        alert.setTitle("Hint");
        alert.setHeaderText(null);
        alert.showAndWait();
    }

}