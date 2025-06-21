package com.awesomeshot5051.separatedFiles.userManagement;

import com.awesomeshot5051.*;
import com.awesomeshot5051.separatedFiles.*;
import com.awesomeshot5051.separatedFiles.Styler.*;
import com.awesomeshot5051.separatedFiles.group.*;
import com.awesomeshot5051.separatedFiles.session.*;
import com.awesomeshot5051.separatedFiles.systemConfiguration.database.*;
import com.awesomeshot5051.separatedFiles.systemConfiguration.passwordManagement.*;
import javafx.beans.property.*;
import javafx.collections.*;
import javafx.geometry.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.control.cell.*;
import javafx.scene.layout.*;
import javafx.stage.*;

import java.sql.*;
import java.util.*;

public class UserListView {
    private final Stage primaryStage;
    private final TableView<User> userTable = new TableView<>();
    private final Connection connection;
    private final String currentUser;

    public UserListView(Stage primaryStage) {
        this.primaryStage = primaryStage;
        this.connection = Main.getConnection();
        this.currentUser = SessionManager.getUsername();
        String currentPassword = new PasswordHasher().getPassword();
    }

    public void show() {
        BorderPane root = new BorderPane();

        // ——— Name Column ———
        TableColumn<User, String> nameColumn = new TableColumn<>("Name");
        nameColumn.setCellValueFactory(cd ->
                cd.getValue().getName().equalsIgnoreCase(SessionManager.getName())
                        ? cd.getValue().nameProperty()
                        : new SimpleStringProperty("***")
        );

        // ——— Username Column ———
        TableColumn<User, String> usernameColumn = new TableColumn<>("Username");
        usernameColumn.setCellValueFactory(cd -> cd.getValue().usernameProperty());

        // ——— Last Login Column ———
        TableColumn<User, String> lastLoginColumn = new TableColumn<>("Last Login");
        lastLoginColumn.setCellValueFactory(cd -> cd.getValue().lastLoginProperty());

        // ——— Group Column ———
        TableColumn<User, String> groupColumn = new TableColumn<>("Group");
        groupColumn.setCellValueFactory(cd -> cd.getValue().groupProperty());
        groupColumn.setCellFactory(ComboBoxTableCell.forTableColumn(
                "Default",
                "Standard", "Admin",
                SessionManager.getGroupType() instanceof SuperAdminIGroup ? "SuperAdmin" : null
        ));
        groupColumn.setOnEditCommit(evt -> {
            User u = evt.getRowValue();
            try {
                if (u.getGroup().getGroupName().equalsIgnoreCase("default")) {
                    showAlert("Error", "Cannot change the default group!");
                    u.setGroup(evt.getOldValue());
                    return;
                } else if (u.getName().equalsIgnoreCase(SessionManager.getName())) {
                    showAlert("Error", "Cannot change your own group!\nContact another admin.");
                    u.setGroup(evt.getOldValue());
                    return;
                } else {
                    u.setGroup(evt.getNewValue());
                    u.setModified(true);
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });

        // ——— Status Column ———
        TableColumn<User, String> statusColumn = new TableColumn<>("Status");
        statusColumn.setCellValueFactory(cd -> cd.getValue().statusProperty());
        statusColumn.setCellFactory(ComboBoxTableCell.forTableColumn("Enabled", "Disabled"));
        statusColumn.setOnEditCommit(evt -> {
            User u = evt.getRowValue();
            u.setStatus(evt.getNewValue());
            u.setModified(true);
        });

        // ——— Password Expiration Column ———
        TableColumn<User, String> expirationColumn = new TableColumn<>("Password Expiration (Days)");
        expirationColumn.setCellValueFactory(cd -> cd.getValue().passwordExpirationProperty());
        expirationColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        expirationColumn.setOnEditCommit(evt -> {
            User user = evt.getRowValue();
            String newVal = evt.getNewValue();
            if (newVal != null && newVal.matches("\\d+")) {
                user.setPasswordExpiration(newVal);
                user.setExpirationModified(true);
            } else {
                user.setPasswordExpiration(evt.getOldValue());
                userTable.refresh();
            }
        });

        // ——— Delete Action Column ———
        TableColumn<User, Void> deleteColumn = getUserVoidTableColumn();

        userTable.getColumns().setAll(List.of(
                nameColumn,
                usernameColumn,
                groupColumn,
                statusColumn,
                expirationColumn,
                lastLoginColumn,
                deleteColumn
        ));
        userTable.setEditable(true);
        userTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        userTable.getStyleClass().add("user-table");

        loadUsersFromDatabase();

        root.setCenter(userTable);

        // ——— Buttons ———
        Button updateButton = new Button("Update Database");
        Button exitButton = new Button("Exit");
        updateButton.getStyleClass().add("button");
        exitButton.getStyleClass().add("button");

        updateButton.setOnAction(e -> {
            for (User u : userTable.getItems()) {
                if (u.isModified()) {
                    new DatabaseInteraction().updateUser(u);
                    u.setModified(false);
                }
                if (u.isExpirationModified()) {
                    updateExpirationTime(u.getUsername(), u.getName(), u.getPasswordExpiration());
                    u.setExpirationModified(false);
                }
            }
        });

        exitButton.setOnAction(e -> exitUserManagement());

        HBox buttonPanel = new HBox(12, updateButton, exitButton);
        buttonPanel.setAlignment(Pos.CENTER);
        buttonPanel.setPadding(new Insets(10));
        root.setBottom(buttonPanel);

        Scene scene = new Scene(root, 900, 600);
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/styles/Styles.css")).toExternalForm());

        primaryStage.setScene(scene);
        primaryStage.setTitle("User List");
        primaryStage.show();
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        FXAlertStyler.style(alert);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void loadUsersFromDatabase() {
        ObservableList<User> list = FXCollections.observableArrayList();
        String sql = "SELECT name, username, `group`, status FROM users";
        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                if (!SessionManager.getGroupType().getGroupName().equalsIgnoreCase("superadmin") && rs.getString("group").equalsIgnoreCase("superadmin")) {
                    continue;
                }
                String name = rs.getString("name");
                String username = rs.getString("username");
                String group = rs.getString("group");
                String status = rs.getString("status");
                String expiration = new GetPasswordExpiration().getPasswordExpirationDays(username);
                String lastLogin = getLastLogin(name, username); // Add this line
                User u = new User(name, username, group, status);
                u.setPasswordExpiration(expiration);
                u.setExpirationModified(false);
                u.setLastLogin(lastLogin);
                list.add(u);
            }
            userTable.setItems(list);
        } catch (SQLException ex) {
            Main.getErrorLogger().handleException("Error loading user list", ex);
        }
    }

    private String getLastLogin(String name, String username) {
        try (CallableStatement cs = connection.prepareCall("{ call getLastLoginDate(?, ?) }")) {
            cs.setString(1, name);
            cs.setString(2, username);
            try (ResultSet rs = cs.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("last_login");
                }
            }
        } catch (SQLException ex) {
            Main.getErrorLogger().handleException("Error getting last login date", ex);
        }
        return "Unknown";
    }


