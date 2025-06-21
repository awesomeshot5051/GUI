package com.awesomeshot5051.separatedFiles.systemConfiguration;

import com.awesomeshot5051.*;
import com.awesomeshot5051.separatedFiles.*;
import com.awesomeshot5051.separatedFiles.session.*;
import com.awesomeshot5051.separatedFiles.systemConfiguration.passwordManagement.*;
import javafx.geometry.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.*;

import java.util.*;

public class SystemConfigurationGUI {
    // GUI for changing the password
    public void SystemConfigurationMainGUI() {
        Stage stage = Main.getStage();
        stage.setTitle("System Configuration");

        Button changePasswordExpirationButton = new Button("Change Password Expiration");
        changePasswordExpirationButton.setMaxWidth(Double.MAX_VALUE);
        changePasswordExpirationButton.setOnAction(e -> new ChangePasswordExpiration().ChangePasswordExpirationGUI());

        Button changePasswordComplexityButton = new Button("Change Password Complexity");
        changePasswordComplexityButton.setMaxWidth(Double.MAX_VALUE);
        // Add action here if needed

        Button changePasswordHistoryLimit = new Button("Change Password History Limit");
        changePasswordHistoryLimit.setMaxWidth(Double.MAX_VALUE);
        changePasswordHistoryLimit.setOnAction(e -> new ChangePasswordHistory().ChangePasswordHistoryGUI());

        VBox layout = getVBox(changePasswordExpirationButton, changePasswordComplexityButton, changePasswordHistoryLimit);

        Scene scene = new Scene(layout, 320, 280);
        // Load the consistent Styles.css
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/styles/Styles.css")).toExternalForm());

        stage.setScene(scene);
        stage.sizeToScene();
        stage.centerOnScreen();
        stage.show();
    }

    private static VBox getVBox(Button changePasswordExpirationButton, Button changePasswordComplexityButton, Button changePasswordHistoryRequirementsButton) {
        Button exitButton = new Button("Exit");
        exitButton.setMaxWidth(Double.MAX_VALUE);
        exitButton.setOnAction(e -> {
            // Rebuild the main screen with current session info
            new MainScreen(SessionManager.getGroupType(), SessionManager.getStatus(), SessionManager.getUsername(), SessionManager.getName(), SessionManager.getConnection(), Main.getStage());
        });

        VBox layout = new VBox(12, changePasswordExpirationButton, changePasswordComplexityButton, changePasswordHistoryRequirementsButton, exitButton);
        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new Insets(20));
        layout.setMaxWidth(280);
        return layout;
    }
}
