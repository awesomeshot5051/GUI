package com.awesomeshot5051.separatedFiles.systemConfiguration.folderManagement;

import com.awesomeshot5051.*;
import com.awesomeshot5051.separatedFiles.Styler.*;
import javafx.application.*;
import javafx.geometry.*;
import javafx.scene.*;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.*;
import org.checkerframework.checker.nullness.qual.*;

import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
import java.util.stream.*;

public class ManageFiles extends Application {
    // Create a thread pool with the number of available processors
    private static final int PROCESSOR_COUNT = Runtime.getRuntime().availableProcessors();
    private static final ExecutorService folderScanExecutor = Executors.newFixedThreadPool(PROCESSOR_COUNT);
    private static final ForkJoinPool forkJoinPool = new ForkJoinPool(PROCESSOR_COUNT);

    private static ProgressBar progressBar;
    private static Button cancelButton;
    private static Button backButton;
    private static Button upButton;
    private static ToggleButton sortSizeButton;
    private static Label currentPathLabel;
    private static Label summaryLabel;
    private static Label statusLabel;
    private static VBox fileListVBox;
    private static Path selectedFolder;
    private static Stack<Path> folderHistory = new Stack<>();
    private static volatile boolean cancelScan = false;
    private static AtomicInteger pendingTasks = new AtomicInteger(0);
    private static List<FolderInfo> currentFolderInfos = new ArrayList<>();
    private static boolean sortBySize = false;

