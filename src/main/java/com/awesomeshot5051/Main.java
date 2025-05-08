package com.awesomeshot5051;

import com.awesomeshot5051.separatedFiles.*;
import com.awesomeshot5051.separatedFiles.defaultLoginCheck.*;
import com.awesomeshot5051.separatedFiles.logs.*;
import com.awesomeshot5051.separatedFiles.personalInfo.*;
import com.awesomeshot5051.separatedFiles.session.*;
import com.awesomeshot5051.separatedFiles.systemConfiguration.passwordManagement.*;
import com.awesomeshot5051.separatedFiles.userValidation.*;
import javafx.application.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.image.*;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import javafx.stage.*;

import java.io.*;
import java.nio.file.*;
import java.sql.*;
import java.util.logging.*;

import static com.awesomeshot5051.Launcher.*;

public class Main extends Application {
    private static final Logger LOGGER = new logger().makeLogger();
    private static Connection connection;

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

    public static Connection getConnection() {
        return connection;
    }


    // Connect to the MySQL database
    public void connectToDatabase() throws SQLException {
        String url = "jdbc:mysql://mysql-javaguidbhosting.alwaysdata.net:3306/javaguidbhosting_userdatabase?noAccessToProcedureBodies=true";
        connection = DriverManager.getConnection(url, "409644_guest", serverPassword);
    }

    public void connectToDatabase(String filePath) throws SQLException, IOException {
        String url = "jdbc:mysql://mysql-javaguidbhosting.alwaysdata.net:3306/javaguidbhosting_userdatabase?noAccessToProcedureBodies=true";
        connection = DriverManager.getConnection(url, "409644_guest", new String(Files.readAllBytes(Paths.get("D:\\GUI\\src\\main\\resources\\guestPassword.txt"))));
    }

    // Setup the login screen
    public void loginScreen() {
        // Create UI elements
        Label userLabel = new Label("Username:");
        TextField username = new TextField();
        Label passLabel = new Label("Password:");
        PasswordField password = new PasswordField();
        Button loginButton = new Button("Login");

        // Set login action for button & enter key press
        loginButton.setOnAction(e -> handleLogin(username, password));
        username.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) handleLogin(username, password);
        });
        password.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) handleLogin(username, password);
        });

        // Use VBox for vertical layout
        VBox root = new VBox(10); // Spacing of 10px between elements
        root.getChildren().addAll(userLabel, username, passLabel, password, loginButton);

        // Create scene
        Scene scene = new Scene(root, 400, 300);

        // Configure stage
        primaryStage.setTitle("JavaFX App");
        primaryStage.setScene(scene);
        primaryStage.show();
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
            throw new RuntimeException(ex);
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR); // Use ERROR for login issues
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
