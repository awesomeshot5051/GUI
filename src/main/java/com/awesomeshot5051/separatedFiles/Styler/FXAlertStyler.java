package com.awesomeshot5051.separatedFiles.Styler;

import com.awesomeshot5051.separatedFiles.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.stage.*;

import java.util.*;

public class FXAlertStyler {
    public static void style(Alert alert) {
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.getStylesheets().add(
                Objects.requireNonNull(FXAlertStyler.class.getResource("/styles/Styles.css")).toExternalForm()
        );
        Stage alertStage = (Stage) alert.getDialogPane().getScene().getWindow();
        alertStage.getIcons().add(IconFinder.findIcon());
        // Apply dark background to the whole dialog
        dialogPane.getStyleClass().add("form-container");

        // Fix bright header background
        Node header = dialogPane.lookup(".header-panel");
        if (header != null) {
            header.setStyle("-fx-background-color: transparent;");
        }

        // Style the title label
        Node title = dialogPane.lookup(".header-panel .label");
        if (title != null) {
            title.setStyle("-fx-text-fill: white;");
        }

        // Style the content text
        Node content = dialogPane.lookup(".content");
        if (content != null) {
            content.setStyle("-fx-text-fill: white;");
        }
    }

    public static void style(ChoiceDialog<String> choiceDialog) {
        DialogPane dialogPane = choiceDialog.getDialogPane();
        dialogPane.getStylesheets().add(
                Objects.requireNonNull(FXAlertStyler.class.getResource("/styles/Styles.css")).toExternalForm()
        );

        // Apply dark background to the whole dialog
        dialogPane.getStyleClass().add("form-container");

        // Fix bright header background
        Node header = dialogPane.lookup(".header-panel");
        if (header != null) {
            header.setStyle("-fx-background-color: transparent;");
        }

        // Style the title label
        Node title = dialogPane.lookup(".header-panel .label");
        if (title != null) {
            title.setStyle("-fx-text-fill: white;");
        }

        // Style the content text
        Node content = dialogPane.lookup(".content");
        if (content != null) {
            content.setStyle("-fx-text-fill: white;");
        }
    }
}

