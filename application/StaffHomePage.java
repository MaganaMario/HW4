package application;

import database.DatabaseHelper;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * Represents the home page for a staff member.
 * <p>
 * This page provides navigation options for staff to:
 * <ul>
 *     <li>Search and review questions</li>
 *     <li>Log out of their account</li>
 * </ul>
 * It is designed to facilitate content review and assessment by staff users.
 */
public class StaffHomePage {

    /** Database helper to interact with the application's database. */
    private DatabaseHelper databaseHelper;

    /** ID of the currently logged-in staff member. */
    private int userId;

    /**
     * Constructs a StaffHomePage instance with the provided database helper and user ID.
     *
     * @param databaseHelper the DatabaseHelper used for database operations
     * @param userId the unique identifier of the currently logged-in staff member
     */
    public StaffHomePage(DatabaseHelper databaseHelper, int userId) {
        this.databaseHelper = databaseHelper;
        this.userId = userId;
    }

    /**
     * Displays the staff home page on the given stage.
     * <p>
     * This includes:
     * <ul>
     *     <li>A welcome label</li>
     *     <li>A button to navigate to the question search page</li>
     *     <li>A logout button</li>
     * </ul>
     *
     * @param primaryStage the main application window (stage) to display the scene on
     */
    public void show(Stage primaryStage) {
        VBox layout = new VBox(5);
        layout.setStyle("-fx-alignment: center; -fx-padding: 20;");

        // Label to greet the staff member
        Label staffLabel = new Label("Welcome, Staff Member!");
        staffLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        // Button to go to question search page
        Button questionButton = new Button("Questions");
        questionButton.setOnAction(a -> {
            new QuestionSearchPage(databaseHelper, userId).show(primaryStage);
        });

        // Button to log out
        Button logoutButton = new Button("Log out");
        logoutButton.setOnAction(e -> {
            new SetupLoginSelectionPage(databaseHelper).show(primaryStage);
        });

        // Add UI elements to layout
        layout.getChildren().addAll(staffLabel, questionButton, logoutButton);

        // Create and set the scene
        Scene scene = new Scene(layout, 800, 400);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Staff Home Page");
    }
}
