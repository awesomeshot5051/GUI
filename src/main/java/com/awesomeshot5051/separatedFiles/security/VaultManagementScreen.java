package com.awesomeshot5051.separatedFiles.security;

import com.awesomeshot5051.*;
import com.awesomeshot5051.separatedFiles.*;
import com.awesomeshot5051.separatedFiles.session.*;
import javafx.animation.*;
import javafx.application.*;
import javafx.collections.*;
import javafx.concurrent.*;
import javafx.geometry.*;
import javafx.geometry.Insets;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.*;
import javafx.stage.*;
import javafx.util.*;

import java.awt.*;
import java.io.*;
import java.nio.file.*;
import java.sql.*;
import java.util.*;
import java.util.List;

public class VaultManagementScreen {
    private final Stage stage;

    public VaultManagementScreen() {
        this.stage = Main.getStage();
    }

    // GUI for Vault Management
    public void VaultManagementMainGUI() {
        stage.setTitle("Vault Management");

        // Buttons
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

        Button openVaultFolder = new Button("Open Vault Folder");
        openVaultFolder.setOnAction(e -> {
            try {
                File vaultFolder = new FileEncryption().getVaultDirectory().toFile();
                Desktop.getDesktop().open(vaultFolder);
            } catch (Exception ex) {
                showErrorAlert("Could not open vault folder", ex.getMessage());
            }
        });

        Button exitButton = new Button("Exit");
        exitButton.setOnAction(e -> {
            new MainScreen(SessionManager.getGroupType(), SessionManager.getStatus(), SessionManager.getUsername(), SessionManager.getName(), SessionManager.getConnection(), stage);
        });

        VBox layout = getVBox(encryptButton, decryptButton, viewButton, openVaultFolder, exitButton);
        layout.getStyleClass().addAll("vault-screen", "root");  // Add both


        // Add a CSS class to your layout VBox
        layout.getStyleClass().add("vault-layout");

        // Apply the CSS stylesheet to the scene
        Scene scene = new Scene(layout, 300, 250);
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/styles/vault.css")).toExternalForm());

        // Add CSS classes to buttons to style them
        encryptButton.getStyleClass().add("vault-button");
        decryptButton.getStyleClass().add("vault-button");
        viewButton.getStyleClass().add("vault-button");
        openVaultFolder.getStyleClass().add("vault-button");
        exitButton.getStyleClass().add("vault-button");

        stage.setScene(scene);
        stage.show();
    }

    private Button getDecryptButton() {
        Button decryptButton = new Button("Decrypt");
        decryptButton.setOnAction(e -> {
            // Use our custom Windows-like file chooser that shows decrypted names with multi-select
            List<String> selectedEncryptedNames = showMultiSelectDecryptFileChooser();

            if (!selectedEncryptedNames.isEmpty()) {
                // Ask the user where to save the decrypted files
                DirectoryChooser dirChooser = new DirectoryChooser();
                dirChooser.setTitle("Select Directory to Save Decrypted Files");
                File targetDir = dirChooser.showDialog(stage);

                if (targetDir != null) {
                    // Create a background task for decryption
                    DecryptionService decryptionService = new DecryptionService(selectedEncryptedNames, targetDir.toPath());

                    // Notify user that decryption will continue in background
                    Alert backgroundAlert = new Alert(Alert.AlertType.INFORMATION);
                    backgroundAlert.setTitle("Background Processing");
                    backgroundAlert.setHeaderText(null);
                    backgroundAlert.setContentText("Decryption will continue in the background.\n" +
                            "You can continue using the application, and you'll be\n" +
                            "notified when the decryption process is complete.");
                    backgroundAlert.show();

                    // Auto-close the notification after 3 seconds
                    PauseTransition delay = new PauseTransition(Duration.seconds(3));
                    delay.setOnFinished(event -> backgroundAlert.close());
                    delay.play();

                    // Start the decryption service
                    decryptionService.start();
                }
            }
        });
        return decryptButton;
    }

