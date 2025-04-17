package com.awesomeshot5051.separatedFiles.dashboards;

import javafx.scene.*;

import java.awt.*;

public interface DashboardScreen {
    Parent getView();

    static Button getLogoutButton() {
        return null;
    }
}
