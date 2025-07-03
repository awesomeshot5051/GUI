package com.awesomeshot5051;

import java.io.*;
import java.util.*;

public class VersionHelper {
    public static String getCurrentVersion() {
        try (InputStream in = VersionHelper.class.getResourceAsStream("/version.properties")) {
            Properties props = new Properties();
            props.load(in);
            return props.getProperty("app.version", "unknown");
        } catch (IOException e) {
            return "unknown";
        }
    }
}