    private Button getEncryptButton() {
        Button encryptButton = new Button("Encrypt");
        encryptButton.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Select Files to Encrypt");

            // Set the initial directory to the current working directory
            File currentDir = new File(System.getProperty("user.dir"));
            if (currentDir.exists()) {
                fileChooser.setInitialDirectory(currentDir);
            }

            // Allow multiple file selection
            List<File> selectedFiles = fileChooser.showOpenMultipleDialog(Main.getStage());

            if (selectedFiles != null && !selectedFiles.isEmpty()) {
                // Create a background task service for encryption
                EncryptionService encryptionService = new EncryptionService(selectedFiles);

                // Notify user that encryption will continue in background
                Alert backgroundAlert = new Alert(Alert.AlertType.INFORMATION);
                backgroundAlert.setTitle("Background Processing");
                backgroundAlert.setHeaderText(null);
                backgroundAlert.setContentText("Encryption will continue in the background.\n" +
                        "You can continue using the application, and you'll be\n" +
                        "notified when the encryption process is complete.");
                backgroundAlert.show();

                // Auto-close the notification after 3 seconds
                PauseTransition delay = new PauseTransition(Duration.seconds(3));
                delay.setOnFinished(event -> backgroundAlert.close());
                delay.play();

                // Start the encryption service
                encryptionService.start();
            }
        });
        return encryptButton;
    }

    // Helper method to return VBox layout
    private static VBox getVBox(Button encryptButton, Button decryptButton, Button viewButton, Button openVaultFolderButton, Button exitButton) {
        VBox layout = new VBox(10, encryptButton, decryptButton, viewButton, openVaultFolderButton, exitButton);
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
            showErrorAlert("Could not display file list: " + e.getMessage(), "An error occurred during encryption: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Shows a Windows-like file chooser dialog that displays decrypted file names with multi-selection
     *
     * @return List of selected files' encrypted names, or empty list if no files were selected
     */
    private List<String> showMultiSelectDecryptFileChooser() {
        try {
            // Create a new stage for the file selection dialog
            Stage fileChooserStage = new Stage();
            fileChooserStage.setTitle("Select Files to Decrypt");
            fileChooserStage.initModality(Modality.APPLICATION_MODAL);
            fileChooserStage.initOwner(stage);

            // Get the list of files in the vault with their original and encrypted names
            Map<String, String> files = new FileEncryption().listVaultFiles();

            // Create a ListView to display files with multiple selection mode
            ListView<String> fileListView = new ListView<>();
            fileListView.setPlaceholder(new Label("No files found in vault"));

            // Enable multiple selection
            fileListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

            // Create a map to track which original name maps to which encrypted name
            Map<String, String> displayToEncryptedMap = new HashMap<>();

            // Populate the file list with original (decrypted) names
            for (Map.Entry<String, String> entry : files.entrySet()) {
                String displayName = entry.getKey();
                fileListView.getItems().add(displayName);
                displayToEncryptedMap.put(displayName, entry.getValue());
            }

            // Create buttons in a horizontal layout
            Button openButton = new Button("Open");
            openButton.setDisable(true); // Initially disabled until file is selected
            openButton.setDefaultButton(true);

            Button cancelButton = new Button("Cancel");
            cancelButton.setCancelButton(true);

            HBox buttonBox = new HBox(10, openButton, cancelButton);
            buttonBox.setAlignment(Pos.CENTER_RIGHT);
            buttonBox.setPadding(new Insets(10, 0, 0, 0));

            // Add a simple address bar
            TextField addressBar = new TextField("Secure Vault");
            addressBar.setEditable(false);

            // Create a title label for the file list
            Label fileListLabel = new Label("Files (select multiple with Ctrl/Shift):");

            // Enable the open button when at least one file is selected
            fileListView.getSelectionModel().selectedItemProperty().addListener(
                    (obs, oldSelection, newSelection) ->
                            openButton.setDisable(fileListView.getSelectionModel().getSelectedItems().isEmpty()));

            // Create final wrapper variables for communication between action handlers
            final boolean[] cancelled = {true};
            final List<String> selectedEncryptedNames = new ArrayList<>();

            // Set button actions
            openButton.setOnAction(e -> {
                ObservableList<String> selectedDisplayNames = fileListView.getSelectionModel().getSelectedItems();
                if (selectedDisplayNames != null && !selectedDisplayNames.isEmpty()) {
                    for (String displayName : selectedDisplayNames) {
                        selectedEncryptedNames.add(displayToEncryptedMap.get(displayName));
                    }
                    cancelled[0] = false;
                    fileChooserStage.close();
                }
            });

            cancelButton.setOnAction(e -> fileChooserStage.close());

            // Create the layout
            VBox layout = new VBox(10);
            layout.setPadding(new Insets(10));
            layout.getChildren().addAll(addressBar, fileListLabel, fileListView, buttonBox);
            VBox.setVgrow(fileListView, Priority.ALWAYS);

            // Set the scene and show the stage
            Scene scene = new Scene(layout, 450, 400);
            fileChooserStage.setScene(scene);
            fileChooserStage.showAndWait(); // This blocks until the dialog is closed

            // Return the selected encrypted names or empty list if canceled
            return cancelled[0] ? new ArrayList<>() : selectedEncryptedNames;

        } catch (Exception e) {
            showErrorAlert("Could not display file list: " + e.getMessage(), "An error occurred during encryption: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    private void showErrorAlert(String message, String s) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Service for encrypting files in the background
     */
    private class EncryptionService extends Service<EncryptionResult> {
        private final List<File> filesToEncrypt;

        public EncryptionService(List<File> files) {
            this.filesToEncrypt = new ArrayList<>(files);
        }

        @Override
        protected Task<EncryptionResult> createTask() {
            return new Task<>() {
                @Override
                protected EncryptionResult call() throws Exception {
                    EncryptionResult result = new EncryptionResult();

                    // Store files that were successfully encrypted for potential deletion
                    List<Path> successfullyEncrypted = new ArrayList<>();

                    int totalFiles = filesToEncrypt.size();
                    int current = 0;

                    // Process each file in the task thread
                    for (File file : filesToEncrypt) {
                        try {
                            updateMessage("Encrypting: " + file.getName() + " (" + (current + 1) + "/" + totalFiles + ")");
                            updateProgress(current, totalFiles);

                            Path filePath = file.toPath();
                            String fileName = file.getName();

                            // Encrypt the file
                            boolean success = new FileEncryption().encryptFile(filePath, fileName);

                            if (success) {
                                result.succeeded.add(fileName);
                                successfullyEncrypted.add(filePath);
                            } else {
                                result.failed.add(fileName);
                            }
                        } catch (Exception ex) {
                            result.failed.add(file.getName());
                            result.exceptions.add(ex);
                        }

                        current++;
                        updateProgress(current, totalFiles);
                    }

                    result.successfullyEncryptedPaths = successfullyEncrypted;
                    return result;
                }
            };
        }

        @Override
        protected void succeeded() {
            EncryptionResult result = getValue();

            // Show completion notification
            Platform.runLater(() -> {
                // Create custom dialog for better notification
                Stage notificationStage = new Stage();
                notificationStage.initModality(Modality.NONE);
                notificationStage.setTitle("Encryption Complete");

                // Create content
                Label headerLabel = new Label("Encryption Process Complete");
                headerLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 16px;");

                String message = result.succeeded.size() + " of " + (result.succeeded.size() + result.failed.size()) +
                        " files were encrypted successfully.";
                Label messageLabel = new Label(message);
                messageLabel.setWrapText(true);

                VBox messageBox = new VBox(10, headerLabel, messageLabel);

                // Create button row
                Button closeButton = new Button("Close");
                closeButton.setOnAction(event -> notificationStage.close());

                Button deleteButton = new Button("Delete Original Files");
                deleteButton.setDisable(result.successfullyEncryptedPaths.isEmpty());

                deleteButton.setOnAction(event -> {
                    notificationStage.close();
                    offerToDeleteOriginals(result.successfullyEncryptedPaths);
                });

                HBox buttonBox = new HBox(10, deleteButton, closeButton);
                buttonBox.setAlignment(Pos.CENTER_RIGHT);

                // Main layout
                VBox mainLayout = new VBox(15);
                mainLayout.setPadding(new Insets(20));
                mainLayout.getChildren().addAll(messageBox, buttonBox);

                // Show the notification
                Scene scene = new Scene(mainLayout, 400, 200);
                notificationStage.setScene(scene);
                notificationStage.show();
            });
        }

        @Override
        protected void failed() {
            Throwable exception = getException();
            Platform.runLater(() -> {
                showErrorAlert("Encryption Error", "An error occurred during encryption: " +
                        (exception != null ? exception.getMessage() : "Unknown error"));
            });
        }
    }

    /**
     * Class to hold encryption results
     */
    private static class EncryptionResult {
        List<String> succeeded = new ArrayList<>();
        List<String> failed = new ArrayList<>();
        List<Exception> exceptions = new ArrayList<>();
        List<Path> successfullyEncryptedPaths = new ArrayList<>();
    }

    /**
     * Offers to delete original files after successful encryption
     */
    private void offerToDeleteOriginals(List<Path> filesToDelete) {
        if (filesToDelete.isEmpty()) return;

        Alert confirmDelete = new Alert(
                Alert.AlertType.CONFIRMATION,
                "Do you want to delete the original " + filesToDelete.size() + " file(s)?\n" +
                        "This is more secure as the only way to access the file(s) again would be through this program.",
                ButtonType.YES,
                ButtonType.NO
        );
        confirmDelete.setTitle("Delete Original Files");
        confirmDelete.setHeaderText("Secure Deletion Option");

        // Process user's choice
        confirmDelete.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                // Create a new deletion service
                DeletionService deletionService = new DeletionService(filesToDelete);

                // Create a progress dialog for deletion
                ProgressDialog deletionProgress = new ProgressDialog(deletionService);
                deletionProgress.setTitle("Deleting Files");
                deletionProgress.setHeaderText("Deleting original files...");

                // Start the deletion service
                deletionService.start();
            }
        });
    }

    /**
     * Service for file deletion in the background
     */
    private class DeletionService extends Service<DeletionResult> {
        private final List<Path> filesToDelete;

        public DeletionService(List<Path> files) {
            this.filesToDelete = new ArrayList<>(files);
        }

        @Override
        protected Task<DeletionResult> createTask() {
            return new Task<>() {
                @Override
                protected DeletionResult call() throws Exception {
                    DeletionResult result = new DeletionResult();

                    int totalFiles = filesToDelete.size();
                    int current = 0;

                    for (Path path : filesToDelete) {
                        try {
                            updateMessage("Deleting: " + path.getFileName());
                            updateProgress(current, totalFiles);

                            Files.delete(path);
                            result.succeeded.add(path.toString());
                        } catch (IOException ex) {
                            result.failed.add(path.toString());
                            result.exceptions.add(ex);
                        }

                        current++;
                        updateProgress(current, totalFiles);
                    }

                    return result;
                }
            };
        }

        @Override
        protected void succeeded() {
            DeletionResult result = getValue();

            Platform.runLater(() -> {
                Alert deleteComplete = new Alert(Alert.AlertType.INFORMATION);
                deleteComplete.setTitle("Deletion Complete");
                deleteComplete.setHeaderText(null);

                StringBuilder message = new StringBuilder();
                message.append(result.succeeded.size()).append(" files deleted successfully.");

                if (!result.failed.isEmpty()) {
                    message.append("\n").append(result.failed.size()).append(" files could not be deleted.");
                }

                deleteComplete.setContentText(message.toString());
                deleteComplete.showAndWait();
            });
        }

        @Override
        protected void failed() {
            Throwable exception = getException();
            Platform.runLater(() -> {
                showErrorAlert("Deletion Error", "An error occurred during file deletion: " +
                        (exception != null ? exception.getMessage() : "Unknown error"));
            });
        }
    }

    /**
     * Class to hold deletion results
     */
    private static class DeletionResult {
        List<String> succeeded = new ArrayList<>();
        List<String> failed = new ArrayList<>();
        List<Exception> exceptions = new ArrayList<>();
    }

    /**
     * Custom progress dialog that can be used with services
     */
    private class ProgressDialog extends Dialog<Void> {
        private final ProgressBar progressBar = new ProgressBar();
        private final Label messageLabel = new Label();

        public ProgressDialog(Service<?> service) {
            initModality(Modality.NONE);

            // Set up the dialog content
            VBox content = new VBox(10);
            content.setPadding(new Insets(20, 10, 10, 10));

            progressBar.setPrefWidth(300);
            progressBar.setProgress(0);

            messageLabel.setWrapText(true);

            content.getChildren().addAll(messageLabel, progressBar);
            getDialogPane().setContent(content);

            // Bind to service properties
            progressBar.progressProperty().bind(service.progressProperty());
            messageLabel.textProperty().bind(service.messageProperty());

            // Close the dialog when the service is done
            service.runningProperty().addListener((obs, wasRunning, isRunning) -> {
                if (!isRunning) {
                    Platform.runLater(this::close);
                }
            });

            // Add a cancel button that will cancel the service
            ButtonType cancelButtonType = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
            getDialogPane().getButtonTypes().add(cancelButtonType);

            setResultConverter(buttonType -> {
                if (buttonType == cancelButtonType) {
                    service.cancel();
                }
                return null;
            });

            // Auto-show the dialog
            Platform.runLater(this::show);
        }
    }

    /**
     * Service for decrypting files in the background
     */
    private class DecryptionService extends Service<DecryptionResult> {
        private final List<String> filesToDecrypt;
        private final Path targetDirectory;

        public DecryptionService(List<String> encryptedFileNames, Path targetDir) {
            this.filesToDecrypt = new ArrayList<>(encryptedFileNames);
            this.targetDirectory = targetDir;
        }

        @Override
        protected Task<DecryptionResult> createTask() {
            return new Task<>() {
                @Override
                protected DecryptionResult call() throws Exception {
                    DecryptionResult result = new DecryptionResult();
                    Map<String, String> fileNameMap = new HashMap<>(); // Map from encrypted name to original name

                    int totalFiles = filesToDecrypt.size();
                    int current = 0;

                    FileEncryption fileEncryption = new FileEncryption();

                    // Process each encrypted file
                    for (String encryptedName : filesToDecrypt) {
                        try {
                            // Get the original filename
                            String originalFileName = fileEncryption.getOriginalFileName(encryptedName);
                            fileNameMap.put(encryptedName, originalFileName);

                            // Update the task message and progress
                            updateMessage("Decrypting: " + originalFileName + " (" + (current + 1) + "/" + totalFiles + ")");
                            updateProgress(current, totalFiles);

                            // Create the target file path
                            Path targetPath = targetDirectory.resolve(originalFileName);

                            // Check if file already exists
                            boolean proceed = true;
                            if (Files.exists(targetPath)) {
                                // Create a copy of the filename with a number suffix
                                String baseFileName = originalFileName;
                                String extension = "";
                                int lastDot = originalFileName.lastIndexOf('.');
                                if (lastDot > 0) {
                                    baseFileName = originalFileName.substring(0, lastDot);
                                    extension = originalFileName.substring(lastDot);
                                }

                                // Try to find an available filename
                                int counter = 1;
                                while (Files.exists(targetPath) && counter < 100) {
                                    String newFileName = baseFileName + " (" + counter + ")" + extension;
                                    targetPath = targetDirectory.resolve(newFileName);
                                    counter++;
                                }

                                // If we still can't find an available name, skip this file
                                if (Files.exists(targetPath)) {
                                    result.skipped.add(originalFileName);
                                    proceed = false;
                                }
                            }

                            if (proceed) {
                                // Check connection and reconnect if needed
                                Connection connection = Main.getConnection();
                                if (connection == null || connection.isClosed()) {
                                    Main.getInstance().connectToDatabase();
                                }

                                // Decrypt the file
                                boolean success = fileEncryption.decryptFile(encryptedName, targetPath);

                                if (success) {
                                    result.succeeded.add(originalFileName);
                                } else {
                                    result.failed.add(originalFileName);
                                }
                            }
                        } catch (Exception ex) {
                            String fileName = fileNameMap.getOrDefault(encryptedName, encryptedName);
                            result.failed.add(fileName);
                            result.exceptions.add(ex);
                        }

                        current++;
                        updateProgress(current, totalFiles);
                    }

                    return result;
                }
            };
        }

        @Override
        protected void succeeded() {
            DecryptionResult result = getValue();

            // Show completion notification on the JavaFX application thread
            Platform.runLater(() -> {
                // Create custom dialog for better notification
                Stage notificationStage = new Stage();
                notificationStage.initModality(Modality.NONE);
                notificationStage.setTitle("Decryption Complete");

                // Create content
                Label headerLabel = new Label("Decryption Process Complete");
                headerLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 16px;");

                StringBuilder messageBuilder = new StringBuilder();
                messageBuilder.append(result.succeeded.size()).append(" of ")
                        .append(result.succeeded.size() + result.failed.size() + result.skipped.size())
                        .append(" files were decrypted successfully.");

                if (!result.skipped.isEmpty()) {
                    messageBuilder.append("\n").append(result.skipped.size())
                            .append(" files were skipped (already exist or other issues).");
                }

                if (!result.failed.isEmpty()) {
                    messageBuilder.append("\n").append(result.failed.size())
                            .append(" files failed to decrypt.");
                }

                Label messageLabel = new Label(messageBuilder.toString());
                messageLabel.setWrapText(true);

                // Show path where files were saved
                Label pathLabel = new Label("Files were saved to:\n" + targetDirectory.toString());
                pathLabel.setWrapText(true);

                VBox messageBox = new VBox(10, headerLabel, messageLabel, pathLabel);

                // Create button to open the directory
                Button openDirButton = new Button("Open Directory");
                openDirButton.setOnAction(event -> {
                    try {
                        Desktop.getDesktop().open(targetDirectory.toFile());
                    } catch (IOException e) {
                        showErrorAlert("Could not open directory: ", e.getMessage());
                    }
                });

                Button closeButton = new Button("Close");
                closeButton.setOnAction(event -> notificationStage.close());

                HBox buttonBox = new HBox(10, openDirButton, closeButton);
                buttonBox.setAlignment(Pos.CENTER_RIGHT);

                // Show details of failed files if any
                VBox detailsBox = new VBox();
                if (!result.failed.isEmpty()) {
                    TitledPane failedFilesPane = createFailedFilesPane(result.failed);
                    detailsBox.getChildren().add(failedFilesPane);
                }

                // Main layout
                VBox mainLayout = new VBox(15);
                mainLayout.setPadding(new Insets(20));
                mainLayout.getChildren().addAll(messageBox, detailsBox, buttonBox);

                // Show the notification
                Scene scene = new Scene(mainLayout, 500,
                        Math.min(400, 200 + result.failed.size() * 15));
                notificationStage.setScene(scene);
                notificationStage.show();
            });
        }

        @Override
        protected void failed() {
            Throwable exception = getException();
            Platform.runLater(() -> {
                showErrorAlert("Decryption Error", "An error occurred during decryption: " +
                        (exception != null ? exception.getMessage() : "Unknown error"));
            });
        }

        private TitledPane createFailedFilesPane(List<String> failedFiles) {
            ListView<String> listView = new ListView<>();
            for (String file : failedFiles) {
                listView.getItems().add(file);
            }
            listView.setPrefHeight(Math.min(200, failedFiles.size() * 24 + 10));

            TitledPane titledPane = new TitledPane("Failed Files", listView);
            titledPane.setExpanded(false);
            return titledPane;
        }
    }

    /**
     * Class to hold decryption results
     */
    private static class DecryptionResult {
        List<String> succeeded = new ArrayList<>();
        List<String> failed = new ArrayList<>();
        List<String> skipped = new ArrayList<>();
        List<Exception> exceptions = new ArrayList<>();
    }
}