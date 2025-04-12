package application;

import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.List;

import database.DatabaseHelper;

/**
 * AdminPage class represents the user interface for the admin user.
 * This page displays a simple welcome message for the admin.
 */

public class AdminHomePage {
	/**
     * Displays the admin page in the provided primary stage.
     * @param primaryStage The primary stage where the scene will be displayed.
     */
	
	// Incorporates DatabaseHelper onto AdminHomePage file

	private final DatabaseHelper databaseHelper;

    public AdminHomePage(DatabaseHelper databaseHelper) {
        this.databaseHelper = databaseHelper;
    }
    
    public void show(Stage primaryStage) {
    	VBox layout = new VBox();
    	
	    layout.setStyle("-fx-alignment: center; -fx-padding: 20;");
	    
	    // label to display the welcome message for the admin
	    Label adminLabel = new Label("Hello, Admin!");
	    
	    adminLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
	    
	    // Button to View Existing User Information
	    Button viewUsersButton = new Button("View Existing User Information");
	    viewUsersButton.setOnAction(a -> viewUsers());    

	    

	    layout.getChildren().addAll(adminLabel, viewUsersButton);

	    Scene adminScene = new Scene(layout, 800, 400);

	    // Set the scene to primary stage
	    primaryStage.setScene(adminScene);
	    primaryStage.setTitle("Admin Page");
    }
    

 // Displays a page with a list of existing users' usernames, email, and role



 private void viewUsers() {
     Stage userStage = new Stage();
     VBox layout = new VBox(10);
     layout.setStyle("-fx-padding: 20;");

     List<User> users = databaseHelper.getAllUsersList(); // Fetch users from database
     for (User user : users) {
         String userInfo = "Username: " + user.getUserName() +
        		 		   ", Name: " + user.getFullName() +
                           ", Email: " + user.getEmail() +
                           ", Role(s): " + user.getRoleString();
         layout.getChildren().add(new Label(userInfo));
     }

     Scene scene = new Scene(layout, 600, 300);

     userStage.setScene(scene);
     userStage.setTitle("Existing Users");
     userStage.show();
}
 
 
}