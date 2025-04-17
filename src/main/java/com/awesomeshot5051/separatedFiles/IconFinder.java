package com.awesomeshot5051.separatedFiles;


import javafx.scene.image.*;

import java.io.*;
import java.net.*;

public class IconFinder {

    // Method to find the ETIcon.jpg file in resources/icon directory
    public static Image findIcon() {
        try {
            // First attempt: Try to load from resources using getResource with correct path
            URL resourceUrl = IconFinder.class.getClassLoader().getResource("icon/ETIcon.jpg");
            if (resourceUrl != null) {
                return new Image(resourceUrl.toExternalForm());
            }

            // Second attempt: Try to load from resources using getResourceAsStream with correct path
            InputStream inputStream = IconFinder.class.getClassLoader().getResourceAsStream("icon/ETIcon.jpg");
            if (inputStream != null) {
                Image image = new Image(inputStream);
                inputStream.close();
                return image;
            }

            // Third attempt: Try with absolute path to src/main/resources/icon
            String projectRoot = System.getProperty("user.dir");
            File iconFile = new File(projectRoot, "src/main/resources/icon/ETIcon.jpg");

            if (iconFile.exists()) {
                return new Image(iconFile.toURI().toString());
            }

            // Fourth attempt: Use the absolute path directly
            File absoluteIconFile = new File("D:/GUI/src/main/resources/icon/ETIcon.jpg");
            if (absoluteIconFile.exists()) {
                return new Image(absoluteIconFile.toURI().toString());
            }

            // Icon not found using any method
            System.out.println("ETIcon.jpg not found in resources/icon directory.");
            return null;

        } catch (Exception e) {
            System.out.println("Error loading icon: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

}