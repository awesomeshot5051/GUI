package com.awesomeshot5051.separatedFiles.security.PasswordManagement;

import com.awesomeshot5051.*;
import com.awesomeshot5051.Tables.*;
import com.awesomeshot5051.separatedFiles.*;
import com.awesomeshot5051.separatedFiles.Styler.*;
import com.awesomeshot5051.separatedFiles.extraStuff.*;
import com.awesomeshot5051.separatedFiles.security.*;
import com.awesomeshot5051.separatedFiles.session.*;
import javafx.application.*;
import javafx.beans.property.*;
import javafx.collections.*;
import javafx.concurrent.*;
import javafx.geometry.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.control.cell.*;
import javafx.scene.layout.*;
import javafx.scene.text.*;
import javafx.stage.*;
import javafx.util.converter.*;

import java.sql.*;
import java.sql.Date;
import java.text.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.*;

public class PasswordManager {
    private final Connection connection;

    public PasswordManager() {
        this.connection = Main.getConnection();
    }

    private final CustomTableView<PasswordEntry> tableView = new CustomTableView<>();
    ;
    private final ObservableList<PasswordEntry> data = FXCollections.observableArrayList();

    public void showPasswordManager(Stage owner) {
        owner.setTitle("Password Manager");

        BorderPane root = new BorderPane();
        tableView.setEditable(true);
        tableView.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY); // allow scroll

