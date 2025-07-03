package com.awesomeshot5051.separatedFiles.security;

import com.awesomeshot5051.*;
import com.awesomeshot5051.separatedFiles.Styler.*;
import javafx.beans.property.*;
import javafx.geometry.*;
import javafx.geometry.Insets;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.image.*;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.stage.*;

import javax.crypto.*;
import javax.crypto.spec.*;
import java.awt.*;
import java.io.*;
import java.nio.charset.*;
import java.nio.file.*;
import java.security.*;
import java.util.*;
import java.util.List;
import java.util.stream.*;

public class FileEncryption {

    private static final String ENCRYPTION_ALGORITHM = "AES";
    private static final String CIPHER_TRANSFORMATION = "AES/GCM/NoPadding";
    private static final int GCM_IV_LENGTH = 12; // recommended length for GCM
    private static final int GCM_TAG_LENGTH = 128; // in bits

    // File type categories
    private static final Set<String> TEXT_EDITABLE_EXTENSIONS = new HashSet<>(Arrays.asList(
            "txt", "md", "java", "py", "js", "html", "css", "xml", "json", "csv", "log", "ini", "cfg", "properties"
    ));

    private static final Set<String> OFFICE_EDITABLE_EXTENSIONS = new HashSet<>(Arrays.asList(
            "doc", "docx", "xls", "xlsx", "ppt", "pptx", "odt", "ods", "odp"
    ));

    private static final Set<String> IMAGE_EXTENSIONS = new HashSet<>(Arrays.asList(
            "jpg", "jpeg", "png", "gif", "bmp", "tiff", "svg"
    ));

    private static final Set<String> PDF_EXTENSIONS = new HashSet<>(
            Collections.singletonList("pdf")
    );

    private Path vaultDirectory;
    private final SecureRandom secureRandom;
    private final SecureFileNameEncryptor fileNameEncryptor;

    public Path getVaultDirectory() {
        return vaultDirectory;
    }

    // Enum to represent different file types
    public enum FileType {
        TEXT,           // Plain text files (.txt, .md, .java, etc.)
        IMAGE,          // Image files (.jpg, .png, etc.)
        PDF,            // PDF documents
        OFFICE_DOCUMENT,// Office documents (.docx, .xlsx, etc.)
        BINARY          // Other binary files
    }

    public FileEncryption() throws Exception {
        SecurePathManager scp = new SecurePathManager();
        scp.ensureAllDirectoriesExist();
        this.vaultDirectory = scp.getVaultDir();
        this.secureRandom = new SecureRandom();
        this.fileNameEncryptor = new SecureFileNameEncryptor();
    }

    public String getEncryptedFileName(String original) throws Exception {
        return fileNameEncryptor.encryptFileName(original);
    }

    public String getOriginalFileName(String encrypted) throws Exception {
        return fileNameEncryptor.decryptFileName(encrypted);
    }

