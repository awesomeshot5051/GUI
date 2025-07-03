package com.awesomeshot5051;

import com.awesomeshot5051.separatedFiles.Styler.*;
import javafx.application.*;
import javafx.scene.control.*;
import javafx.stage.*;

import java.awt.*;
import java.io.*;
import java.net.*;
import java.net.http.*;
import java.util.*;
import java.util.concurrent.*;

public class UpdateChecker {
    private static final String UPDATE_URL = "https://api.github.com/repos/awesomeshot5051/GUI/releases/latest";

    public static void checkForUpdate(Stage owner) {
        CompletableFuture.runAsync(() -> {
            try {
                HttpClient client = HttpClient.newHttpClient();
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(UPDATE_URL))
                        .header("Accept", "application/vnd.github.v3+json")
                        .build();

                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() == 200) {
                    String json = response.body();

                    // Extract the version and download URL from the JSON manually
                    String latestTag = json.split("\"tag_name\"\\s*:\\s*\"")[1].split("\"")[0];
                    String latestUrl = json.split("\"html_url\"\\s*:\\s*\"")[1].split("\"")[0];

                    String currentVersion = VersionHelper.getCurrentVersion().replace("v", "");
                    String latestVersion = latestTag.replace("v", "");

                    if (isNewerVersion(latestVersion, currentVersion)) {
                        Platform.runLater(() -> {
                            Alert alert = new Alert(Alert.AlertType.INFORMATION);
                            alert.setTitle("Update Available");
                            alert.setHeaderText("A new version is available!");
                            alert.setContentText("Current version: " + currentVersion + "\nLatest version: " + latestVersion);
                            FXAlertStyler.style(alert);

                            ButtonType downloadButton = new ButtonType("Download", ButtonBar.ButtonData.OK_DONE);
                            alert.getButtonTypes().setAll(downloadButton, ButtonType.CLOSE);

                            Optional<ButtonType> result = alert.showAndWait();
                            if (result.isPresent() && result.get() == downloadButton) {
                                try {
                                    Desktop.getDesktop().browse(URI.create(latestUrl));
                                } catch (IOException e) {
                                    Main.getErrorLogger().handleException("Failed to open browser", e);
                                }
                            }
                        });
                    }
                }
            } catch (Exception e) {
                Main.getErrorLogger().silentlyHandle(e); // Optional: log silently
            }
        });
    }

    private static boolean isNewerVersion(String remote, String local) {
        String[] r = remote.split("\\.");
        String[] l = local.split("\\.");
        for (int i = 0; i < Math.max(r.length, l.length); i++) {
            int rv = i < r.length ? Integer.parseInt(r[i]) : 0;
            int lv = i < l.length ? Integer.parseInt(l[i]) : 0;
            if (rv > lv) return true;
            if (rv < lv) return false;
        }
        return false;
    }
}
