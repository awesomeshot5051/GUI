package com.awesomeshot5051.separatedFiles;

import com.awesomeshot5051.separatedFiles.dashboards.*;
import com.awesomeshot5051.separatedFiles.group.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.stage.*;

import java.sql.*;

public class MainScreen {
    private final IGroup IGroupType;
    private final String status;
    private final String username;
    private final String name;
    private final Connection connection;

    public MainScreen(IGroup IGroupType, String status, String username, String name, Connection connection, Stage stage) {
        this.connection = connection;
        this.IGroupType = IGroupType;
        this.status = status;
        this.username = username;
        this.name = name;
        setUpMainScreen(stage);
    }

    // com.awesomeshot5051.separatedFiles.dashboards.DashboardScreen Interface (defined in separate classes)
    public interface DashboardScreen {
        Parent getView();

        Button getLogoutButton();
    }

    // Redirection Logic to Load Different Screens Based on GroupType
    public void setUpMainScreen(Stage stage) {
        DashboardScreen dashboardScreen = getDashboardScreen();
        Scene dashboardScene = new Scene(dashboardScreen.getView(), 600, 400);

        // Set up the main stage
        stage.setScene(dashboardScene);
        stage.setTitle(IGroupType.getGroupName() + " Dashboard");
        stage.setWidth(1200);   // Much larger width
        stage.setHeight(800);   // Much larger height
        stage.setMinWidth(900); // Minimum width so it can't be too small
        stage.setMinHeight(700); // Minimum height so it can't be too small
        stage.setMaximized(false); // Optional: prevent JavaFX from maximizing unexpectedly
        stage.centerOnScreen();    // Optional: recenter
        stage.show();
    }

    // Method to determine the appropriate dashboard screen based on IGroupType
    public DashboardScreen getDashboardScreen() {

        return switch (IGroupType) {
            case AdminIGroup adminIGroup -> new AdminDashboard();
            case SuperAdminIGroup superAdminIGroup -> new SuperAdminDashboard();
            case StandardIGroup standardIGroup -> new StandardDashboard();
            case null, default ->   // Default group or fallback
                    new DefaultDashboard();
        };
    }
}

