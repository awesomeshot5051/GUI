package com.awesomeshot5051;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static javafx.application.Application.launch;

public class Launcher {
    public static String serverPassword;

    public static void main(String[] args) throws IOException {
        serverPassword = new String(Files.readAllBytes(Paths.get("D:\\GUI\\src\\main\\resources\\guestPassword.txt")));
        launch(Main.class, args); // This starts the JavaFX application
    }
}
