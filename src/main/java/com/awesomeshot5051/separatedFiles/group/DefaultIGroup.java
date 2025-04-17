package com.awesomeshot5051.separatedFiles.group;

public class DefaultIGroup implements IGroup {
    @Override
    public String getGroupName() {
        return "Default";
    }

    @Override
    public String getPermissions() {
        return "Can only create 1 user.";
    }

    @Override
    public boolean canCreateUser() {
        return true; // Default can create 1 user
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof DefaultIGroup;
    }
}
