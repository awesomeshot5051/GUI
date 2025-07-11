package com.awesomeshot5051.separatedFiles.systemConfiguration.passwordManagement;

import com.awesomeshot5051.*;
import com.awesomeshot5051.separatedFiles.systemConfiguration.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.*;

import java.sql.*;

public class ChangePasswordHistory {

    private final Connection connection;

    public ChangePasswordHistory() {
        this.connection = Main.getConnection();
    }


    public void ChangePasswordHistoryGUI() {
        Stage stage = new Stage();
        stage.setTitle("System Configuration");
        Label passwordHistoryLimitLabel = new Label("Password History Limit:");
        passwordHistoryLimitLabel.setLabelFor(passwordHistoryLimitLabel);
        TextField passwordHistoryLimit = new TextField();
        VBox layout = getVBox(stage, passwordHistoryLimit);

        stage.setScene(new Scene(layout, 300, 250));
        stage.show();
    }

    private VBox getVBox(Stage stage, TextField passwordHistoryLimit) {
        Button set = new Button("Set");
        set.setOnAction(e -> {
            stage.close();
            try {
                updatePasswordHistory(Integer.parseInt(passwordHistoryLimit.getText()));
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
        });
        Button exit = new Button("Exit");
        exit.setOnAction(e -> {
            stage.close();
            new SystemConfigurationGUI().SystemConfigurationMainGUI();
        });
        VBox layout = new VBox(10, passwordHistoryLimit, set, exit);
        return layout;
    }

    private void updatePasswordHistory(int i) throws SQLException {
        PreparedStatement statement = connection.prepareStatement("call updateGlobalPasswordHistory(?)");
        statement.setString(1, String.valueOf(i));
        statement.execute();
    }
}
