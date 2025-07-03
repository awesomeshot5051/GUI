package com.awesomeshot5051.Tables;

import javafx.scene.control.*;
import javafx.scene.control.skin.*;

import java.util.*;

public class CustomTableViewSkin<T> extends TableViewSkin<T> {
    private List<CustomTableColumnHeader> columnHeadersList = new ArrayList<>();

    private static class CustomTableColumnHeader extends TableColumnHeader {
        /**
         * Creates a new TableColumnHeader instance to visually represent the given
         * {@link TableColumnBase} instance.
         *
         * @param tc The table column to be visually represented by this instance.
         */
        public CustomTableColumnHeader(TableColumnBase tc) {
            super(tc);
        }

        public void resizeColumnToFitContent() {
            super.resizeColumnToFitContent(-1);
        }
    }

    public CustomTableViewSkin(TableView<T> tableView) {
        super(tableView);
    }

    @Override
    protected TableHeaderRow createTableHeaderRow() {
        return new TableHeaderRow(this) {
            @Override
            protected NestedTableColumnHeader createRootHeader() {
                return new NestedTableColumnHeader(null) {
                    @Override
                    protected TableColumnHeader createTableColumnHeader(TableColumnBase col) {
                        CustomTableColumnHeader columnHeader = new CustomTableColumnHeader(col);

                        if (columnHeadersList == null) {
                            columnHeadersList = new ArrayList<>();
                        }

                        columnHeadersList.add(columnHeader);

                        return columnHeader;
                    }
                };
            }
        };
    }

    public void resizeColumnToFit() {
        if (!columnHeadersList.isEmpty()) {
            for (CustomTableColumnHeader columnHeader : columnHeadersList) {
                columnHeader.resizeColumnToFitContent();
            }
        }
    }
}