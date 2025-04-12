package application;

import database.DatabaseHelper;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * This page displays a simple welcome message for the student.
 */

public class StudentHomePage {
	
	private DatabaseHelper databaseHelper;
	private int userId;
	
	public StudentHomePage(DatabaseHelper databaseHelper, int userId) {
		this.databaseHelper = databaseHelper;
		this.userId = userId;
	}

    public void show(Stage primaryStage) {
    	VBox layout = new VBox(5);
	    layout.setStyle("-fx-alignment: center; -fx-padding: 20;");
	    
	    // Label to display Hello user
	    Label studentLabel = new Label("Hello, Student!");
	    studentLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
	    
	    // Button to go to question search page
	    Button questionButton = new Button("Questions");
	    questionButton.setOnAction(a -> {
	        new QuestionSearchPage(databaseHelper, userId).show(primaryStage);
	    });
	    
	    // Button to logout of account
	    Button logoutButton = new Button("Log out");
	    logoutButton.setOnAction(a -> {
	    	new SetupLoginSelectionPage(databaseHelper).show(primaryStage);
	    });

	    layout.getChildren().addAll(studentLabel, questionButton, logoutButton);
	    Scene studentScene = new Scene(layout, 800, 400);

	    // Set the scene to primary stage
	    primaryStage.setScene(studentScene);
	    primaryStage.setTitle("Student Page");
    	
    }
}