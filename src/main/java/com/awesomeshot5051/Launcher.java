package com.awesomeshot5051;

import com.awesomeshot5051.separatedFiles.systemConfiguration.database.*;

import java.io.*;
import java.nio.charset.*;

import static javafx.application.Application.*;

public class Launcher {
    public static String serverPassword;

    public static void main(String[] args) throws IOException {
        InputStream in = DatabaseGUI.class.getResourceAsStream("/guestPassword.txt");
        if (in == null) {
            throw new FileNotFoundException("password.txt not found in resources.");
        }
        serverPassword = new String(in.readAllBytes(), StandardCharsets.UTF_8);
        launch(Main.class, args); // This starts the JavaFX application
    }
}