        addEditableColumn("Website", PasswordEntry::websiteProperty, PasswordEntry::setWebsite, PasswordEntry::setWebsiteEdited);
        addEditableColumn("Username", PasswordEntry::usernameProperty, PasswordEntry::setUsername, PasswordEntry::setUsernameEdited);
        addEditableColumn("Password", PasswordEntry::passwordProperty, PasswordEntry::setPassword, PasswordEntry::setPasswordEdited);
        addEditableColumn("Label", PasswordEntry::labelProperty, PasswordEntry::setLabel, PasswordEntry::setLabelEdited);
        addEditableColumn("Notes", PasswordEntry::notesProperty, PasswordEntry::setNotes, PasswordEntry::setNotesEdited);
        // Add display column ONCE
        TableColumn<PasswordEntry, String> strengthValueCol = new TableColumn<>("Strength");
        strengthValueCol.setCellValueFactory(cell -> cell.getValue().strengthProperty());
        tableView.getColumns().add(strengthValueCol);

// Add test button column
        TableColumn<PasswordEntry, Void> strengthTestCol = new TableColumn<>("Test Strength");
        strengthTestCol.setCellFactory(col -> new TableCell<>() {
            private final Button testButton = new Button("Test");
            private final ProgressIndicator loadingIndicator = new ProgressIndicator();

            {
                loadingIndicator.setPrefSize(16, 16);
                loadingIndicator.setStyle("-fx-progress-color: green;");

                testButton.setOnAction(event -> {
                    PasswordEntry entry = getTableView().getItems().get(getIndex());
                    String password = entry.getPassword();

                    if (password == null || password.isEmpty()) {
                        Alert alert = new Alert(Alert.AlertType.WARNING, "Password is empty.");
                        FXAlertStyler.style(alert);
                        alert.showAndWait();
                        return;
                    }

                    setGraphic(loadingIndicator);

                    BruteForceCodeCracker.testPasswordStrength(
                            password,
                            score -> Platform.runLater(() -> {
                                entry.setStrength(String.valueOf(score));
                                entry.setStrengthEdited(true);
                                try {
                                    updateDatabase();
                                } catch (Exception e) {
                                    Main.getErrorLogger().silentlyHandle(e);
                                } finally {
                                    setGraphic(testButton);
                                }
                            }),
                            error -> Platform.runLater(() -> {
                                Main.getErrorLogger().handleException("Error testing password strength", error);
                                setGraphic(testButton);
                            })
                    );
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getIndex() >= getTableView().getItems().size()) {
                    setGraphic(null);
                } else {
                    setGraphic(testButton); // restore default state when row is created
                }
            }
        });
        tableView.getColumns().add(strengthTestCol);
        addEditableColumn("Last Accessed", PasswordEntry::lastAccessedProperty, PasswordEntry::setLastAccessed, PasswordEntry::setLastAccessedEdited);
        tableView.setItems(data);

        // Bottom buttons
        Button addButton = new Button("Add Entry");
        Button exitButton = new Button("Exit");

        addButton.setOnAction(e -> addEntry());
        exitButton.setOnAction(e -> new Thread(() -> {
            try {
                updateDatabase();
            } catch (Exception ex) {
                Main.getErrorLogger().silentlyHandle(ex);
            }

            // Navigate back to main screen on JavaFX thread
            Platform.runLater(() -> {
                new MainScreen(
                        SessionManager.getGroupType(),
                        SessionManager.getStatus(),
                        SessionManager.getUsername(),
                        SessionManager.getName(),
                        Main.getConnection(),
                        Main.getStage()
                );
            });
        }).start());
        TableColumn<PasswordEntry, Void> removeCol = new TableColumn<>("Remove");
        removeCol.setCellFactory(col -> new TableCell<>() {
            private final Button removeButton = new Button("Remove");

            {
                removeButton.setOnAction(event -> {
                    PasswordEntry entry = getTableView().getItems().get(getIndex());

                    Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                            "Are you sure you want to delete this entry?");
                    FXAlertStyler.style(confirm);
                    confirm.setTitle("Confirm Deletion");
                    confirm.setHeaderText("Delete Password Entry");

                    Optional<ButtonType> result = confirm.showAndWait();
                    if (result.isPresent() && result.get() == ButtonType.OK) {
                        try {
                            removePasswordEntry(entry); // 🔁 See Step 2
                            data.remove(entry); // Remove from table view
                        } catch (Exception ex) {
                            Main.getErrorLogger().handleException("Failed to delete entry", ex);
                        }
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : removeButton);
            }
        });

        tableView.getColumns().add(removeCol);

        HBox buttons = new HBox(10, addButton, exitButton);
        buttons.setPadding(new Insets(10));

        ScrollPane scrollPane = new ScrollPane(tableView);
        scrollPane.setFitToHeight(true);
        scrollPane.setFitToWidth(true);
        scrollPane.setPannable(true);

        VBox centerBox = new VBox(scrollPane);
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        root.setCenter(centerBox);
        root.setBottom(buttons);

        Scene scene = new Scene(root, 1100, 400);
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/styles/Styles.css")).toExternalForm());
        // Load entries asynchronously
        new Thread(() -> {
            try {
                loadPasswordEntries(owner);  // Pass owner stage to resize
            } catch (Exception e) {
                Main.getErrorLogger().handleException("Error loading password entries", e);
            }
            Platform.runLater(() -> {
                owner.setScene(scene);
                owner.sizeToScene();  // Optional: initial sizing before load
            });
        }).start();


    }

    private void removePasswordEntry(PasswordEntry entry) throws SQLException {
        try (CallableStatement stmt = connection.prepareCall("{CALL deletePasswordEntry(?, ?, ?)}")) {
            stmt.setString(1, SessionManager.getUsername());
            stmt.setString(2, SessionManager.getName());
            stmt.setInt(3, entry.getId());
            stmt.execute();
        }
    }

    private void addEditableColumn(String title,
                                   Function<PasswordEntry, StringProperty> prop,
                                   BiConsumer<PasswordEntry, String> setter,
                                   BiConsumer<PasswordEntry, Boolean> editedFlagSetter) {
        TableColumn<PasswordEntry, String> col = new TableColumn<>(title);
        col.setCellValueFactory(cell -> prop.apply(cell.getValue()));
        col.setCellFactory(TextFieldTableCell.forTableColumn(new DefaultStringConverter()));
        col.setOnEditCommit(event -> {
            PasswordEntry entry = event.getRowValue();
            setter.accept(entry, event.getNewValue());
            editedFlagSetter.accept(entry, true);
        });
        tableView.getColumns().add(col);
    }


    // Placeholder — you'll implement this
    private void loadPasswordEntries(Stage owner) throws Exception {
        List<CompletableFuture<Void>> futures = new ArrayList<>();

        Platform.runLater(() -> {
            data.clear();
            tableView.setPlaceholder(new VBox(
                    new ProgressIndicator(),
                    new Label("Loading passwords...")
            ));

        });

        try (CallableStatement stmt = connection.prepareCall("CALL retrievePassword(?, ?)")) {
            FileEncryption encryptor = new FileEncryption();
            stmt.setString(1, SessionManager.getUsername());
            stmt.setString(2, SessionManager.getName());

            boolean hasResult = stmt.execute();
            if (hasResult) {
                try (ResultSet rs = stmt.getResultSet()) {
                    while (rs.next()) {
                        int id = rs.getInt("id");
                        byte[] websiteBytes = rs.getBytes("website");
                        byte[] usernameBytes = rs.getBytes("username");
                        byte[] passwordBytes = rs.getBytes("password");
                        byte[] labelBytes = rs.getBytes("password_label");
                        byte[] notesBytes = rs.getBytes("notes");
                        int strth = rs.getInt("password_strength");
                        Date sqlDate = rs.getDate("last_accessed");

                        CompletableFuture<Void> future = CompletableFuture.supplyAsync(() -> {
                            try {
                                String website = websiteBytes != null ? encryptor.decryptText(websiteBytes) : "";
                                String username = usernameBytes != null ? encryptor.decryptText(usernameBytes) : "";
                                String password = passwordBytes != null ? encryptor.decryptText(passwordBytes) : "";
                                String label = labelBytes != null ? encryptor.decryptText(labelBytes) : "";
                                String notes = notesBytes != null ? encryptor.decryptText(notesBytes) : "";
                                String strength = strth == 0 ? "" : Integer.toString(strth);
                                String lastAccessed = sqlDate != null ? sqlDate.toString() : "";

                                PasswordEntry entry = new PasswordEntry(id, website, username, password, label, notes, strength, lastAccessed);
                                Platform.runLater(() -> data.add(entry));
                            } catch (Exception e) {
                                Main.getErrorLogger().handleException("Decryption failed", e);
                            }
                            return null;
                        });
                        futures.add(future);
                    }
                }
            }
        }

        CompletableFuture
                .allOf(futures.toArray(new CompletableFuture[0]))
                .thenRun(() -> {
                    if (owner != null) {
                        Platform.runLater(() -> {
                            if (data.isEmpty()) {
                                tableView.setPlaceholder(new Label("No password entries found."));
                            } else {
                                tableView.setPlaceholder(null);
                            }
                            owner.sizeToScene();
                            tableView.resizeColumnsToFitContent();
                        });
                    }
                });
    }


    public static void autoResizeColumns(TableView<?> table) {
        //Set the right policy
        table.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);
        table.getColumns().forEach((column) ->
        {
            //Minimal width = columnheader
            Text t = new Text(column.getText());
            double max = t.getLayoutBounds().getWidth();
            for (int i = 0; i < table.getItems().size(); i++) {
                //cell must not be empty
                if (column.getCellData(i) != null) {
                    t = new Text(column.getCellData(i).toString());
                    double calcwidth = t.getLayoutBounds().getWidth();
                    //remember new max-width
                    if (calcwidth > max) {
                        max = calcwidth;
                    }
                }
            }
            //set the new max-widht with some extra space
            column.setPrefWidth(max + 20.0d);
        });
    }

