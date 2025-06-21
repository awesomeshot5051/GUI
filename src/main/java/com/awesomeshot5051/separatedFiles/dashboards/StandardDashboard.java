package com.awesomeshot5051.separatedFiles.dashboards;

import com.awesomeshot5051.*;
import com.awesomeshot5051.separatedFiles.*;
import com.awesomeshot5051.separatedFiles.extraStuff.*;
import com.awesomeshot5051.separatedFiles.personalInfo.*;
import com.awesomeshot5051.separatedFiles.security.*;
import com.awesomeshot5051.separatedFiles.session.*;
import com.awesomeshot5051.separatedFiles.systemConfiguration.folderManagement.*;
import javafx.geometry.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.util.*;

public class StandardDashboard implements MainScreen.DashboardScreen {

    @Override
    public Parent getView() {
        VBox root = new VBox(15);
        root.setPadding(new Insets(20));
        root.setAlignment(Pos.CENTER);
        root.getStyleClass().add("root");

        root.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/styles/dashboard.css")).toExternalForm());

        Label welcome = new Label("Welcome, " + SessionManager.getName());
        welcome.getStyleClass().add("dashboard-header");


        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.getStyleClass().add("scroll-pane");

        GridPane buttonGrid = new GridPane();
        buttonGrid.setHgap(10);
        buttonGrid.setVgap(10);
        buttonGrid.setPadding(new Insets(5));
        buttonGrid.setAlignment(Pos.CENTER);
        buttonGrid.getStyleClass().add("grid-pane");

        ColumnConstraints column1 = new ColumnConstraints();
        column1.setPercentWidth(50);
        column1.setHalignment(HPos.CENTER);

        ColumnConstraints column2 = new ColumnConstraints();
        column2.setPercentWidth(50);
        column2.setHalignment(HPos.CENTER);

        buttonGrid.getColumnConstraints().addAll(column1, column2);

        List<Button> dashboardButtons = createDashboardButtons();

        int row = 0;
        int col = 0;
        for (Button button : dashboardButtons) {
            button.setMaxWidth(Double.MAX_VALUE);
            button.getStyleClass().add("button");
            buttonGrid.add(button, col, row);
            GridPane.setFillWidth(button, true);

            col++;
            if (col > 1) {
                col = 0;
                row++;
            }
        }

        VBox panelWrapper = new VBox(buttonGrid);
        panelWrapper.setPadding(new Insets(20));
        panelWrapper.getStyleClass().add("panel");

        scrollPane.setContent(panelWrapper);

        Button logoutButton = getLogoutButton();
        logoutButton.setMaxWidth(300);
        logoutButton.getStyleClass().add("button");

        root.getChildren().addAll(welcome, scrollPane, logoutButton);

        return root;
    }

    private List<Button> createDashboardButtons() {
        List<Button> buttons = new ArrayList<>();

        Button changeUsername = new Button("Change Username");
        changeUsername.setOnAction(e -> new infoChangeGUI().showChangeUsernameGUI());
        buttons.add(changeUsername);

        Button changePassword = new Button("Change Password");
        changePassword.setOnAction(e -> new infoChangeGUI().showChangePasswordGUI());
        buttons.add(changePassword);

        Button viewContentButton = new Button("View Content");
        viewContentButton.setOnAction(e -> System.out.println("Standard: View Content clicked."));
        buttons.add(viewContentButton);

        Button manageFilesButton = new Button("Manage Files and Folders");
        manageFilesButton.setOnAction(e -> ManageFiles.launchFolderScanner());
        buttons.add(manageFilesButton);

        Button vault = new Button("Vault");
        vault.setOnAction(e -> new VaultManagementScreen().VaultManagementMainGUI());
        buttons.add(vault);

        Button codeCrackerButton = new Button("Code Cracker");
        codeCrackerButton.setOnAction(e -> new BruteForceCodeCracker().start(Main.getStage()));
        buttons.add(codeCrackerButton);

        Button numberGameButton = new Button("Number Game");
        numberGameButton.setOnAction(e -> new NumberGame());
        buttons.add(numberGameButton);

        return buttons;
    }

    @Override
    public Button getLogoutButton() {
        Button logoutButton = new Button("Logout");
        logoutButton.setOnAction(e -> {
            if (SessionManager.isSwitchedUser()) {
                SessionManager.revertToAdmin();
            } else {
                Main.getStage().close();
                new Main().loginScreen();
            }
        });
        logoutButton.getStyleClass().add("button");
        return logoutButton;
    }
}
