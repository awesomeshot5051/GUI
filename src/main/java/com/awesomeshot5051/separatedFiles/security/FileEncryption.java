package com.awesomeshot5051.separatedFiles.security;

import com.awesomeshot5051.*;
import javafx.geometry.*;
import javafx.geometry.Insets;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
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
import java.util.stream.*;

public class FileEncryption {

    private static final String ENCRYPTION_ALGORITHM = "AES";
    private static final String CIPHER_TRANSFORMATION = "AES/CBC/PKCS5Padding";
    private static final int IV_LENGTH = 16;

    private final Path vaultDirectory;
    private final SecureRandom secureRandom;
    private final SecureFileNameEncryptor fileNameEncryptor;

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

        byte[] iv = new byte[IV_LENGTH];
        secureRandom.nextBytes(iv);
        IvParameterSpec ivSpec = new IvParameterSpec(iv);
        Cipher cipher = Cipher.getInstance(CIPHER_TRANSFORMATION);
        cipher.init(Cipher.ENCRYPT_MODE, aesKey, ivSpec);

        Path out = vaultDirectory.resolve(encryptedName);
        try (InputStream in = Files.newInputStream(sourceFile);
             OutputStream outStream = Files.newOutputStream(out)) {

            outStream.write(iv);
            byte[] buf = new byte[4096];
            int r;
            while ((r = in.read(buf)) != -1) {
                byte[] chunk = cipher.update(buf, 0, r);
                if (chunk != null) outStream.write(chunk);
            }
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
        Path encryptedPath = vaultDirectory.resolve(encryptedFileName);
        if (!Files.exists(encryptedPath)) {
            String alt = fileNameEncryptor.encryptFileName(encryptedFileName);
            encryptedPath = vaultDirectory.resolve(alt);
            if (!Files.exists(encryptedPath)) {
                throw new FileNotFoundException("Vault file not found: " + encryptedFileName);
            }
        }

        try (InputStream in = Files.newInputStream(encryptedPath);
             OutputStream out = Files.newOutputStream(targetFile)) {

            byte[] iv = new byte[IV_LENGTH];
            if (in.read(iv) != IV_LENGTH) throw new IOException("Cannot read IV");
            IvParameterSpec ivSpec = new IvParameterSpec(iv);

            Cipher cipher = Cipher.getInstance(CIPHER_TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, aesKey, ivSpec);

            byte[] buf = new byte[4096];
            int r;
            while ((r = in.read(buf)) != -1) {
                byte[] chunk = cipher.update(buf, 0, r);
                if (chunk != null) out.write(chunk);
            }
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
            Stage stage = new Stage();
            stage.setTitle("Vault Files");
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(Main.getStage());

            Map<String, String> files = listVaultFiles();

            TableView<Map.Entry<String, String>> table = new TableView<>();
            table.setPlaceholder(new Label("No files in vault"));

            TableColumn<Map.Entry<String, String>, String> nameCol = new TableColumn<>("Filename");
            nameCol.setCellValueFactory(e ->
                    new javafx.beans.property.SimpleStringProperty(e.getValue().getKey()));
            nameCol.setPrefWidth(250);

            TableColumn<Map.Entry<String, String>, Void> actCol = new TableColumn<>("Actions");
            actCol.setCellFactory(tc -> new TableCell<>() {
                private final Button viewBtn = new Button("View");
                private final Button editInt = new Button("Edit Internally");
                private final Button editExt = new Button("Edit Externally");
                private final HBox box = new HBox(5, viewBtn, editInt, editExt);

                {
                    viewBtn.setOnAction(e -> handleView(getIndex()));
                    editInt.setOnAction(e -> handleInternalEdit(getIndex()));
                    editExt.setOnAction(e -> handleExternalEdit(getIndex()));
                }


                @Override
                protected void updateItem(Void v, boolean empty) {
                    super.updateItem(v, empty);
                    setGraphic(empty ? null : box);
                }
            });
            actCol.setPrefWidth(270);

            table.getColumns().addAll(nameCol, actCol);
            table.getItems().addAll(files.entrySet());

            Button close = new Button("Close");
            close.setOnAction(e -> stage.close());

            VBox root = new VBox(10, new Label("Your Vault Files:"), table, close);
            root.setPadding(new Insets(10));
            root.setAlignment(Pos.CENTER);
            VBox.setVgrow(table, Priority.ALWAYS);

            stage.setScene(new Scene(root, 600, 500));
            stage.show();
        } catch (Exception ex) {
            showError("Error listing vault files", ex);
        }
    }

    private void handleView(int idx) {
        try {
            Map.Entry<String, String> e = new ArrayList<>(listVaultFiles().entrySet()).get(idx);
            viewFileContents(e.getValue(), e.getKey());
        } catch (Exception ex) {
            showError("View error", ex);
        }
    }

    private void handleInternalEdit(int idx) {
        try {
            Map.Entry<String, String> e = new ArrayList<>(listVaultFiles().entrySet()).get(idx);
            editFileWithIntegratedEditor(e.getValue(), e.getKey());
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

    private void viewFileContents(String encryptedName, String originalName) {
        try {
            Path tmp = Files.createTempFile("vault-view-", null);
            decryptFile(encryptedName, tmp);

            TextArea ta = new TextArea();
            ta.setEditable(false);
            try {
                ta.setText(Files.readString(tmp));
            } catch (IOException ignored) {
                ta.setText("<binary data>");
            }

            Button closeBtn = new Button("Close");
            closeBtn.setOnAction(e -> {
                deleteTemp(tmp);
                ((Stage) closeBtn.getScene().getWindow()).close();
            });

            VBox v = new VBox(10, ta, closeBtn);
            v.setPadding(new Insets(10));
            VBox.setVgrow(ta, Priority.ALWAYS);

            Stage st = new Stage();
            st.setTitle("Viewing: " + originalName);
            st.initModality(Modality.WINDOW_MODAL);
            st.initOwner(Main.getStage());
            st.setScene(new Scene(v, 600, 400));
            st.show();
        } catch (Exception ex) {
            showError("View file error", ex);
        }
    }

    private void editFileWithIntegratedEditor(String encryptedName, String originalName) throws Exception {
        Path tmp = Files.createTempFile("vault-edit-", null);
        decryptFile(encryptedName, tmp);

        String content = Files.readString(tmp);

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
        st.show();
    }

    private void openFileWithExternalEditor(String encryptedName, String originalName) throws Exception {
        Path tempDir = vaultDirectory.resolve("temp_edit");
        Files.createDirectories(tempDir);
        Path tmp = tempDir.resolve(originalName);

        decryptFile(encryptedName, tmp);
        Desktop.getDesktop().open(tmp.toFile());

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
     * Derives the AES key by hashing the user’s private key bytes.
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
            return !verifier.verifyKeys();
        } catch (Exception e) {
            return true;
        }
    }

    private void showError(String title, Exception ex) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(ex.getMessage());
        a.showAndWait();
    }

    private void showInfo(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }
}