    // Placeholder — you'll encrypt & insert/update entries here
    private void updateDatabase() throws Exception {
        FileEncryption encryptor = new FileEncryption();

        for (PasswordEntry entry : data) {
            if (entry.isNewEntry()) {
                insertPassword(entry); // Inserts handle encryption internally
                continue;
            }

            // Skip unedited rows entirely
            if (!(entry.isWebsiteEdited() || entry.isUsernameEdited() || entry.isPasswordEdited() ||
                    entry.isLabelEdited() || entry.isNotesEdited() || entry.isStrengthEdited() ||
                    entry.isLastAccessedEdited())) {
                continue;
            }

            CallableStatement stmt = connection.prepareCall("{CALL updatePasswordEntry(?, ?, ?, ?, ?, ?, ?, ?, ?, ?)}");
            stmt.setInt(1, entry.getId());                   // used in WHERE clause
            stmt.setString(2, SessionManager.getUsername()); // plaintext
            stmt.setString(3, SessionManager.getName());     // plaintext


            stmt.setBytes(4, entry.isWebsiteEdited() ? encryptor.encryptText(entry.getWebsite()) : null);
            stmt.setBytes(5, entry.isUsernameEdited() ? encryptor.encryptText(entry.getUsername()) : null);
            stmt.setBytes(6, entry.isPasswordEdited() ? encryptor.encryptText(entry.getPassword()) : null);
            stmt.setBytes(7, entry.isLabelEdited() ? encryptor.encryptText(entry.getLabel()) : null);
            stmt.setBytes(8, entry.isNotesEdited() ? encryptor.encryptText(entry.getNotes()) : null);
            stmt.setInt(9, entry.isStrengthEdited() ? Integer.parseInt(entry.getStrength()) : 0);

            Date lastAccessed = null;
            try {
                String rawDate = entry.getLastAccessed();
                if (rawDate != null && rawDate.matches("\\d{4}-\\d{2}-\\d{2}")) {
                    lastAccessed = Date.valueOf(rawDate);
                }
            } catch (IllegalArgumentException ignored) {
            }

            stmt.setDate(10, entry.isLastAccessedEdited() ? lastAccessed : null);

            stmt.execute();

            // Reset all edit flags
            entry.setWebsiteEdited(false);
            entry.setUsernameEdited(false);
            entry.setPasswordEdited(false);
            entry.setLabelEdited(false);
            entry.setNotesEdited(false);
            entry.setStrengthEdited(false);
            entry.setLastAccessedEdited(false);
        }
    }


