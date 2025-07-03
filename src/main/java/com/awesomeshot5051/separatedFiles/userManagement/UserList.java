package com.awesomeshot5051.separatedFiles.userManagement;

import java.util.ArrayList;

public class UserList extends ArrayList<User> {

    public UserList() {
        super();
    }

    public User findByUsername(String username) {
        for (User user : this) {
            if (user.getUsername().equalsIgnoreCase(username)) {
                return user;
            }
        }
        return null;
    }

    public boolean containsUsername(String username) {
        return findByUsername(username) != null;
    }

    public void removeByUsername(String username) {
        this.removeIf(user -> user.getUsername().equalsIgnoreCase(username));
    }

    public UserList filterByGroup(String groupName) {
        UserList filtered = new UserList();
        for (User user : this) {
            if (user.getGroup().getGroupName().equalsIgnoreCase(groupName)) {
                filtered.add(user);
            }
        }
        return filtered;
    }
}
