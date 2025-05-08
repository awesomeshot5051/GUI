package com.awesomeshot5051.separatedFiles.security;

import com.awesomeshot5051.*;
import com.awesomeshot5051.separatedFiles.*;
import com.awesomeshot5051.separatedFiles.session.*;
import javafx.geometry.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.*;

import java.io.*;
import java.nio.file.*;
import java.sql.*;
import java.util.*;

public class VaultManagementScreen {
    private final Stage stage;

    public VaultManagementScreen() {
        this.stage = Main.getStage();
    }

    // GUI for Vault Management
    public void VaultManagementMainGUI() {
        stage.setTitle("Vault Management");

        // Button for Vault operations
        Button encryptButton = getEncryptButton();


        Button decryptButton = getDecryptButton();


        Button viewButton = new Button("View");
        viewButton.setOnAction(e -> {
            try {
                new FileEncryption().viewVaultFiles();
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        });
        Button editButton = new Button("Edit");
        editButton.setOnAction(e -> {
        });


        // Exit Button
        Button exitButton = new Button("Exit");
        exitButton.setOnAction(e -> {
//            stage.close();
            new MainScreen(SessionManager.getGroupType(), SessionManager.getStatus(), SessionManager.getUsername(), SessionManager.getName(), SessionManager.getConnection(), Main.getStage());
        });

        VBox layout = getVBox(encryptButton, decryptButton, viewButton, exitButton);

        stage.setScene(new Scene(layout, 300, 250));
        stage.show();
    }

    private Button getDecryptButton() {
        Button decryptButton = new Button("Decrypt");
        decryptButton.setOnAction(e -> {
            // Use our custom Windows-like file chooser that shows decrypted names
            String selectedEncryptedName = showDecryptFileChooser();

            if (selectedEncryptedName != null) {
                try {
                    // Get the original filename for the selected encrypted file
                    String originalFileName = new FileEncryption().getOriginalFileName(selectedEncryptedName);

                    // Now choose where to save the decrypted file
                    FileChooser targetFileChooser = new FileChooser();
                    targetFileChooser.setTitle("Save Decrypted File As");

                    // Use the original filename as the suggested name
                    targetFileChooser.setInitialFileName(originalFileName);

                    // Set some common file filters if needed
                    targetFileChooser.getExtensionFilters().addAll(
                            new FileChooser.ExtensionFilter("All Files", "*.*")
                    );

                    File targetFile = targetFileChooser.showSaveDialog(stage);

                    if (targetFile != null) {
                        try {
                            // Check connection and reconnect if needed
                            Connection connection = Main.getConnection();
                            if (connection == null || connection.isClosed()) {
                                Main.getInstance().connectToDatabase();
                            }

                            // Call the decryptFile method with source filename and target path
                            boolean success = new FileEncryption().decryptFile(
                                    selectedEncryptedName,  // First parameter: vault file name
                                    targetFile.toPath()     // Second parameter: target file path
                            );

                            if (success) {
                                // Show a success message
                                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                                alert.setTitle("Decryption Complete");
                                alert.setHeaderText(null);
                                alert.setContentText("File decrypted successfully!");
                                alert.showAndWait();
                            }
                        } catch (Exception ex) {
                            // Show an error message
                            Alert alert = new Alert(Alert.AlertType.ERROR);
                            alert.setTitle("Decryption Failed");
                            alert.setHeaderText(null);
                            alert.setContentText("Failed to decrypt file: " + ex.getMessage());
                            ex.printStackTrace();
                            alert.showAndWait();
                        }
                    }
                } catch (Exception ex) {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Error");
                    alert.setHeaderText(null);
                    alert.setContentText("An error occurred: " + ex.getMessage());
                    ex.printStackTrace();
                    alert.showAndWait();
                }
            }
        });
        return decryptButton;
    }

    private Button getEncryptButton() {
        Button encryptButton = new Button("Encrypt");
        encryptButton.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Select File to Encrypt");

            // Set the initial directory to the current working directory
            File currentDir = new File(System.getProperty("user.dir"));
            if (currentDir.exists()) {
                fileChooser.setInitialDirectory(currentDir);
            }

            File selectedFile = fileChooser.showOpenDialog(Main.getStage());

            if (selectedFile != null) {
                try {
                    Path filePath = selectedFile.toPath();
                    String fileName = selectedFile.getName();
                    boolean success = new FileEncryption().encryptFile(filePath, fileName);

                    if (success) {
                        // Show a success message
                        Alert alert = new Alert(Alert.AlertType.INFORMATION);
                        alert.setTitle("Encryption Complete");
                        alert.setHeaderText(null);
                        alert.setContentText("File encrypted successfully!");
                        alert.showAndWait();

                        // Ask if the user wants to delete the original file
                        Alert confirmDelete = new Alert(
                                Alert.AlertType.CONFIRMATION,
                                "Do you want to delete the original file?\nThis is more secure as the \nonly way to access the file again would be through this program.",
                                ButtonType.YES,
                                ButtonType.NO
                        );
                        confirmDelete.setTitle("Delete Original File");
                        confirmDelete.setHeaderText("Secure Deletion Option");

                        // Process the user's choice
                        confirmDelete.showAndWait().ifPresent(response -> {
                            if (response == ButtonType.YES) {
                                try {
                                    // Permanently delete the file (without moving to recycle bin)
                                    Files.delete(filePath);

                                    // Show confirmation that file was deleted
                                    Alert deleteSuccess = new Alert(Alert.AlertType.INFORMATION);
                                    deleteSuccess.setTitle("File Deleted");
                                    deleteSuccess.setHeaderText(null);
                                    deleteSuccess.setContentText("Original file has been permanently deleted.");
                                    deleteSuccess.showAndWait();
                                } catch (IOException ex) {
                                    // Show error if deletion fails
                                    Alert deleteError = new Alert(Alert.AlertType.ERROR);
                                    deleteError.setTitle("Deletion Failed");
                                    deleteError.setHeaderText(null);
                                    deleteError.setContentText("Could not delete the original file: " + ex.getMessage());
                                    deleteError.showAndWait();
                                }
                            }
                        });
                    } else {
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setTitle("Encryption Failed");
                        alert.setHeaderText(null);
                        alert.setContentText("Failed to encrypt file: \nFile might be corrupted or private key is invalid");
                        alert.showAndWait();
                    }
                } catch (Exception ex) {
                    // Show an error message
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Encryption Failed");
                    alert.setHeaderText(null);
                    alert.setContentText("Failed to encrypt file: " + ex.getMessage());
                    alert.showAndWait();
                }
            }
        });
        return encryptButton;
    }

    // Helper method to return VBox layout
    private static VBox getVBox(Button encryptButton, Button decryptButton, Button viewButton, Button exitButton) {
        VBox layout = new VBox(10, encryptButton, decryptButton, viewButton, exitButton);
        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new Insets(20));
        return layout;
    }

    /**
     * Shows a Windows-like file chooser dialog that displays decrypted file names
     *
     * @return The selected file's encrypted name, or null if no file was selected
     */
    private String showDecryptFileChooser() {
        try {
            // Create a new stage for the file selection dialog
            Stage fileChooserStage = new Stage();
            fileChooserStage.setTitle("Select File to Decrypt");
            fileChooserStage.initModality(Modality.APPLICATION_MODAL);
            fileChooserStage.initOwner(stage);

            // Get the list of files in the vault with their original and encrypted names
            Map<String, String> files = new FileEncryption().listVaultFiles();

            // Create a ListView to display files (simpler like Windows Explorer)
            ListView<String> fileListView = new ListView<>();
            fileListView.setPlaceholder(new Label("No files found in vault"));

            // Create a map to track which original name maps to which encrypted name
            Map<String, String> displayToEncryptedMap = new HashMap<>();

            // Populate the file list with original (decrypted) names
            for (Map.Entry<String, String> entry : files.entrySet()) {
                String displayName = entry.getKey();
                fileListView.getItems().add(displayName);
                displayToEncryptedMap.put(displayName, entry.getValue());
            }

            // Create buttons in a horizontal layout similar to Windows dialogs
            Button openButton = new Button("Open");
            openButton.setDisable(true); // Initially disabled until file is selected
            openButton.setDefaultButton(true);

            Button cancelButton = new Button("Cancel");
            cancelButton.setCancelButton(true);

            HBox buttonBox = new HBox(10, openButton, cancelButton);
            buttonBox.setAlignment(Pos.CENTER_RIGHT);
            buttonBox.setPadding(new Insets(10, 0, 0, 0));

            // Add a simple address bar (just for familiarity)
            TextField addressBar = new TextField("Secure Vault");
            addressBar.setEditable(false);

            // Create a title label for the file list
            Label fileListLabel = new Label("Files:");

            // Enable the open button when a file is selected
            fileListView.getSelectionModel().selectedItemProperty().addListener(
                    (obs, oldSelection, newSelection) -> openButton.setDisable(newSelection == null));

            // Create final wrapper variables for communication between action handlers
            final boolean[] cancelled = {true};
            final String[] selectedEncryptedName = {null};

            // Set button actions
            openButton.setOnAction(e -> {
                String selectedDisplayName = fileListView.getSelectionModel().getSelectedItem();
                if (selectedDisplayName != null) {
                    selectedEncryptedName[0] = displayToEncryptedMap.get(selectedDisplayName);
                    cancelled[0] = false;
                    fileChooserStage.close();
                }
            });

            cancelButton.setOnAction(e -> fileChooserStage.close());

            // Double-click to select (like Windows Explorer)
            fileListView.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && fileListView.getSelectionModel().getSelectedItem() != null) {
                    String selectedDisplayName = fileListView.getSelectionModel().getSelectedItem();
                    selectedEncryptedName[0] = displayToEncryptedMap.get(selectedDisplayName);
                    cancelled[0] = false;
                    fileChooserStage.close();
                }
            });

            // Create the layout
            VBox layout = new VBox(10);
            layout.setPadding(new Insets(10));
            layout.getChildren().addAll(addressBar, fileListLabel, fileListView, buttonBox);
            VBox.setVgrow(fileListView, Priority.ALWAYS);

            // Set the scene and show the stage
            Scene scene = new Scene(layout, 450, 400);
            fileChooserStage.setScene(scene);
            fileChooserStage.showAndWait(); // This blocks until the dialog is closed

            // Return the selected encrypted name or null if canceled
            return cancelled[0] ? null : selectedEncryptedName[0];

        } catch (Exception e) {
            showErrorAlert("Could not display file list: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    private void showErrorAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}