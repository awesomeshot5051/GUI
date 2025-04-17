package com.awesomeshot5051.separatedFiles.userManagement;

import com.awesomeshot5051.separatedFiles.group.*;
import javafx.beans.property.*;

import java.sql.*;

public class User {
    private final StringProperty name;
    private final StringProperty username;
    private IGroup group; // Made this non-final to allow updates
    private final StringProperty status;
    private boolean modified;

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

    public String getName() {
        return name.get();
    }
}
