package com.awesomeshot5051;

import com.awesomeshot5051.separatedFiles.*;
import javafx.application.*;
import javafx.geometry.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.*;

import java.io.*;
import java.sql.*;

public class PasswordHasherGUI extends Application {

    @Override
    public void start(Stage stage) {
        // Create labels and text fields
        Label passwordLabel = new Label("Enter Password:");
        TextField passwordField = new TextField();

        Label usernameLabel = new Label("Enter Username (for existing salt):");
        TextField usernameField = new TextField();

        // Create radio buttons and toggle group
        RadioButton existingSaltRadio = new RadioButton("Generate password hash using existing salt");
        RadioButton newSaltRadio = new RadioButton("Generate password hash with a new salt");
        ToggleGroup toggleGroup = new ToggleGroup();
        existingSaltRadio.setToggleGroup(toggleGroup);
        newSaltRadio.setToggleGroup(toggleGroup);

        // Create generate button
        Button generateButton = new Button("Generate");

        // Event handling for button click
        generateButton.setOnAction(event -> {
            String password = passwordField.getText();
            PasswordHasher hasher = new PasswordHasher(password);

            if (existingSaltRadio.isSelected()) {
                String username = usernameField.getText();
                String existingSalt = hasher.getSalt(username);
                if (existingSalt != null) {
                    String hashedPassword = hasher.hashPassword(password + existingSalt);
                    System.out.println("Salt: " + existingSalt);
                    System.out.println("Hashed Password: " + hashedPassword);
                } else {
                    System.out.println("No salt found for the given username.");
                }
            } else if (newSaltRadio.isSelected()) {
                String hashedPassword = hasher.generateSaltedHashedPassword();
                String newSalt = hasher.getSalt();
                System.out.println("Salt: " + newSalt);
                System.out.println("Hashed Password: " + hashedPassword);
            }
        });

        // Layout setup
        VBox layout = new VBox(10, passwordLabel, passwordField, usernameLabel, usernameField, existingSaltRadio, newSaltRadio, generateButton);
        layout.setPadding(new Insets(15));

        // Scene and stage setup
        Scene scene = new Scene(layout, 400, 300);
        stage.setScene(scene);
        stage.setTitle("Password Hasher GUI");
        stage.show();
    }

    public static void main(String[] args) throws SQLException, IOException {
        new Main().connectToDatabase("someFilePath");
        launch(args);
    }
}