    public void insertPassword(PasswordEntry entry) throws Exception {
        FileEncryption encryptor = new FileEncryption();

        PreparedStatement statement = connection.prepareCall("CALL insertPassword(?,?,?,?,?,?,?,?,?)");

        // Unencrypted for user identification
        statement.setString(1, SessionManager.getUsername());
        statement.setString(2, SessionManager.getName());

        // Encrypted fields
        statement.setBytes(3, !entry.getUsername().isEmpty()
                ? encryptor.encryptText(entry.getUsername())
                : null);
        statement.setBytes(4, !entry.getPassword().isEmpty()
                ? encryptor.encryptText(entry.getPassword())
                : null);

        // Fix: param 5 is website (encrypted)
        statement.setBytes(5, !entry.getWebsite().isEmpty()
                ? encryptor.encryptText(entry.getWebsite())
                : null);

        // param 6 is notes (encrypted)
        statement.setBytes(6, !entry.getNotes().isEmpty()
                ? encryptor.encryptText(entry.getNotes())
                : null);

        // param 7 is label (encrypted)
        statement.setBytes(7, !entry.getLabel().isEmpty()
                ? encryptor.encryptText(entry.getLabel())
                : null);

        // password_strength as integer — convert if necessary
        int strength;
        try {
            strength = Integer.parseInt(entry.getStrength());
        } catch (NumberFormatException e) {
            strength = 0; // Or some default
        }
        statement.setInt(8, strength);

        // Convert date string (e.g., "2025-06-30") to java.sql.Date
        Date lastAccessed;
        try {
            String rawDate = entry.getLastAccessed();
            if (rawDate != null && rawDate.matches("\\d{4}-\\d{2}-\\d{2}")) {
                lastAccessed = Date.valueOf(rawDate);
            } else {
                lastAccessed = new Date(System.currentTimeMillis()); // fallback
            }
        } catch (IllegalArgumentException ex) {
            lastAccessed = new Date(System.currentTimeMillis()); // fallback
        }

        statement.setDate(9, lastAccessed);

        statement.execute();
    }

