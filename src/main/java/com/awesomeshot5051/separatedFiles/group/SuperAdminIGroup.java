package com.awesomeshot5051.separatedFiles.group;

public class SuperAdminIGroup implements IGroup {
    @Override
    public String getGroupName() {
        return "SuperAdmin";
    }

    @Override
    public String getPermissions() {
        return "Can create, manage users, and modify system configurations.";
    }

    @Override
    public boolean canCreateUser() {
        return true; // SuperAdmin can create users, as they have full permissions
    }


    @Override
    public boolean equals(Object obj) {
        return obj instanceof SuperAdminIGroup;
    }
}
