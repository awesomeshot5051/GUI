package com.awesomeshot5051;

import javafx.application.Application;
import javafx.stage.*;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Objects;

public class Launcher extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
        // Path to your classes directory or jar file
        String path = "D:\\GUI\\build\\classes\\java\\main\\com\\awesomeshot5051";
        File dir = new File(path);
        
        // Find all classes in the package
        findAndRunMainClassInPackage(dir);
    }

    private void findAndRunMainClassInPackage(File dir) throws Exception {
        File[] files = dir.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    findAndRunMainClassInPackage(file); // Recursively check subdirectories
                } else if (file.getName().endsWith(".class")) {
                    String className = file.getName().replace(".class", "");
                    Class<?> clazz = Class.forName("com.awesomeshot5051." + className);

                    // Check if class has main method
                    try {
                        Method mainMethod = clazz.getMethod("main", String[].class);
                        mainMethod.invoke(null, (Object) new String[]{}); // Invoke main method
                    } catch (NoSuchMethodException e) {
                        // No main method, continue searching
                    }
                }
            }
        }
    }

    public static void main(String[] args) {
        launch(Main.class,args); // This starts the JavaFX application
    }
}