    private void addEntry() {
        Stage stage = new Stage();
        stage.setTitle("Add Password Entry");

        VBox root = new VBox(12);
        root.getStyleClass().add("form-container");
        root.setPadding(new Insets(20));

        Label websiteLabel = new Label("Website:");
        websiteLabel.getStyleClass().add("label");
        TextField websiteField = new TextField();
        websiteField.setPromptText("Enter website URL");

        Label usernameLabel = new Label("Username:");
        usernameLabel.getStyleClass().add("label");
        TextField usernameField = new TextField();
        usernameField.setPromptText("Enter username");

        Label passwordLabel = new Label("Password:");
        passwordLabel.getStyleClass().add("label");
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Enter password");

        Label labelLabel = new Label("Label:");
        labelLabel.getStyleClass().add("label");
        TextField labelField = new TextField();
        labelField.setPromptText("Optional label");
        Label noteLabel = new Label("Notes:");
        noteLabel.getStyleClass().add("label");
        TextField noteField = new TextField();
        noteField.setPromptText("Optional notes");
        Label strengthLabel = new Label("Strength:");
        strengthLabel.getStyleClass().add("label");
        TextField strengthField = new TextField();
        strengthField.setPromptText("Optional strength");
        HBox buttons = new HBox(10);
        buttons.setAlignment(Pos.CENTER_RIGHT);

        Button saveBtn = new Button("Save");
        saveBtn.getStyleClass().add("button");

        Button cancelBtn = new Button("Cancel");
        cancelBtn.getStyleClass().addAll("button", "button-cancel");

        buttons.getChildren().addAll(cancelBtn, saveBtn);
        root.getChildren().addAll(
                websiteLabel, websiteField,
                usernameLabel, usernameField,
                passwordLabel, passwordField,
                labelLabel, labelField,
                noteLabel, noteField,
                strengthLabel, strengthField,
                buttons
        );
        // Wrap the root VBox in a ScrollPane
        ScrollPane scrollPane = new ScrollPane(root);
        scrollPane.setFitToWidth(true);       // Make scroll content width match viewport width
        scrollPane.setFitToHeight(true);      // Optional, but usually set
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER); // Disable horizontal scroll
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        // Create scene with ScrollPane as root
        Scene scene = new Scene(scrollPane, 400, 400);
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/styles/Styles.css")).toExternalForm());
        stage.setScene(scene);
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setResizable(false);

        cancelBtn.setOnAction(e -> stage.close());

        saveBtn.setOnAction(e -> {
            // Your validation and insert code, including using getTask(entry, stage)
            if (websiteField.getText().isEmpty() || usernameField.getText().isEmpty() || passwordField.getText().isEmpty()) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Validation Error");
                alert.setHeaderText(null);
                alert.setContentText("Website, Username, and Password are required.");
                alert.showAndWait();
                return;
            }

            PasswordEntry entry = new PasswordEntry(
                    0,
                    websiteField.getText(),
                    usernameField.getText(),
                    passwordField.getText(),
                    labelField.getText(),
                    noteField.getText(),
                    strengthField.getText(),
                    new SimpleDateFormat("yyyy-MM-dd").format(System.currentTimeMillis())
            );

            Task<Void> insertTask = getTask(entry, stage);
            new Thread(insertTask).start();
        });

        stage.showAndWait();
    }

    private Task<Void> getTask(PasswordEntry entry, Stage stage) {
        Task<Void> insertTask = new Task<>() {
            @Override
            protected Void call() throws Exception {
                insertPassword(entry);
                return null;
            }
        };

        insertTask.setOnSucceeded(ev -> {
            stage.close();
            try {
                PasswordManager.this.loadPasswordEntries(null);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        insertTask.setOnFailed(ev -> {
            Throwable ex = insertTask.getException();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Insert Failed");
            alert.setHeaderText(null);
            alert.setContentText("Failed to save password entry:\n" + ex.getMessage());
            FXAlertStyler.style(alert);
            alert.showAndWait();
        });
        return insertTask;
    }

}
