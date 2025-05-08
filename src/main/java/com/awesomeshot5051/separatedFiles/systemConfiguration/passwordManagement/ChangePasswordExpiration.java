package com.awesomeshot5051.separatedFiles.systemConfiguration.passwordManagement;

import com.awesomeshot5051.*;
import com.awesomeshot5051.separatedFiles.systemConfiguration.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.*;

import java.sql.*;

public class ChangePasswordExpiration {

    private Connection connection;

    public ChangePasswordExpiration() {
        this.connection = Main.getConnection();
    }

    public void ChangePasswordExpirationGUI() {
        Stage stage = new Stage();
        stage.setTitle("System Configuration");
        TextField expirationDays = new TextField();
        Button set = new Button("Set");
        set.setOnAction(e -> {
            stage.close();
            try {
                updateDatabaseExpiration(Integer.parseInt(expirationDays.getText()));
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
        });
        Button exit = new Button("Exit");
        exit.setOnAction(e -> {
            stage.close();
            new SystemConfigurationGUI().SystemConfigurationMainGUI();
        });
        VBox layout = new VBox(10, expirationDays, set, exit);

        stage.setScene(new Scene(layout, 300, 250));
        stage.show();
    }

    private void updateDatabaseExpiration(int days) throws SQLException {
        PreparedStatement statement = connection.prepareStatement("call updateGlobalExpirationTime(?)");
        statement.setString(1, String.valueOf(days));
        statement.execute();
    }

}
