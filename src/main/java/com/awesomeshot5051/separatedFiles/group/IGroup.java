package com.awesomeshot5051.separatedFiles.group;

public interface IGroup {
    // Method to get the group name
    String getGroupName();

    // Method to get permissions associated with the group
    String getPermissions();

    // Method to check if the user can create new users
    boolean canCreateUser();
}
