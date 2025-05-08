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

public class SystemConfigurationGUI {
    // GUI for changing the password
    public void SystemConfigurationMainGUI() {
        Stage stage = Main.getStage();
        stage.setTitle("System Configuration");
        Button changePasswordExpirationButton = new Button("Change Password Expiration");
        changePasswordExpirationButton.setOnAction(e -> new ChangePasswordExpiration().ChangePasswordExpirationGUI());
        Button changePasswordComplexityButton = new Button("Change Password Complexity");
        Button changePasswordHistoryLimit = new Button("Change Password History Limit");
        changePasswordHistoryLimit.setOnAction(e -> new ChangePasswordHistory().ChangePasswordHistoryGUI());
        VBox layout = getVBox(changePasswordExpirationButton, changePasswordComplexityButton, changePasswordHistoryLimit);

        stage.setScene(new Scene(layout, 300, 250));
        stage.show();
    }

    private static VBox getVBox(Button changePasswordExpirationButton, Button changePasswordComplexityButton, Button changePasswordHistoryRequirementsButton) {
        Button exitButton = new Button("Exit");
        exitButton.setOnAction(e -> {
            new MainScreen(SessionManager.getGroupType(), SessionManager.getStatus(), SessionManager.getUsername(), SessionManager.getName(), SessionManager.getConnection(), Main.getStage());
        });

        VBox layout = new VBox(10, changePasswordExpirationButton, changePasswordComplexityButton, changePasswordHistoryRequirementsButton, exitButton);
        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new Insets(20));
        return layout;
    }
}
