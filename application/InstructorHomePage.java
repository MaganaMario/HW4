package application;

import database.DatabaseHelper;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * This page displays a simple welcome message for the instructor.
 */

public class InstructorHomePage {
	
	private DatabaseHelper databaseHelper;
	private int userId;
	
	public InstructorHomePage(DatabaseHelper databaseHelper, int userId) {
		this.databaseHelper = databaseHelper;
		this.userId = userId;
	}

    public void show(Stage primaryStage) {
    	VBox layout = new VBox(5);
	    layout.setStyle("-fx-alignment: center; -fx-padding: 20;");
	    
	    // Label to display Hello user
	    Label instructorLabel = new Label("Hello, Instructor!");
	    instructorLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
	    
	    // Button to go to question search page
	    Button questionButton = new Button("Questions");
	    questionButton.setOnAction(a -> {
	        new QuestionSearchPage(databaseHelper, userId).show(primaryStage);
	    });
	    
	    // Button to go to role requests page
	    Button roleRequestsButton = new Button("Role Requests");
	    roleRequestsButton.setOnAction(a -> {
	    	new RoleRequestsPage(databaseHelper).show(primaryStage);
	    });
	    
	    // Button to logout of account
	    Button logoutButton = new Button("Log out");
	    logoutButton.setOnAction(a -> {
	    	new SetupLoginSelectionPage(databaseHelper).show(primaryStage);
	    });

	    layout.getChildren().addAll(instructorLabel, questionButton, roleRequestsButton, logoutButton);
	    Scene instructorScene = new Scene(layout, 800, 400);

	    // Set the scene to primary stage
	    primaryStage.setScene(instructorScene);
	    primaryStage.setTitle("Instructor Page");
    	
    }
}