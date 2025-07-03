package com.awesomeshot5051.Tables;

import javafx.scene.control.*;

public class CustomTableView<T> extends TableView<T> {
    private final CustomTableViewSkin<T> thisSkin;

    public CustomTableView() {
        super();
        setSkin(thisSkin = new CustomTableViewSkin<T>(this));
    }

    public void resizeColumnsToFitContent() {
        if (thisSkin != null && getSkin() == thisSkin) {
            thisSkin.resizeColumnToFit();
        }
    }
}
