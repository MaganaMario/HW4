package application;

import javafx.scene.Scene;

import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import java.util.ArrayList;

import database.DatabaseHelper;

/**
 * The WelcomeLoginPage class displays a welcome screen for authenticated users.
 * It allows users to navigate to their respective pages based on their role or quit the application.
 */
public class WelcomeLoginPage {
	
	private final DatabaseHelper databaseHelper;
	private int userId;

    public WelcomeLoginPage(DatabaseHelper databaseHelper, int userId) {
        this.databaseHelper = databaseHelper;
        this.userId = userId;
    }
    public void show( Stage primaryStage, User user) {
    	
    	VBox layout = new VBox(5);
	    layout.setStyle("-fx-alignment: center; -fx-padding: 20;");
	    
	    Label welcomeLabel = new Label("Welcome!!");
	    welcomeLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
	    
	    Label roleQuestion = new Label("What role would you like to play in this session?");
	    roleQuestion.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
	    
	    ArrayList<String> role = user.getRole();
    	System.out.println(role);
	    
	    // Button to logout of account
	    Button logoutButton = new Button("Log out");
	    logoutButton.setOnAction(a -> {
	    	new SetupLoginSelectionPage(databaseHelper).show(primaryStage);
	    });
	    
	    // Buttons to navigate to the user's respective page based on their role
	    HBox roleButtons = new HBox(10); // Horizontal container with 10 pixel spacing
	    roleButtons.setStyle("-fx-alignment: center");
	    
	    // Add buttons to HBox
    	for (int i = 0; i < role.size(); i++) {
    		String userRole = role.get(i);
    		Button roleButton = new Button(userRole);
    		roleButton.setOnAction(a -> {
    	    	
    	    	if(userRole.equals("admin")) {
    	    		new AdminHomePage(databaseHelper).show(primaryStage);
    	    	}
    	    	else if(userRole.equals("student")) {
    	    		new StudentHomePage(databaseHelper, userId).show(primaryStage);
    	    	}
    	    	else if(userRole.equals("instructor")) {
    	    		new InstructorHomePage(databaseHelper, userId).show(primaryStage);
    	    	}
    	    	else if(userRole.equals("staff")) {
    	    		new StaffHomePage(databaseHelper, userId).show(primaryStage);
    	    	}
    	    	else if(userRole.equals("reviewer")) {
    	    		new ReviewerHomePage(databaseHelper, userId).show(primaryStage);
    	    	}
    	    });
    		roleButtons.getChildren().add(roleButton);
    	}


	    layout.getChildren().addAll(welcomeLabel,roleQuestion,roleButtons);
	    
	    // "Invite" button for admin to generate invitation codes
	    if (user.getRole().contains("admin")) {
	    	Button inviteButton = new Button("Invite");
            inviteButton.setOnAction(a -> {
                new InvitationPage().show(databaseHelper, primaryStage);
            });
            layout.getChildren().add(inviteButton);
        }
	    
	    layout.getChildren().add(logoutButton);
	    Scene welcomeScene = new Scene(layout, 800, 400);

	    // Set the scene to primary stage
	    primaryStage.setScene(welcomeScene);
	    primaryStage.setTitle("Welcome Page");
    }
}