    private void updateExpirationTime(String username, String name, String days) {
        try (CallableStatement cs = connection.prepareCall("{ call updateExpirationTime(?, ?, ?) }")) {
            cs.setString(1, username);
            cs.setString(2, name);
            cs.setInt(3, Integer.parseInt(days));
            cs.execute();
        } catch (SQLException ex) {
            Main.getErrorLogger().handleException("Error updating password expiration time", ex);
        }
    }

    private TableColumn<User, Void> getUserVoidTableColumn() {
        TableColumn<User, Void> col = new TableColumn<>("Actions");
        col.setCellFactory(tv -> new TableCell<>() {
            private final Button del = new Button("Delete");

            {
                del.setOnAction(e -> {
                    User u = getTableView().getItems().get(getIndex());
                    try {
                        new DatabaseInteraction().deleteUser(u, currentUser);
                        loadUsersFromDatabase();
                    } catch (Exception ex) {
                        // Show a popup if there is an SQL exception
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        FXAlertStyler.style(alert);
                        alert.setTitle("Database Error");
                        alert.setHeaderText("Operation Failed");
                        alert.setContentText(ex.getMessage()); // Shows "You cannot delete your own account"
                        alert.showAndWait();
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : del);
            }
        });
        return col;
    }

    private void exitUserManagement() {
        primaryStage.close();
        new MainScreen(
                SessionManager.getGroupType(),
                SessionManager.getStatus(),
                SessionManager.getUsername(),
                SessionManager.getName(),
                SessionManager.getConnection(),
                Main.getStage()
        );
    }
}