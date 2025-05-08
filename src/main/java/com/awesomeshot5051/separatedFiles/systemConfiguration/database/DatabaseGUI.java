package com.awesomeshot5051.separatedFiles.systemConfiguration.database;

import com.awesomeshot5051.*;
import com.awesomeshot5051.separatedFiles.*;
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

public class DatabaseGUI {
    private static String databaseName;
    private static Stage primaryStage;
    private static Scene mainScene;

    public static void showDatabaseGui() {
        DatabaseGUI.primaryStage = Main.getStage();
        try {
            // Read the password from the password file
            InputStream in = DatabaseGUI.class.getResourceAsStream("/guest_password.txt");
            if (in == null) {
                throw new FileNotFoundException("password.txt not found in resources.");
            }
            String serverPassword = new String(in.readAllBytes(), StandardCharsets.UTF_8);


            // Use existing connection method
            new Main().connectToDatabase(serverPassword);

            chooseDatabase();
        } catch (IOException e) {
            e.printStackTrace();
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
            e.printStackTrace();
            // Handle any SQL errors here
        }
    }

    public static void chooseDatabase() {
        try {
            // Query the server for available databases
            Statement statement = Main.getConnection().createStatement();
            ResultSet resultSet = statement.executeQuery("SHOW DATABASES");

            // Create a layout to display the database buttons
            FlowPane buttonPane = new FlowPane(10, 10);
            buttonPane.setPadding(new Insets(10));
            buttonPane.setAlignment(Pos.CENTER);

            // Iterate through the result set and create a button for each database
            while (resultSet.next()) {
                String dbName = resultSet.getString(1);
                Button dbButton = getDbButton(dbName);
                buttonPane.getChildren().add(dbButton);
            }

            Button exitButton = new Button("Exit");
            exitButton.setPrefWidth(120);
            exitButton.setOnAction(e -> {
                // Create new MainScreen using SessionManager
                new MainScreen(SessionManager.getGroupType(), SessionManager.getStatus(), SessionManager.getUsername(), SessionManager.getName(), SessionManager.getConnection(), Main.getStage());
            });
            buttonPane.getChildren().add(exitButton);

            // Set the scene and show the stage
            Scene scene = new Scene(buttonPane, 400, 200);
            primaryStage.setTitle("Choose Database");
            primaryStage.setScene(scene);
            primaryStage.show();

            primaryStage.setOnCloseRequest((WindowEvent event) -> {
                new MainScreen(SessionManager.getGroupType(), SessionManager.getStatus(), SessionManager.getUsername(), SessionManager.getName(), SessionManager.getConnection(), Main.getStage());
            });

        } catch (SQLException e) {
            e.printStackTrace();
            // Handle any SQL errors here
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

        // Create text field for SQL commands
        TextField commandField = new TextField();
        commandField.setPromptText("Enter SQL command");

        // Create buttons
        Button executeButton = new Button("Execute Command");
        Button exitButton = new Button("Exit");

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
                        // Position cursor at the end
                        Platform.runLater(() -> commandField.positionCaret(prevCommand.length()));
                    }
                    break;
                case DOWN:
                    String nextCommand = historyManager.getNextCommand();
                    commandField.setText(nextCommand != null ? nextCommand : "");
                    if (nextCommand != null) {
                        // Position cursor at the end
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

        // Create and set scene
        Scene scene = new Scene(root, 500, 200);
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
                alert.setTitle("Command Result");
                alert.setHeaderText(null);
                alert.setContentText("Command executed successfully. Rows affected: " + updateCount);
                alert.showAndWait();
            }
        } catch (SQLException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
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

        // Get metadata
        ResultSetMetaData metaData = rs.getMetaData();
        int columnCount = metaData.getColumnCount();

        // Create table columns
        for (int i = 0; i < columnCount; i++) {
            final int j = i;
            TableColumn<ObservableList<Object>, Object> column = new TableColumn<>(metaData.getColumnName(j + 1));
            column.setCellValueFactory(param ->
                    new SimpleObjectProperty<>(param.getValue().get(j)));
            tableView.getColumns().add(column);
        }

        // Add data to table
        ObservableList<ObservableList<Object>> data = FXCollections.observableArrayList();
        while (rs.next()) {
            ObservableList<Object> row = FXCollections.observableArrayList();
            for (int i = 1; i <= columnCount; i++) {
                // Mask sensitive data
                if (metaData.getColumnName(i).equalsIgnoreCase("password") ||
                        metaData.getColumnName(i).equalsIgnoreCase("salt")) {
                    row.add("***");
                } else {
                    row.add(rs.getObject(i));
                }
            }
            data.add(row);
        }
        tableView.setItems(data);

        // Create a new window to display the table
        Stage resultStage = new Stage();
        VBox vbox = new VBox(tableView);
        vbox.setPadding(new Insets(10));
        Scene scene = new Scene(vbox, 600, 400);
        resultStage.setScene(scene);
        resultStage.setTitle("Query Results");
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