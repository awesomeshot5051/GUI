package com.awesomeshot5051.separatedFiles.group;

import javafx.beans.property.*;

import java.sql.*;

public class Group implements IGroup {
    private final IGroup group;

    public Group(String group) throws SQLException {
        this.group = switch (group) {
            case "Admin", "admin" -> new AdminIGroup();
            case "Standard", "standard" -> new StandardIGroup();
            case "SuperAdmin", "superadmin", "Superadmin", "superAdmin" -> new SuperAdminIGroup();
            case "Default", "default" -> new DefaultIGroup();
            default -> throw new SQLException("Unknown group: " + group);
        };
    }

    public IGroup getGroup() {
        return group;
    }

    public String getGroupName() {
        return group.getGroupName();
    }

    @Override
    public String getPermissions() {
        return group.getPermissions();
    }

    @Override
    public boolean canCreateUser() {
        return group.canCreateUser();
    }

    public static StringProperty toProperty(IGroup value) {
        return new SimpleStringProperty(value.getGroupName());
    }
}
