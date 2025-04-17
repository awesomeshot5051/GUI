package com.awesomeshot5051.separatedFiles.userManagement;

import com.awesomeshot5051.separatedFiles.*;
import com.awesomeshot5051.separatedFiles.group.*;
import com.awesomeshot5051.separatedFiles.session.*;
import javafx.stage.*;

import java.sql.*;

public class ManageUsers {
    private final Stage primaryStage;

    public ManageUsers(Stage primaryStage) {
        this.primaryStage = primaryStage;
        showUserList();
    }

    public void showUserList() {
        UserListView userListView = new UserListView(primaryStage);
        userListView.show();
    }

    public void showUpdateUserGroupView() {
        UpdateUserGroupView updateUserGroupView = new UpdateUserGroupView(primaryStage, this);
        updateUserGroupView.show();
    }

    public void exitUserManagement() {
        // Retrieve user details from session
        String username = SessionManager.getUsername();
        String name = SessionManager.getName();
        String status = SessionManager.getStatus();
        IGroup IGroupType = SessionManager.getGroupType();
        Connection connection = SessionManager.getConnection();

        // Ensure values are valid before switching screens
        if (username != null && name != null && status != null && IGroupType != null && connection != null) {
            // Create and display the main screen
            new MainScreen(IGroupType, status, username, name, connection, primaryStage);
        } else {
            System.err.println("Session data is missing. Cannot return to main screen.");
            primaryStage.close();
        }
    }
}