    /**
     * Determines file type category based on file extension and contents
     *
     * @param fileName     Original file name with extension
     * @param fileContents Optional byte content of file for more accurate detection
     * @return FileType enum representing the detected type
     */
    public FileType detectFileType(String fileName, byte[] fileContents) {
        // Get extension
        String extension = "";
        int lastDot = fileName.lastIndexOf('.');
        if (lastDot > 0 && lastDot < fileName.length() - 1) {
            extension = fileName.substring(lastDot + 1).toLowerCase();
        }

        // Check by extension first
        if (TEXT_EDITABLE_EXTENSIONS.contains(extension)) {
            return FileType.TEXT;
        } else if (OFFICE_EDITABLE_EXTENSIONS.contains(extension)) {
            return FileType.OFFICE_DOCUMENT;
        } else if (IMAGE_EXTENSIONS.contains(extension)) {
            return FileType.IMAGE;
        } else if (PDF_EXTENSIONS.contains(extension)) {
            return FileType.PDF;
        }

        // If content is available and no extension match, try to detect by content
        if (fileContents != null && fileContents.length > 0) {
            // Check if content appears to be text (high proportion of ASCII chars)
            int asciiCount = 0;
            int sampleSize = Math.min(fileContents.length, 1000); // Check up to 1000 bytes
            for (int i = 0; i < sampleSize; i++) {
                if ((fileContents[i] >= 32 && fileContents[i] <= 126) ||
                        fileContents[i] == 9 || fileContents[i] == 10 || fileContents[i] == 13) {
                    asciiCount++;
                }
            }

            if ((double) asciiCount / sampleSize > 0.8) { // If >80% is ASCII, likely text
                return FileType.TEXT;
            }

            // Check for common file signatures (magic numbers)
            if (fileContents.length > 4) {
                // PDF signature
                if (fileContents[0] == '%' && fileContents[1] == 'P' &&
                        fileContents[2] == 'D' && fileContents[3] == 'F') {
                    return FileType.PDF;
                }

                // Some image formats
                if (fileContents[0] == (byte) 0xFF && fileContents[1] == (byte) 0xD8) {
                    return FileType.IMAGE; // JPEG
                }
                if (fileContents[0] == (byte) 0x89 && fileContents[1] == 'P' &&
                        fileContents[2] == 'N' && fileContents[3] == 'G') {
                    return FileType.IMAGE; // PNG
                }
            }
        }

        // Default to binary if we can't determine
        return FileType.BINARY;
    }

    public byte[] encryptText(String inputText) throws Exception {
        new PublicKeyManager().rotatePublicKey();
        if (!new KeyVerifier(
                new PublicKeyManager().getPublicKey().publicKey(),
                new PrivateKeyManager().loadPrivateKey()
        ).verifyKeys()) {
            throw new SecurityException("Key verification failed. Cannot encrypt text.");
        }

        SecretKey aesKey = deriveAesKey();

        byte[] iv = new byte[GCM_IV_LENGTH];
        secureRandom.nextBytes(iv);
        GCMParameterSpec gcmSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);

        Cipher cipher = Cipher.getInstance(CIPHER_TRANSFORMATION);
        cipher.init(Cipher.ENCRYPT_MODE, aesKey, gcmSpec);

        byte[] plaintextBytes = inputText.getBytes(StandardCharsets.UTF_8);
        byte[] ciphertext = cipher.doFinal(plaintextBytes);

        // Combine IV + ciphertext
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        outputStream.write(iv);
        outputStream.write(ciphertext);

