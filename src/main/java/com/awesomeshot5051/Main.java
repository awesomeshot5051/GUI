package com.awesomeshot5051;

import com.awesomeshot5051.separatedFiles.*;
import com.awesomeshot5051.separatedFiles.Styler.*;
import com.awesomeshot5051.separatedFiles.defaultLoginCheck.*;
import com.awesomeshot5051.separatedFiles.logs.*;
import com.awesomeshot5051.separatedFiles.personalInfo.*;
import com.awesomeshot5051.separatedFiles.session.*;
import com.awesomeshot5051.separatedFiles.systemConfiguration.passwordManagement.*;
import com.awesomeshot5051.separatedFiles.userValidation.*;
import javafx.application.*;
import javafx.geometry.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.image.*;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import javafx.stage.*;

import java.io.*;
import java.nio.file.*;
import java.sql.*;
import java.util.*;
import java.util.logging.*;

import static com.awesomeshot5051.Launcher.*;

public class Main extends Application {
    private static final Logger LOGGER = new logger().makeLogger();
    private static Connection connection;
    private static final ErrorLogger ERROR_LOGGER = new ErrorLogger();
    private static Main instance;
    private static Stage primaryStage;

    public static Main getInstance() {
        if (instance == null) {
            instance = new Main();
        }
        return instance;
    }


    public static Logger getLogger() {
        return LOGGER;
    }

    public static ErrorLogger getErrorLogger() {
        return ERROR_LOGGER;
    }

    public static void main(String[] args) {
        launch(args);
    }

    public static void setConnection(Connection connection) {
        Main.connection = connection;
    }

    public static Connection getConnection() {
        return connection;
    }


    // Connect to the MySQL database
    public void connectToDatabase() throws SQLException {
        String url = "jdbc:mysql://mysql-java-hosting-javaguidbhosting.d.aivencloud.com:11510/userdatabase?noAccessToProcedureBodies=true";
        connection = DriverManager.getConnection(url, "guest", serverPassword);
    }

    public void connectToDatabase(String filePath) throws SQLException, IOException {
        String url = "jdbc:mysql://mysql-javaguidbhosting.alwaysdata.net:3306/javaguidbhosting_userdatabase?noAccessToProcedureBodies=true";
        connection = DriverManager.getConnection(url, "409644_guest", new String(Files.readAllBytes(Paths.get(filePath))));
    }

    public void loginScreen() {
        Label userLabel = new Label("Username:");
        TextField username = new TextField();
        Label passLabel = new Label("Password:");
        PasswordField password = new PasswordField();
        Button loginButton = new Button("Login");

        loginButton.setOnAction(e -> handleLogin(username, password));
        username.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) handleLogin(username, password);
        });
        password.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) handleLogin(username, password);
        });

        VBox root = new VBox(10, userLabel, username, passLabel, password, loginButton);
        root.setAlignment(Pos.CENTER); // This centers the content *within* the VBox
        root.setPadding(new Insets(20));
        root.getStyleClass().add("root");

        // Create the scene with the desired initial size
        Scene loginScene = new Scene(root, 400, 300);
        loginScene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/styles/login.css")).toExternalForm());

        Stage stage = Main.getStage(); // Get the existing primary stage


        // 2. Set the scene on the stage
        stage.setScene(loginScene);

        // 3. Explicitly set the stage's width and height.
        //    It's important to do this *after* setting the scene, and preferably
        //    within Platform.runLater() to ensure the rendering engine has a chance
        //    to process the scene change.
        Platform.runLater(() -> {
            stage.setWidth(400);  // Set the desired width
            stage.setHeight(300); // Set the desired height
            stage.centerOnScreen(); // Center the stage on the screen
        });


        stage.setTitle("Login");
        stage.show();
    }

    // Handles login logic
    private void handleLogin(TextField username, PasswordField password) {
        CredentialChecker credentialChecker = new CredentialChecker();
        try {
            PasswordHasher passwordHasher = new PasswordHasher(password.getText());
            String fullySaltedPassword = passwordHasher.getUnsaltedHashedPassword() + passwordHasher.getSalt(username.getText());
            if (credentialChecker.validateCredentials(username.getText(), password.getText())) {
                if (!new StatusChecker(username.getText(), fullySaltedPassword).isDisabled()) {
                    UserValues userValues = new UserValues(username.getText(), fullySaltedPassword);

                    // Save session data
                    new SessionManager(
                            userValues.getUsername(),
                            userValues.getName(),
                            userValues.getStatus(),
                            userValues.getGroupType(),
                            connection
                    );
                    if (new GetPasswordExpiration(username.getText(), fullySaltedPassword).isExpired()) {
                        showAlert("Password Expired!", "You're password is expired! You must change it before continuing!");
                        new infoChangeGUI().showChangePasswordGUI();
                    }
                    // Open the main screen
                    new MainScreen(userValues.getGroupType(), userValues.getStatus(), userValues.getUsername(), userValues.getName(), connection, primaryStage);
                    PreparedStatement stmt = connection.prepareStatement("call updateLastLoginDate(CURRENT_DATE,?,?)");
                    stmt.setString(1, SessionManager.getName());
                    stmt.setString(2, SessionManager.getUsername());
                    stmt.execute();
                    LOGGER.info("Successful login by " + username.getText());
                } else {
                    // ❗ User is disabled
                    showAlert("Account Disabled", "This account is disabled!\nContact an administrator to enable it.");
                    LOGGER.warning("Disabled account login attempt by " + username.getText());
                }
            } else {
                // ❗ Invalid login credentials
                showAlert("Invalid Login", "Invalid username or password.");
                LOGGER.warning("Invalid login by " + username.getText());
            }
        } catch (SQLException ex) {
            ERROR_LOGGER.handleException("Error while checking credentials: ", ex);
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        FXAlertStyler.style(alert);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static Stage getStage() {
        return primaryStage;
    }

    // Start method to initialize the application
    @Override
    public void start(Stage primaryStage) throws SQLException {
        Main.primaryStage = primaryStage;
        connectToDatabase(); // Ensure connection before loading the UI
        Image icon = IconFinder.findIcon();
        if (icon != null) {
            primaryStage.getIcons().add(icon);
        } else {
            LOGGER.warning("Icon not found by IconFinder.");
        }
        DefaultAccountChecker.checkAndNotifyIfDefaultNeeded();
        loginScreen(); // Show the login screen
    }
}
