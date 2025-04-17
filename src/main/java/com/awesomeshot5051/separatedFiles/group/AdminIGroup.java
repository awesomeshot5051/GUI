package com.awesomeshot5051.separatedFiles.group;

public class AdminIGroup implements IGroup {
    @Override
    public String getGroupName() {
        return "Admin";
    }

    @Override
    public String getPermissions() {
        return "Can create users, manage content, and view logs.";
    }

    @Override
    public boolean canCreateUser() {
        return true; // Admin can create users
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof AdminIGroup;
    }
}
