package com.awesomeshot5051.separatedFiles.session;

import com.awesomeshot5051.*;
import com.awesomeshot5051.separatedFiles.*;
import com.awesomeshot5051.separatedFiles.accesskey.*;
import com.awesomeshot5051.separatedFiles.group.*;
import com.awesomeshot5051.separatedFiles.userManagement.*;

import java.sql.*;

public class SessionManager {
    private static String username;
    private static String name;
    private static String status;
    private static IGroup IGroupType;
    private static Connection connection;
    private static boolean validAccessKey;
    private String password;
    private static User user;
    private static User originalAdminUser; // Stores the original admin session if they switch users

    public SessionManager(String username, String fullName, String userStatus, IGroup userIGroup, Connection conn) throws SQLException {
        SessionManager.username = username;
        name = fullName;
        status = userStatus;
        IGroupType = userIGroup;
        connection = conn;
        originalAdminUser = null; // Reset any previous switching session
        user = new User(fullName, username, userIGroup.getGroupName(), status);
        new AccessKeyVerification().accessKeyExists();
        validAccessKey = user.isAccessKeyValid();
    }


    // Getters
    public static String getUsername() {
        return username;
    }

    public static User getUser() {
        return user;
    }

    public static User getOriginalAdminUser() {
        return originalAdminUser;
    }

    public static String getName() {
        return name;
    }

    public static String getStatus() {
        return status;
    }

    public static IGroup getGroupType() {
        return IGroupType;
    }

    public static Connection getConnection() {
        return connection;
    }

    public static boolean isAccessKeyValid() {
        return validAccessKey;
    }

    public static void setAccessKeyValid(boolean valid) {
        validAccessKey = valid;
    }

    // Switch to another user's session (Admins/SuperAdmins only)
    public static void switchUser(String newUser) throws SQLException {
        if (originalAdminUser == null) {
            originalAdminUser = new User(getName(), getUsername(), getGroupType().getGroupName(), getStatus()); // Store original session only the first time
        }
        loadUserSession(newUser);
        Main.getLogger().info("Successful login by " + originalAdminUser.getUsername() + " as " + SessionManager.getUsername());
        Main.getStage().close();
        new MainScreen(IGroupType, status, username, name, connection, Main.getStage());
    }

    // Revert back to the original admin session
    public static void revertToAdmin() {
        if (originalAdminUser != null) {
            loadUserSession(originalAdminUser.getUsername());
            originalAdminUser = null; // Reset after switching back
        }
        Main.getStage().close();
        new MainScreen(IGroupType, status, username, name, connection, Main.getStage());
    }

    // Check if the user session is switched
    public static boolean isSwitchedUser() {
        return originalAdminUser != null;
    }

    // Load session details for a new user
    private static void loadUserSession(String newUser) {
        try (PreparedStatement stmt = connection.prepareStatement("SELECT * FROM users WHERE username = ?")) {
            stmt.setString(1, newUser);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                username = newUser;
                name = rs.getString("name");
                status = rs.getString("status");
                IGroupType = new Group(rs.getString("group")).getGroup(); // Assuming a method exists to convert string to IGroup
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
