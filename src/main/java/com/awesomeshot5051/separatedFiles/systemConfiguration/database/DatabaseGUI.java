package com.awesomeshot5051.separatedFiles.systemConfiguration.database;

import com.awesomeshot5051.*;
import com.awesomeshot5051.separatedFiles.*;
import com.awesomeshot5051.separatedFiles.Styler.*;
import com.awesomeshot5051.separatedFiles.session.*;
import javafx.application.*;
import javafx.beans.property.*;
import javafx.collections.*;
import javafx.geometry.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.*;

import java.io.*;
import java.nio.charset.*;
import java.sql.*;
import java.util.*;

import static com.awesomeshot5051.Launcher.*;

public class DatabaseGUI {
    private static String databaseName;
    private static Stage primaryStage;
    private static Scene mainScene;

    public void showDatabaseGui() {
        DatabaseGUI.primaryStage = Main.getStage();
        try {
            // Read the password from the password file
            InputStream in = DatabaseGUI.class.getResourceAsStream("/guestPassword.txt");
            if (in == null) {
                throw new FileNotFoundException("password.txt not found in resources.");
            }
            serverPassword = new String(in.readAllBytes(), StandardCharsets.UTF_8);


            // Use an existing connection method
            new Main().connectToDatabase();

            chooseDatabase();
        } catch (IOException e) {
            Main.getErrorLogger().handleException("Error reading guest password", e);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public DatabaseGUI() {
        showDatabaseGui();
    }

    private static void useDatabase(String dbName) {
        String useCommand = "USE " + dbName;
        try (Statement statement = Main.getConnection().createStatement()) {
            statement.execute(useCommand);
            Main.getLogger().info("Database changed to: " + dbName);
        } catch (SQLException e) {
            Main.getErrorLogger().handleException("Error changing database", e);
            // Handle any SQL errors here
        }
    }

    public static void chooseDatabase() {
        try {
            // Query the server for available databases
            Statement statement = Main.getConnection().createStatement();
            ResultSet resultSet = statement.executeQuery("SHOW DATABASES");

            // Create a GridPane for 2 columns
            GridPane grid = new GridPane();
            grid.setPadding(new Insets(20));
            grid.setHgap(15);  // Horizontal spacing between buttons
            grid.setVgap(15);  // Vertical spacing between buttons
            grid.setAlignment(Pos.CENTER);
            grid.getStyleClass().add("vbox");  // reuse your container styling

            int colCount = 2;  // Number of columns
            int col = 0;
            int row = 0;

            // Iterate through the result set and create a button for each database
            while (resultSet.next()) {
                String dbName = resultSet.getString(1);
                Button dbButton = getDbButton(dbName);
                dbButton.getStyleClass().add("button");  // Your button style class
                dbButton.setPrefWidth(150);
                grid.add(dbButton, col, row);

                col++;
                if (col >= colCount) {
                    col = 0;
                    row++;
                }
            }

            // Exit button setup
            Button exitButton = new Button("Exit");
            exitButton.setPrefWidth(150);
            exitButton.getStyleClass().addAll("button", "button-cancel");
            exitButton.setOnAction(e -> {
                new MainScreen(SessionManager.getGroupType(), SessionManager.getStatus(), SessionManager.getUsername(), SessionManager.getName(), SessionManager.getConnection(), Main.getStage());
            });

            // Add exit button wrapped in centered HBox
            row++;  // next row after last db button
            HBox exitButtonWrapper = new HBox(exitButton);
            exitButtonWrapper.setAlignment(Pos.CENTER);
            grid.add(exitButtonWrapper, 0, row, 2, 1);  // span 2 columns but button centered inside


            // Set the scene and add your stylesheet
            Scene scene = new Scene(grid, 400, 300);
            scene.getStylesheets().add(Objects.requireNonNull(Main.class.getResource("/styles/Styles.css")).toExternalForm());

            primaryStage.setTitle("Choose Database");
            primaryStage.setScene(scene);
            primaryStage.show();

            primaryStage.setOnCloseRequest((WindowEvent event) -> {
                new MainScreen(SessionManager.getGroupType(), SessionManager.getStatus(), SessionManager.getUsername(), SessionManager.getName(), SessionManager.getConnection(), Main.getStage());
            });

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    private static Button getDbButton(String dbName) {
        Button dbButton = new Button(dbName);
        dbButton.setPrefWidth(120);
        dbButton.setOnAction(e -> {
            databaseName = dbName; // Set the global variable to the selected database name
            useDatabase(dbName);
            // Show database interaction screen
            try {
                databaseInteraction();
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
        });
        return dbButton;
    }


    private static void databaseInteraction() throws SQLException {
        // Create root layout
        BorderPane root = new BorderPane();
        root.getStyleClass().add("form-container"); // apply dark background and shadow

        // Create text field for SQL commands
        TextField commandField = new TextField();
        commandField.setPromptText("Enter SQL command");
        commandField.getStyleClass().add("text-field");

        // Create buttons
        Button executeButton = new Button("Execute Command");
        executeButton.getStyleClass().add("button");

        Button exitButton = new Button("Exit");
        exitButton.getStyleClass().add("button");

        HBox buttonBox = new HBox(10, executeButton, exitButton);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.setPadding(new Insets(10));

        // Command history manager
        CommandHistoryManager historyManager = new CommandHistoryManager();

        // Handle command execution
        executeButton.setOnAction(e -> {
            String command = commandField.getText();
            executeCommand(command, historyManager);
            commandField.clear();
        });

        // Handle command execution with Enter key
        commandField.setOnAction(e -> {
            String command = commandField.getText();
            if (getCommandType(command).equalsIgnoreCase("use")) {
                String[] words = command.split("\\s+");
                databaseName = words[1].replace(";", "");
                primaryStage.setTitle("Command Executor for Database " + databaseName);
            }
            executeCommand(command, historyManager);
            commandField.clear();
        });

        // Handle up/down keys for command history
        commandField.setOnKeyPressed(event -> {
            switch (event.getCode()) {
                case UP:
                    String prevCommand = historyManager.getPreviousCommand();
                    if (prevCommand != null) {
                        commandField.setText(prevCommand);
                        Platform.runLater(() -> commandField.positionCaret(prevCommand.length()));
                    }
                    break;
                case DOWN:
                    String nextCommand = historyManager.getNextCommand();
                    commandField.setText(nextCommand != null ? nextCommand : "");
                    if (nextCommand != null) {
                        Platform.runLater(() -> commandField.positionCaret(nextCommand.length()));
                    }
                    break;
                default:
                    break;
            }
        });

        // Handle exit button
        exitButton.setOnAction(e -> chooseDatabase());

        // Add components to the layout
        root.setTop(commandField);
        root.setBottom(buttonBox);

        // Create and set scene with Styles.css applied
        Scene scene = new Scene(root, 400, 300);
        scene.getStylesheets().add(Objects.requireNonNull(
                Main.class.getResource("/styles/Styles.css")).toExternalForm()
        );
        primaryStage.setMaximized(true);
        primaryStage.setTitle("Command Executor for Database " + databaseName);
        primaryStage.setScene(scene);
    }


    private static void executeCommand(String command, CommandHistoryManager historyManager) {
        historyManager.addCommand(command);
        String commandType = getCommandType(command);
        System.out.println("Executing " + commandType + " command now");
        execute(command, commandType);
    }

    private static String getCommandType(String command) {
        if (beginsWith(command, "use")) {
            String[] words = command.split("\\s+");
            databaseName = words[1]; // Compare the first word with the keyword
            return "use";
        } else if (beginsWith(command, "update")) {
            return "update";
        } else if (beginsWith(command, "insert")) {
            return "insert";
        } else if (beginsWith(command, "drop")) {
            return "drop";
        } else if (beginsWith(command, "select")) {
            return "select";
        } else if (beginsWith(command, "show")) {
            return "show";
        } else {
            return "unknown";
        }
    }

    private static boolean beginsWith(String command, String keyword) {
        String[] words = command.split("\\s+"); // Split the command by spaces
        return words.length > 0 && words[0].equalsIgnoreCase(keyword); // Compare the first word with the keyword
    }

    private static void execute(String command, String commandType) {
        try (Statement statement = Main.getConnection().createStatement()) {
            boolean isResultSet = statement.execute(command);
            if (isResultSet) {
                ResultSet resultSet = statement.getResultSet();
                // Show results in a table
                showResultsInTable(resultSet);
            } else {
                int updateCount = statement.getUpdateCount();
                System.out.println("Update count: " + updateCount);
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                FXAlertStyler.style(alert);
                alert.setTitle("Command Result");
                alert.setHeaderText(null);
                alert.setContentText("Command executed successfully. Rows affected: " + updateCount);
                alert.showAndWait();
            }
        } catch (SQLException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            FXAlertStyler.style(alert);
            alert.setTitle("SQL Error");
            alert.setHeaderText("Error executing command");
            alert.setContentText("Error: " + e.getMessage());
            alert.showAndWait();
        }
    }

    private static void showResultsInTable(ResultSet rs) throws SQLException {
        // Create a TableView
        TableView<ObservableList<Object>> tableView = new TableView<>();
        tableView.setEditable(false);
        tableView.getStyleClass().add("table-view"); // Apply dark styling

        // Get metadata
        ResultSetMetaData metaData = rs.getMetaData();
        int columnCount = metaData.getColumnCount();

        // Create table columns
        for (int i = 0; i < columnCount; i++) {
            final int j = i;
            TableColumn<ObservableList<Object>, Object> column = new TableColumn<>(metaData.getColumnName(j + 1));
            column.setCellValueFactory(param -> new SimpleObjectProperty<>(param.getValue().get(j)));
            column.setPrefWidth(150);
            tableView.getColumns().add(column);
        }

        // Add data to table
        ObservableList<ObservableList<Object>> data = FXCollections.observableArrayList();
        while (rs.next()) {
            ObservableList<Object> row = FXCollections.observableArrayList();
            for (int i = 1; i <= columnCount; i++) {
                String colName = metaData.getColumnName(i).toLowerCase();
                if (colName.contains("password") || colName.contains("salt") || colName.contains("public_key")) {
                    row.add("***");
                } else {
                    row.add(rs.getObject(i));
                }
            }
            data.add(row);
        }
        tableView.setItems(data);

        // Container setup
        VBox vbox = new VBox(tableView);
        vbox.setPadding(new Insets(10));
        vbox.getStyleClass().add("form-container");

        // Scene setup with dark theme
        Scene scene = new Scene(vbox);
        scene.getStylesheets().add(Objects.requireNonNull(
                Main.class.getResource("/styles/Styles.css")).toExternalForm()
        );

        // Stage setup
        Stage resultStage = new Stage();
        resultStage.setMaximized(true);
        resultStage.setTitle("Query Results");
        resultStage.setScene(scene);
        resultStage.show();
    }


    // Command history manager
    private static class CommandHistoryManager {
        private List<String> commandHistory = new ArrayList<>();
        private int currentCommandIndex = -1;

        public void addCommand(String command) {
            commandHistory.add(command);
            currentCommandIndex = commandHistory.size(); // Reset the index to the end
        }

        public String getPreviousCommand() {
            if (currentCommandIndex > 0) {
                currentCommandIndex--;
                return commandHistory.get(currentCommandIndex);
            }
            return null; // No previous command
        }

        public String getNextCommand() {
            if (currentCommandIndex < commandHistory.size() - 1) {
                currentCommandIndex++;
                return commandHistory.get(currentCommandIndex);
            }
            return null; // No next command
        }
    }
}