package com.awesomeshot5051.separatedFiles.userManagement;

import com.awesomeshot5051.separatedFiles.group.Group;
import com.awesomeshot5051.separatedFiles.group.IGroup;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.sql.SQLException;

public class User {
    private final StringProperty name;
    private final StringProperty username;
    private IGroup group; // Made this non-final to allow updates
    private final StringProperty status;
    private boolean modified;
    private final StringProperty passwordExpiration = new SimpleStringProperty();
    private boolean expirationModified = false;
    private boolean isAccessKeyValid;

    public User(String name, String username, String group, String status) throws SQLException {
        this.name = new SimpleStringProperty(name);
        this.username = new SimpleStringProperty(username);
        this.group = new Group(group).getGroup();
        this.status = new SimpleStringProperty(status);
        this.modified = false;
    }

    public StringProperty nameProperty() {
        return name;
    }

    public StringProperty usernameProperty() {
        return username;
    }

    public StringProperty groupProperty() {
        return Group.toProperty(group);
    }

    public StringProperty statusProperty() {
        return status;
    }

    public String getUsername() {
        return username.get();
    }

    public IGroup getGroup() {
        return group;
    }

    public void setGroup(String newGroup) throws SQLException {
        this.group = new Group(newGroup).getGroup();
    }

    public void setAccessKeyValid(boolean valid) {
        isAccessKeyValid = valid;
    }

    public boolean isAccessKeyValid() {
        return isAccessKeyValid;
    }

    public void setStatus(String newStatus) {
        this.status.set(newStatus);
    }

    public String getStatus() {
        return status.get();
    }


    public boolean isModified() {
        return modified;
    }

    public void setModified(boolean modified) {
        this.modified = modified;
    }


    public StringProperty passwordExpirationProperty() {
        return passwordExpiration;
    }

    public String getPasswordExpiration() {
        return passwordExpiration.get();
    }

    public void setPasswordExpiration(String expiration) {
        this.passwordExpiration.set(expiration);
        this.expirationModified = true;
    }

    public boolean isExpirationModified() {
        return expirationModified;
    }

    public void setExpirationModified(boolean modified) {
        this.expirationModified = modified;
    }

    private final StringProperty lastLogin = new SimpleStringProperty();

    public StringProperty lastLoginProperty() {
        return lastLogin;
    }

    public String getLastLogin() {
        return lastLogin.get();
    }

    public void setLastLogin(String lastLogin) {
        this.lastLogin.set(lastLogin);
    }

    public String getName() {
        return name.get();
    }
}
