package com.awesomeshot5051.separatedFiles.group;

public class StandardIGroup implements IGroup {
    @Override
    public String getGroupName() {
        return "Standard";
    }

    @Override
    public String getPermissions() {
        return "Can create multiple users and view content.";
    }

    @Override
    public boolean canCreateUser() {
        return true; // Standard can create users
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof StandardIGroup;
    }
}