    // Utility method to convert bytes into a human-readable format
    private static String humanReadableByteCount(long bytes) {
        if (bytes < 1024) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(1024));
        String pre = "KMGTPE".charAt(exp - 1) + "";
        return String.format("%.1f %sB", bytes / Math.pow(1024, exp), pre);
    }

    public static void launchFolderScanner() {
        ManageFiles folderScannerApp = new ManageFiles();
        Stage folderScannerStage = new Stage();
        folderScannerApp.start(folderScannerStage);
    }

    @Override
    public void start(Stage primaryStage) {
        progressBar = new ProgressBar(0);
        progressBar.getStyleClass().add("progress-bar");

        cancelButton = new Button("Cancel");
        cancelButton.getStyleClass().addAll("button", "button-cancel");

        Button refreshButton = new Button("Refresh");
        refreshButton.getStyleClass().add("button");

        Button browseButton = new Button("Browse...");
        browseButton.getStyleClass().add("button");

        backButton = new Button("Back");
        backButton.getStyleClass().add("button");

        upButton = new Button("Up");
        upButton.getStyleClass().add("button");

        sortSizeButton = new ToggleButton("Sort by Size");
        sortSizeButton.getStyleClass().add("toggle-button");

        cancelButton.setOnAction(e -> cancelScan());
        refreshButton.setOnAction(e -> {
            if (selectedFolder != null) loadFolderData(selectedFolder);
        });
        browseButton.setOnAction(e -> openNewFolder(primaryStage));
        backButton.setOnAction(e -> goBackFolder());
        upButton.setOnAction(e -> goUpFolder());
        sortSizeButton.setOnAction(e -> {
            sortBySize = sortSizeButton.isSelected();
            updateFileList();
        });

        HBox buttonBar = new HBox(10, cancelButton, refreshButton, browseButton, backButton, upButton, sortSizeButton);
        buttonBar.setPadding(new Insets(10));
        buttonBar.setAlignment(Pos.CENTER_LEFT);
        buttonBar.getStyleClass().add("hbox");

        currentPathLabel = new Label();
        currentPathLabel.getStyleClass().add("label-bold");

        summaryLabel = new Label();
        summaryLabel.getStyleClass().add("label-bold");
        summaryLabel.setPadding(new Insets(5, 0, 5, 0));

        statusLabel = new Label();
        statusLabel.getStyleClass().add("status-label");

        fileListVBox = new VBox(10);
        fileListVBox.setPadding(new Insets(10));
        fileListVBox.getStyleClass().add("vbox");

        ScrollPane scrollPane = new ScrollPane(fileListVBox);
        scrollPane.setFitToWidth(true);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
        scrollPane.getStyleClass().add("scroll-pane");

        VBox mainLayout = new VBox(10, progressBar, buttonBar, currentPathLabel, summaryLabel, statusLabel, scrollPane);
        mainLayout.setPadding(new Insets(10));
        mainLayout.getStyleClass().add("vbox");

        Scene scene = new Scene(mainLayout, 800, 600);

        // Apply your main stylesheet here (adjust the path as needed)
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/styles/Styles.css")).toExternalForm());

        // Folder selection at startup
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Select Folder to Analyze");
        File initialDirectory = new File(System.getProperty("user.home"));
        if (initialDirectory.exists()) {
            directoryChooser.setInitialDirectory(initialDirectory);
        }

        File selectedDir = directoryChooser.showDialog(primaryStage);
        if (selectedDir != null) {
            selectedFolder = selectedDir.toPath();
            loadFolderData(selectedFolder);
        }

        primaryStage.setTitle("Folder Scanner");
        primaryStage.setScene(scene);
        primaryStage.show();

        // Clean up thread pools on close
        primaryStage.setOnCloseRequest(e -> {
            cancelScan = true;
            folderScanExecutor.shutdownNow();
            forkJoinPool.shutdownNow();
        });
    }


    private static void loadFolderData(Path folder) {
        if (folder == null) return;

        // Reset state
        cancelScan = false;
        pendingTasks.set(0);
        cancelButton.setDisable(false);
        progressBar.setProgress(ProgressIndicator.INDETERMINATE_PROGRESS);
        currentPathLabel.setText("Current path: " + folder.toAbsolutePath());
        summaryLabel.setText("Loading...");
        statusLabel.setText("Scanning directory contents...");
        currentFolderInfos.clear();

        // Update button states
        backButton.setDisable(folderHistory.isEmpty());
        upButton.setDisable(folder.getParent() == null);

        // Clear the file list
        Platform.runLater(() -> fileListVBox.getChildren().clear());

        // First, get the list of files and directories in the current folder
        CompletableFuture.supplyAsync(() -> {
                    try (Stream<Path> paths = Files.list(folder)) {
                        return paths.collect(Collectors.toList());
                    } catch (IOException e) {
                        throw new CompletionException(e);
                    }
                }, folderScanExecutor)
                .thenApply(paths -> {
                    // Create initial FolderInfo objects with placeholder sizes for directories
                    List<FolderInfo> initialInfos = new ArrayList<>();
                    for (Path path : paths) {
                        if (cancelScan) break;
                        try {
                            if (Files.isDirectory(path)) {
                                initialInfos.add(new FolderInfo(path, 0, 0, true));
                            } else {
                                long size = Files.size(path);
                                initialInfos.add(new FolderInfo(path, size, 1, false));
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                            initialInfos.add(new FolderInfo(path, 0, 0, Files.isDirectory(path)));
                        }
                    }
                    return initialInfos;
                })
                .thenAccept(initialInfos -> {
                    // Store the initial folder infos
                    currentFolderInfos.addAll(initialInfos);

                    // Display the initial list with placeholders for directory sizes
                    Platform.runLater(() -> {
                        updateFileList();
                        updateSummary(initialInfos);
                        statusLabel.setText("Calculating directory sizes...");
                    });

                    // Now calculate directory sizes in parallel
                    List<CompletableFuture<FolderInfo>> futures = new ArrayList<>();

                    for (FolderInfo info : initialInfos) {
                        if (cancelScan) break;

                        if (info.isDirectory()) {
                            pendingTasks.incrementAndGet();
                            CompletableFuture<FolderInfo> future = CompletableFuture.supplyAsync(() -> {
                                try {
                                    DirectorySizeInfo sizeInfo = calculateDirectorySize(info.path());
                                    return new FolderInfo(info.path(), sizeInfo.size(), sizeInfo.fileCount(), true);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                    return new FolderInfo(info.path(), 0, 0, true);
                                } finally {
                                    int remaining = pendingTasks.decrementAndGet();
                                    Platform.runLater(() -> {
                                        if (remaining == 0) {
                                            statusLabel.setText("Directory scan complete");
                                        } else {
                                            statusLabel.setText("Calculating sizes... " + remaining + " directories remaining");
                                        }
                                    });
                                }
                            }, folderScanExecutor);

                            // Update the UI as each directory size calculation completes
                            future.thenAccept(updatedInfo -> {
                                if (!cancelScan) {
                                    Platform.runLater(() -> {
                                        // Update the folder info in our list
                                        for (int i = 0; i < currentFolderInfos.size(); i++) {
                                            FolderInfo info2 = currentFolderInfos.get(i);
                                            if (info2.path().equals(updatedInfo.path())) {
                                                currentFolderInfos.set(i, updatedInfo);
                                                break;
                                            }
                                        }

                                        // Update the file list with the new data
                                        updateFileList();

                                        // Recalculate summary
                                        updateSummary(currentFolderInfos);
                                    });
                                }
                            });

                            futures.add(future);
                        }
                    }

                    // When all directory size calculations are complete
                    CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                            .thenRun(() -> {
                                if (!cancelScan) {
                                    Platform.runLater(() -> {
                                        progressBar.setProgress(1);
                                        cancelButton.setDisable(true);
                                        statusLabel.setText("Scan complete - using " + PROCESSOR_COUNT + " processor cores");
                                    });
                                }
                            })
                            .exceptionally(ex -> {
                                Main.getErrorLogger().handleException("Error during scan", ex);
                                Platform.runLater(() -> {
                                    statusLabel.setText("Error during scan: " + ex.getMessage());
                                });
                                return null;
                            });
                })
                .exceptionally(ex -> {
                    Main.getErrorLogger().handleException("Error loading folder", ex);
                    Platform.runLater(() -> {
                        Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                        FXAlertStyler.style(errorAlert);
                        errorAlert.setTitle("Error");
                        errorAlert.setHeaderText("Error accessing folder");
                        errorAlert.setContentText("Could not access: " + folder + "\nError: " + ex.getMessage());
                        errorAlert.showAndWait();
                        summaryLabel.setText("Error loading folder");
                        statusLabel.setText("Scan failed");
                        progressBar.setProgress(0);
                        cancelButton.setDisable(true);
                    });
                    return null;
                });
    }

    private static void updateFileList() {
        fileListVBox.getChildren().clear();

        // Create a copy of the list for sorting
        List<FolderInfo> sortedInfos = new ArrayList<>(currentFolderInfos);

        // Sort by size if requested
        if (sortBySize) {
            sortedInfos.sort((a, b) -> Long.compare(b.sizeBytes(), a.sizeBytes()));
        } else {
            // Default sort: directories first, then alphabetical
            sortedInfos.sort((a, b) -> {
                if (a.isDirectory() && !b.isDirectory()) return -1;
                if (!a.isDirectory() && b.isDirectory()) return 1;
                return a.path().getFileName().toString().compareToIgnoreCase(b.path().getFileName().toString());
            });
        }

        // Add entries to the UI
        for (FolderInfo info : sortedInfos) {
            fileListVBox.getChildren().add(createFolderEntry(info));
        }
    }

    private static long parseApproximateSize(String sizeText) {
        // This is a very rough approximation for UI updates only
        try {
            String[] parts = sizeText.split(" ");
            double value = Double.parseDouble(parts[0]);
            String unit = parts[1];

            return switch (unit) {
                case "B" -> (long) value;
                case "KB" -> (long) (value * 1024);
                case "MB" -> (long) (value * 1024 * 1024);
                case "GB" -> (long) (value * 1024 * 1024 * 1024);
                case "TB" -> (long) (value * 1024 * 1024 * 1024 * 1024);
                default -> 0;
            };
        } catch (Exception e) {
            return 0;
        }
    }

    private static DirectorySizeInfo calculateDirectorySize(Path directory) throws IOException {
        if (cancelScan) return new DirectorySizeInfo(0, 0);

        // Use a parallel stream to process files in parallel
        try {
            return forkJoinPool.submit(() -> {
                final AtomicLong size = new AtomicLong(0);
                final AtomicInteger fileCount = new AtomicInteger(0);

                try {
                    Files.walkFileTree(directory, new SimpleFileVisitor<Path>() {
                        @Override
                        @NonNull
                        public FileVisitResult visitFile(@NonNull Path file, @NonNull BasicFileAttributes attrs) {
                            if (cancelScan) return FileVisitResult.TERMINATE;
                            size.addAndGet(attrs.size());
                            fileCount.incrementAndGet();
                            return FileVisitResult.CONTINUE;
                        }

                        @NonNull
                        @Override
                        public FileVisitResult visitFileFailed(@NonNull Path file, @NonNull IOException exc) {
                            // Skip files that can't be accessed
                            return FileVisitResult.CONTINUE;
                        }

                        @NonNull
                        @Override
                        public FileVisitResult preVisitDirectory(@NonNull Path dir, @NonNull BasicFileAttributes attrs) {
                            if (cancelScan) return FileVisitResult.TERMINATE;
                            return FileVisitResult.CONTINUE;
                        }
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                }

                return new DirectorySizeInfo(size.get(), fileCount.get());
            }).get();
        } catch (InterruptedException | ExecutionException e) {
            throw new IOException("Error calculating directory size", e);
        }
    }

    private static Node createFolderEntry(FolderInfo info) {
        HBox hBox = new HBox(10);
        hBox.setAlignment(Pos.CENTER_LEFT);

        Label nameLabel = new Label(info.path().getFileName().toString());
        nameLabel.getStyleClass().add("file-name-label");  // optional, for text styling

        String sizeText = info.isDirectory() && info.sizeBytes() == 0 ? "Calculating..." : humanReadableByteCount(info.sizeBytes());
        String fileCountText = info.isDirectory() && info.fileCount() == 0 ? "Calculating..." : info.fileCount() + " files";

        Label sizeLabel = new Label(sizeText);
        sizeLabel.getStyleClass().add("size-label");  // optional

        Label fileCountLabel = new Label(fileCountText);
        fileCountLabel.getStyleClass().add("count-label");  // optional

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button deleteButton = new Button("Delete");
        deleteButton.getStyleClass().addAll("button", "button-cancel");
        deleteButton.setOnAction(e -> {
            Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
            FXAlertStyler.style(confirmAlert);
            confirmAlert.setTitle("Confirm Delete");
            confirmAlert.setHeaderText("Delete " + info.path().getFileName());
            confirmAlert.setContentText("Are you sure you want to delete this " +
                    (info.isDirectory() ? "folder" : "file") + "?");

            Optional<ButtonType> result = confirmAlert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                deleteItem(info.path());
            }
        });

        hBox.getChildren().addAll(nameLabel, spacer, sizeLabel, fileCountLabel);

        if (info.isDirectory()) {
            Button openButton = new Button("Open");
            openButton.getStyleClass().add("button");
            openButton.setOnAction(e -> openFolder(info.path()));
            hBox.getChildren().add(openButton);

            hBox.getStyleClass().add("folder-entry");

            // Double-click to open folder
            hBox.setOnMouseClicked(e -> {
                if (e.getClickCount() == 2 && !(e.getTarget() instanceof Button)) {
                    openFolder(info.path());
                }
            });
        } else {
            hBox.getStyleClass().add("file-entry");
        }

        hBox.getChildren().add(deleteButton);

        // Optionally, hover effect with CSS; no inline styles here

        return hBox;
    }

    private static void openFolder(Path folderPath) {
        folderHistory.push(selectedFolder);
        selectedFolder = folderPath;
        loadFolderData(selectedFolder);
    }

    private static void deleteItem(Path path) {
        try {
            if (Files.isDirectory(path)) {
                // Delete directory and all contents
                Files.walk(path)
                        .sorted(Comparator.reverseOrder())
                        .map(Path::toFile)
                        .forEach(File::delete);
            } else {
                // Delete single file
                Files.delete(path);
            }
            // Refresh the current view
            loadFolderData(selectedFolder);
        } catch (IOException e) {
            Alert errorAlert = new Alert(Alert.AlertType.ERROR);
            FXAlertStyler.style(errorAlert);
            errorAlert.setTitle("Delete Error");
            errorAlert.setHeaderText("Could not delete " + path.getFileName());
            errorAlert.setContentText("Error: " + e.getMessage());
            errorAlert.showAndWait();
            e.printStackTrace();
        }
    }

    private static void updateSummary(List<FolderInfo> folderInfos) {
        long totalSizeBytes = folderInfos.stream().mapToLong(FolderInfo::sizeBytes).sum();
        int totalFiles = folderInfos.stream().mapToInt(FolderInfo::fileCount).sum();

        summaryLabel.setText("Total Size: " + humanReadableByteCount(totalSizeBytes) +
                " | Total Files: " + totalFiles +
                " | Items: " + folderInfos.size());
    }

    private static void cancelScan() {
        cancelScan = true;
        statusLabel.setText("Cancelling scan...");
        // We don't shut down the executors here, just mark the scan as cancelled
    }

    private static void openNewFolder(Stage stage) {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Select New Folder to Open");

        // Set initial directory to current folder if possible
        if (selectedFolder != null && Files.exists(selectedFolder)) {
            directoryChooser.setInitialDirectory(selectedFolder.toFile());
        }

        File selectedDir = directoryChooser.showDialog(stage);
        if (selectedDir != null) {
            folderHistory.push(selectedFolder); // Save current location to history
            selectedFolder = selectedDir.toPath();
            loadFolderData(selectedFolder);
        }
    }

    private static void goBackFolder() {
        if (!folderHistory.isEmpty()) {
            Path previousFolder = folderHistory.pop();
            selectedFolder = previousFolder;
            loadFolderData(selectedFolder);
        }
    }

    private static void goUpFolder() {
        if (selectedFolder != null && selectedFolder.getParent() != null) {
            folderHistory.push(selectedFolder); // Save current location to history
            selectedFolder = selectedFolder.getParent();
            loadFolderData(selectedFolder);
        }
    }

    // FolderInfo record to hold data
    private record FolderInfo(Path path, long sizeBytes, int fileCount, boolean isDirectory) {
    }

    // Helper record for directory size calculation
    private record DirectorySizeInfo(long size, int fileCount) {
    }
}