        return outputStream.toByteArray();
    }

    public String decryptText(byte[] encryptedData) throws Exception {
        if (!new KeyVerifier(
                new PublicKeyManager().getPublicKey().publicKey(),
                new PrivateKeyManager().loadPrivateKey()
        ).verifyKeys()) {
            throw new SecurityException("Key verification failed. Cannot decrypt text.");
        }

        SecretKey aesKey = deriveAesKey();

        if (encryptedData.length < GCM_IV_LENGTH) {
            throw new IllegalArgumentException("Encrypted data is too short to contain an IV.");
        }

        // Extract IV and ciphertext
        byte[] iv = Arrays.copyOfRange(encryptedData, 0, GCM_IV_LENGTH);
        byte[] ciphertext = Arrays.copyOfRange(encryptedData, GCM_IV_LENGTH, encryptedData.length);

        GCMParameterSpec gcmSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
        Cipher cipher = Cipher.getInstance(CIPHER_TRANSFORMATION);
        cipher.init(Cipher.DECRYPT_MODE, aesKey, gcmSpec);

        byte[] plaintextBytes = cipher.doFinal(ciphertext);
        return new String(plaintextBytes, StandardCharsets.UTF_8);
    }

    public List<byte[]> batchEncryptText(String... texts) throws Exception {
        new PublicKeyManager().rotatePublicKey();
        if (!new KeyVerifier(
                new PublicKeyManager().getPublicKey().publicKey(),
                new PrivateKeyManager().loadPrivateKey()
        ).verifyKeys()) {
            throw new SecurityException("Key verification failed. Cannot encrypt text.");
        }

        SecretKey aesKey = deriveAesKey();
        List<byte[]> encryptedList = new ArrayList<>();

        for (String inputText : texts) {
            byte[] iv = new byte[GCM_IV_LENGTH];
            secureRandom.nextBytes(iv);
            GCMParameterSpec gcmSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);

            Cipher cipher = Cipher.getInstance(CIPHER_TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, aesKey, gcmSpec);

            byte[] plaintextBytes = inputText.getBytes(StandardCharsets.UTF_8);
            byte[] ciphertext = cipher.doFinal(plaintextBytes);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            outputStream.write(iv);
            outputStream.write(ciphertext);

            encryptedList.add(outputStream.toByteArray());
        }

        return encryptedList;
    }

    public List<String> batchDecryptText(byte[]... encryptedBlobs) throws Exception {
        if (!new KeyVerifier(
                new PublicKeyManager().getPublicKey().publicKey(),
                new PrivateKeyManager().loadPrivateKey()
        ).verifyKeys()) {
            throw new SecurityException("Key verification failed. Cannot decrypt text.");
        }

        SecretKey aesKey = deriveAesKey();
        List<String> results = new ArrayList<>();

        for (byte[] encryptedData : encryptedBlobs) {
            if (encryptedData.length < GCM_IV_LENGTH) {
                throw new IllegalArgumentException("Encrypted data too short to contain IV.");
            }

            byte[] iv = Arrays.copyOfRange(encryptedData, 0, GCM_IV_LENGTH);
            byte[] ciphertext = Arrays.copyOfRange(encryptedData, GCM_IV_LENGTH, encryptedData.length);

            GCMParameterSpec gcmSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            Cipher cipher = Cipher.getInstance(CIPHER_TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, aesKey, gcmSpec);

            byte[] plaintextBytes = cipher.doFinal(ciphertext);
            results.add(new String(plaintextBytes, StandardCharsets.UTF_8));
        }

        return results;
    }

    public boolean encryptFile(Path sourceFile, String originalFileName) throws Exception {
        // Rotate public key before each encryption
        new PublicKeyManager().rotatePublicKey();
        if (!new KeyVerifier(
                new PublicKeyManager().getPublicKey().publicKey(),
                new PrivateKeyManager().loadPrivateKey()
        ).verifyKeys()
        ) {
            throw new SecurityException("Key verification failed. Cannot encrypt file.");
        }

        SecretKey aesKey = deriveAesKey();
        String encryptedName = fileNameEncryptor.encryptFileName(originalFileName);

        // Generate IV for GCM (12 bytes is recommended)
        byte[] iv = new byte[GCM_IV_LENGTH];
        secureRandom.nextBytes(iv);

        // Create GCM parameter spec with IV and tag length
        GCMParameterSpec gcmSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);

        Cipher cipher = Cipher.getInstance(CIPHER_TRANSFORMATION);
        cipher.init(Cipher.ENCRYPT_MODE, aesKey, gcmSpec);

        Path out = vaultDirectory.resolve(encryptedName);
        try (InputStream in = Files.newInputStream(sourceFile);
             OutputStream outStream = Files.newOutputStream(out)) {

            // Write IV first
            outStream.write(iv);

            byte[] buf = new byte[4096];
            int r;
            while ((r = in.read(buf)) != -1) {
                byte[] chunk = cipher.update(buf, 0, r);
                if (chunk != null) outStream.write(chunk);
            }

            // Write final block (includes authentication tag for GCM)
            byte[] finalBlock = cipher.doFinal();
            if (finalBlock != null) outStream.write(finalBlock);
        }

        return true;
    }

    public boolean decryptFile(String encryptedFileName, Path targetFile) throws Exception {
        if (!new KeyVerifier(
                new PublicKeyManager().getPublicKey().publicKey(),
                new PrivateKeyManager().loadPrivateKey()
        ).verifyKeys()
        ) {
            throw new SecurityException("Key verification failed. Cannot decrypt file.");
        }

        SecretKey aesKey = deriveAesKey();
        if (encryptedFileName.equals(getEncryptedFileName("accesskey.key"))) {
            vaultDirectory = vaultDirectory.resolve(".accessKey");
        }
        Path encryptedPath = vaultDirectory.resolve(encryptedFileName);
        try {
            if (!Files.exists(encryptedPath)) {
                String alt = fileNameEncryptor.encryptFileName(encryptedFileName);
                encryptedPath = vaultDirectory.resolve(alt);
                if (!Files.exists(encryptedPath)) {
                    throw new FileNotFoundException("Vault file not found: " + encryptedFileName);
                }
            }
        } catch (FileNotFoundException e) {
            Main.getErrorLogger().silentlyHandle(e);
        }

        try (InputStream in = Files.newInputStream(encryptedPath);
             OutputStream out = Files.newOutputStream(targetFile)) {

            // Read IV (12 bytes for GCM)
            byte[] iv = new byte[GCM_IV_LENGTH];
            if (in.read(iv) != GCM_IV_LENGTH) {
                throw new IOException("Cannot read IV - file may be corrupted or not encrypted with GCM");
            }

            // Create GCM parameter spec with IV and tag length
            GCMParameterSpec gcmSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);

            Cipher cipher = Cipher.getInstance(CIPHER_TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, aesKey, gcmSpec);

            byte[] buf = new byte[4096];
            int r;
            while ((r = in.read(buf)) != -1) {
                byte[] chunk = cipher.update(buf, 0, r);
                if (chunk != null) out.write(chunk);
            }

            // Process final block (includes authentication tag verification for GCM)
            byte[] finalBlock = cipher.doFinal();
            if (finalBlock != null) out.write(finalBlock);
        }

        return true;
    }

    public Map<String, String> listVaultFiles() throws Exception {
        Map<String, String> map = new LinkedHashMap<>();
        try (DirectoryStream<Path> ds = Files.newDirectoryStream(vaultDirectory)) {
            for (Path p : ds) {
                if (!Files.isRegularFile(p)) continue;
                String enc = p.getFileName().toString(), orig;
                try {
                    orig = fileNameEncryptor.decryptFileName(enc);
                } catch (Exception ex) {
                    orig = enc;
                }
                map.put(orig, enc);
            }
        }
        return map;
    }

    /**
     * Displays and manages vault files in a JavaFX UI, including
     * internal and external editing with proper key handling.
     */
    public void viewVaultFiles() {
        try {
            // Ensure per-user directories exist
            SecurePathManager spm = new SecurePathManager();
            spm.ensureAllDirectoriesExist();

            Stage stage = new Stage();
            stage.setTitle("Vault Files");
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(Main.getStage());

            Map<String, String> files = listVaultFiles();
            TableView<Map.Entry<String, String>> table = new TableView<>();
            table.setPlaceholder(new Label("No files in vault"));

            // Filename column
            TableColumn<Map.Entry<String, String>, String> nameCol = new TableColumn<>("Filename");
            nameCol.setCellValueFactory(e ->
                    new SimpleStringProperty(e.getValue().getKey()));
            nameCol.setPrefWidth(250);

            // 1) VIEW column — always visible
            TableColumn<Map.Entry<String, String>, Void> viewCol = new TableColumn<>("View");
            viewCol.setCellFactory(tc -> new TableCell<>() {
                private final Button btn = new Button("View");

                @Override
                protected void updateItem(Void v, boolean empty) {
                    super.updateItem(v, empty);
                    if (empty) {
                        setGraphic(null);
                    } else {
                        int idx = getIndex();
                        String orig = table.getItems().get(idx).getKey();
                        if (detectFileType(orig, null) != FileType.TEXT && detectFileType(orig, null) != FileType.OFFICE_DOCUMENT && detectFileType(orig, null) != FileType.PDF && detectFileType(orig, null) != FileType.IMAGE) {
                            setGraphic(null);
                        } else {
                            btn.setOnAction(e -> handleView(idx));
                            setGraphic(btn);
                        }
                    }
                }
            });
            viewCol.setPrefWidth(80);

            // 2) EDIT INTERNALLY — only for TEXT
            TableColumn<Map.Entry<String, String>, Void> editIntCol = new TableColumn<>("Edit Internally");
            editIntCol.setCellFactory(tc -> new TableCell<>() {
                private final Button btn = new Button("Edit");

                @Override
                protected void updateItem(Void v, boolean empty) {
                    super.updateItem(v, empty);
                    if (empty) {
                        setGraphic(null);
                    } else {
                        int idx = getIndex();
                        String orig = table.getItems().get(idx).getKey();
                        if (detectFileType(orig, null) == FileType.TEXT) {
                            btn.setOnAction(e -> handleInternalEdit(idx));
                            setGraphic(btn);
                        } else {
                            setGraphic(null);
                        }
                    }
                }
            });
            editIntCol.setPrefWidth(100);

            // 3) EDIT EXTERNALLY — for TEXT, IMAGE, PDF, OFFICE
            TableColumn<Map.Entry<String, String>, Void> editExtCol = new TableColumn<>("Edit Externally");
            editExtCol.setCellFactory(tc -> new TableCell<>() {
                private final Button btn = new Button("External");

                @Override
                protected void updateItem(Void v, boolean empty) {
                    super.updateItem(v, empty);
                    if (empty) {
                        setGraphic(null);
                    } else {
                        int idx = getIndex();
                        String orig = table.getItems().get(idx).getKey();
                        FileType type = detectFileType(orig, null);
                        if (type == FileType.TEXT
                                || type == FileType.OFFICE_DOCUMENT) {
                            btn.setOnAction(e -> handleExternalEdit(idx));
                            setGraphic(btn);
                        } else {
                            setGraphic(null);
                        }
                    }
                }
            });
            editExtCol.setPrefWidth(100);

            // 4) OPEN WITH SYSTEM DEFAULT — only for BINARY
            TableColumn<Map.Entry<String, String>, Void> openSysCol = new TableColumn<>("Open");
            openSysCol.setCellFactory(tc -> new TableCell<>() {
                private final Button btn = new Button("System");

                @Override
                protected void updateItem(Void v, boolean empty) {
                    super.updateItem(v, empty);
                    if (empty) {
                        setGraphic(null);
                    } else {
                        int idx = getIndex();
                        String orig = table.getItems().get(idx).getKey();
                        if (detectFileType(orig, null) == FileType.BINARY) {
                            btn.setOnAction(e -> handleOpenWithSystemDefault(idx));
                            setGraphic(btn);
                        } else {
                            setGraphic(null);
                        }
                    }
                }
            });
            openSysCol.setPrefWidth(100);

            // Assemble columns
            table.getColumns().addAll(List.of(nameCol, viewCol, editIntCol, editExtCol, openSysCol));
            table.getItems().addAll(files.entrySet());

            Button close = new Button("Close");
            close.setOnAction(e -> stage.close());

            VBox root = new VBox(10, new Label("Your Vault Files:"), table, close);
            root.setPadding(new Insets(10));
            root.setAlignment(Pos.CENTER);
            VBox.setVgrow(table, Priority.ALWAYS);

            stage.setScene(new Scene(root, 700, 500));
            stage.show();

        } catch (Exception ex) {
            showError("Error listing vault files", ex);
        }
    }

    private void handleView(int idx) {
        try {
            Map.Entry<String, String> e = new ArrayList<>(listVaultFiles().entrySet()).get(idx);
            String originalName = e.getKey();
            String encryptedName = e.getValue();

            // Create temp file for viewing
            Path tmp = Files.createTempFile("vault-view-", getFileExtension(originalName));
            decryptFile(encryptedName, tmp);

            // Detect file type
            byte[] fileContent = Files.readAllBytes(tmp);
            FileType fileType = detectFileType(originalName, fileContent);

            // Handle file based on type
            switch (fileType) {
                case TEXT:
                    viewTextFileContents(tmp, originalName);
                    break;
                case IMAGE:
                    viewImageFile(tmp, originalName);
                    break;
                case PDF:
                case OFFICE_DOCUMENT:
                    // Use OS default application for these types
                    viewWithExternalApp(tmp, originalName, false);
                    break;
                case BINARY:
                default:
                    // For any other file type, try to use the OS default application
                    viewWithExternalApp(tmp, originalName, false);
                    break;
            }
        } catch (Exception ex) {
            showError("View error", ex);
        }
    }

    private String getFileExtension(String filename) {
        int lastDot = filename.lastIndexOf('.');
        if (lastDot > 0) {
            return "." + filename.substring(lastDot + 1);
        }
        return "";
    }

    private void viewTextFileContents(Path filePath, String originalName) {
        try {
            TextArea ta = new TextArea();
            ta.setEditable(false);
            try {
                ta.setText(Files.readString(filePath));
            } catch (IOException ex) {
                ta.setText("<Could not read file as text - possibly binary content>");
            }

            Button closeBtn = new Button("Close");
            closeBtn.setOnAction(e -> {
                deleteTemp(filePath);
                ((Stage) closeBtn.getScene().getWindow()).close();
            });

            VBox v = new VBox(10, ta, closeBtn);
            v.setPadding(new Insets(10));
            VBox.setVgrow(ta, Priority.ALWAYS);

            Stage st = new Stage();
            st.setTitle("Viewing: " + originalName);
            st.initModality(Modality.WINDOW_MODAL);
            st.initOwner(Main.getStage());
            st.setScene(new Scene(v, 800, 600));
            st.setOnCloseRequest(e -> deleteTemp(filePath));
            st.show();
        } catch (Exception ex) {
            showError("View file error", ex);
            Main.getErrorLogger().handleException("View file error", ex);
        }
    }

    private void viewImageFile(Path filePath, String originalName) {
        try {
            // Create an image view for the decrypted image
            Image image = new Image(filePath.toUri().toString());
            ImageView imageView = new ImageView(image);
            imageView.setPreserveRatio(true);
            imageView.setFitWidth(800);
            imageView.setFitHeight(600);

            // Create a scroll pane to allow viewing large images
            ScrollPane scrollPane = new ScrollPane(imageView);
            scrollPane.setPannable(true);
            scrollPane.setFitToWidth(true);
            scrollPane.setFitToHeight(true);

            Button closeBtn = new Button("Close");
            closeBtn.setOnAction(e -> {
                deleteTemp(filePath);
                ((Stage) closeBtn.getScene().getWindow()).close();
            });

            Button openExternallyBtn = new Button("Open in Default Viewer");
            openExternallyBtn.setOnAction(e -> {
                try {
                    Desktop.getDesktop().open(filePath.toFile());
                } catch (IOException ex) {
                    showError("Cannot open external viewer", ex);
                }
            });

            HBox buttonBox = new HBox(10, openExternallyBtn, closeBtn);
            buttonBox.setAlignment(Pos.CENTER_RIGHT);
            buttonBox.setPadding(new Insets(10, 0, 0, 0));

            VBox root = new VBox(10, scrollPane, buttonBox);
            VBox.setVgrow(scrollPane, Priority.ALWAYS);
            root.setPadding(new Insets(10));

            Stage st = new Stage();
            st.setTitle("Viewing: " + originalName);
            st.initModality(Modality.WINDOW_MODAL);
            st.initOwner(Main.getStage());
            st.setScene(new Scene(root, 800, 600));
            st.setOnCloseRequest(e -> deleteTemp(filePath));
            st.show();
        } catch (Exception ex) {
            // Fallback to external viewer if JavaFX can't display the image
            viewWithExternalApp(filePath, originalName, false);
        }
    }

    private void viewWithExternalApp(Path filePath, String originalName, boolean deleteOnClose) {
        try {
            // Use the OS's file association mechanism to open the file
            Desktop.getDesktop().open(filePath.toFile());

            if (deleteOnClose) {
                // Show a dialog to let the user tell us when they're done
                Stage dialog = new Stage();
                dialog.initModality(Modality.WINDOW_MODAL);
                dialog.initOwner(Main.getStage());
                dialog.setTitle("External Viewer: " + originalName);

                Label message = new Label("The file is open in your system's default application.\n" +
                        "When you're done, please click the button below to close and clean up.");
                message.setWrapText(true);

                Button doneBtn = new Button("Done Viewing");
                doneBtn.setOnAction(e -> {
                    deleteTemp(filePath);
                    dialog.close();
                });

                VBox root = new VBox(20, message, doneBtn);
                root.setPadding(new Insets(20));
                root.setAlignment(Pos.CENTER);

                dialog.setScene(new Scene(root, 400, 200));
                dialog.setOnCloseRequest(e -> deleteTemp(filePath));
                dialog.show();
            }
        } catch (IOException ex) {
            showError("Cannot open with external application", ex);
        }
    }

    private void handleInternalEdit(int idx) {
        try {
            Map.Entry<String, String> e = new ArrayList<>(listVaultFiles().entrySet()).get(idx);
            String originalName = e.getKey();
            String encryptedName = e.getValue();

            // Only proceed with internal edit for text files
            if (detectFileType(originalName, null) == FileType.TEXT) {
                editFileWithIntegratedEditor(encryptedName, originalName);
            } else {
                showError("Unsupported File Type",
                        new UnsupportedOperationException("This file type doesn't support internal editing"));
            }
        } catch (Exception ex) {
            showError("Internal edit error", ex);
        }
    }

    private void handleExternalEdit(int idx) {
        try {
            Map.Entry<String, String> e = new ArrayList<>(listVaultFiles().entrySet()).get(idx);
            openFileWithExternalEditor(e.getValue(), e.getKey());
        } catch (Exception ex) {
            showError("External edit error", ex);
        }
    }

    private void editFileWithIntegratedEditor(String encryptedName, String originalName) throws Exception {
        Path tmp = Files.createTempFile("vault-edit-", getFileExtension(originalName));
        decryptFile(encryptedName, tmp);

        String content;
        try {
            content = Files.readString(tmp);
        } catch (IOException ex) {
            content = ""; // Handle binary files or encoding issues
        }

        TextArea ta = new TextArea(content);
        Button saveBtn = new Button("Save Changes");
        Button closeBtn = new Button("Close Without Saving");

        saveBtn.setOnAction(e -> {
            try {
                Files.writeString(tmp, ta.getText(), StandardCharsets.UTF_8);
                encryptFile(tmp, originalName);
                showInfo("Saved", "Your changes have been saved.");
                deleteTemp(tmp);
                ((Stage) saveBtn.getScene().getWindow()).close();
            } catch (Exception ex) {
                showError("Save failed", ex);
            }
        });

        closeBtn.setOnAction(e -> {
            deleteTemp(tmp);
            ((Stage) closeBtn.getScene().getWindow()).close();
        });

        HBox hb = new HBox(10, saveBtn, closeBtn);
        hb.setAlignment(Pos.CENTER_RIGHT);
        hb.setPadding(new Insets(10, 0, 0, 0));

        VBox v = new VBox(10, ta, hb);
        v.setPadding(new Insets(10));
        VBox.setVgrow(ta, Priority.ALWAYS);

        Stage st = new Stage();
        st.setTitle("Editing: " + originalName);
        st.initModality(Modality.WINDOW_MODAL);
        st.initOwner(Main.getStage());
        st.setScene(new Scene(v, 800, 600));
        st.setOnCloseRequest(e -> {
            e.consume(); // Prevent automatic closing
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION,
                    "Do you want to save your changes before closing?",
                    ButtonType.YES, ButtonType.NO, ButtonType.CANCEL);
            alert.setTitle("Save Changes?");
            FXAlertStyler.style(alert);
            alert.showAndWait().ifPresent(response -> {
                if (response == ButtonType.YES) {
                    try {
                        Files.writeString(tmp, ta.getText(), StandardCharsets.UTF_8);
                        encryptFile(tmp, originalName);
                        showInfo("Saved", "Your changes have been saved.");
                        deleteTemp(tmp);
                        st.close();
                    } catch (Exception ex) {
                        showError("Save failed", ex);
                    }
                } else if (response == ButtonType.NO) {
                    deleteTemp(tmp);
                    st.close();
                }
                // If CANCEL, do nothing and keep the dialog open
            });
        });
        st.show();
    }

    private void openFileWithExternalEditor(String encryptedName, String originalName) throws Exception {
        Path tempDir = vaultDirectory.resolve("temp_edit");
        Files.createDirectories(tempDir);
        Path tmp = tempDir.resolve(originalName);

        decryptFile(encryptedName, tmp);

        // Try to open the file with the system's default application
        try {
            Desktop.getDesktop().open(tmp.toFile());
        } catch (IOException ex) {
            showError("External Editor Error",
                    new IOException("Could not open file with system default application: " + ex.getMessage()));
            return;
        }

        Label lbl = new Label(
                "Edit in your external editor.\nWhen done, click Save or Discard below."
        );
        lbl.setWrapText(true);

        Button saveBtn = new Button("Save Changes");
        Button discardBtn = new Button("Discard Changes");

        saveBtn.setOnAction(e -> {
            try {
                encryptFile(tmp, originalName);
                deleteTemp(tmp);
                showInfo("Saved", "Your changes have been saved.");
                ((Stage) saveBtn.getScene().getWindow()).close();
            } catch (Exception ex) {
                showError("Save failed", ex);
            }
        });
        discardBtn.setOnAction(e -> {
            deleteTemp(tmp);
            ((Stage) discardBtn.getScene().getWindow()).close();
        });

        HBox hb = new HBox(10, saveBtn, discardBtn);
        hb.setAlignment(Pos.CENTER);
        hb.setPadding(new Insets(10));

        VBox v = new VBox(10, lbl, hb);
        v.setPadding(new Insets(10));

        Stage st = new Stage();
        st.setTitle("External Edit: " + originalName);
        st.initModality(Modality.WINDOW_MODAL);
        st.initOwner(Main.getStage());
        st.setScene(new Scene(v, 400, 200));
        st.show();
    }

    private void deleteTemp(Path tmp) {
        try {
            Files.deleteIfExists(tmp);
            Path dir = tmp.getParent();
            try (Stream<Path> s = Files.list(dir)) {
                if (s.findFirst().isEmpty()) Files.delete(dir);
            }
        } catch (IOException ignored) {
        }
    }

    /**
     * Derives the AES key by hashing the user's private key bytes.
     */
    private SecretKey deriveAesKey() throws Exception {
        String privPem = new PrivateKeyManager().loadPrivateKey();
        byte[] privBytes = Base64.getDecoder().decode(privPem);
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] keyBytes = md.digest(privBytes);
        return new SecretKeySpec(keyBytes, ENCRYPTION_ALGORITHM);
    }

    /**
     * Verifies that the stored public/private key pair is valid.
     */
    private boolean verifyKeys() {
        try {
            PublicKeyData pubPem = new PublicKeyManager().getPublicKey();

            String privPem = new PrivateKeyManager().loadPrivateKey();
            KeyVerifier verifier = new KeyVerifier(pubPem.publicKey(), privPem);
            return verifier.verifyKeys();
        } catch (Exception e) {
            return true;
        }
    }

    private void showError(String title, Exception ex) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        FXAlertStyler.style(a);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(ex.getMessage());
        a.showAndWait();
    }

    private void showInfo(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        FXAlertStyler.style(a);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }

    private void handleOpenWithSystemDefault(int idx) {
        try {
            Map.Entry<String, String> e = new ArrayList<>(listVaultFiles().entrySet()).get(idx);
            String originalName = e.getKey();
            String encryptedName = e.getValue();

            // Create temp file for viewing with system default application
            Path tmp = Files.createTempFile("vault-open-", getFileExtension(originalName));
            decryptFile(encryptedName, tmp);

            // Open with system default application
            viewWithExternalApp(tmp, originalName, true);
        } catch (Exception ex) {
            showError("Open with system default error", ex);
        }
    }
}