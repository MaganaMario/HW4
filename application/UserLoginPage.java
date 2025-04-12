package application;

import javafx.scene.Scene;

import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.sql.SQLException;
import java.util.ArrayList;

import database.DatabaseHelper;

/**
 * The UserLoginPage class provides a login interface for users to access their accounts.
 * It validates the user's credentials and navigates to the appropriate page upon successful login.
 */
public class UserLoginPage {
	
    private final DatabaseHelper databaseHelper;

    public UserLoginPage(DatabaseHelper databaseHelper) {
        this.databaseHelper = databaseHelper;
    }

    public void show(Stage primaryStage) {
    	// Input field for the user's userName, password
        TextField userNameField = new TextField();
        userNameField.setPromptText("Enter userName");
        userNameField.setMaxWidth(250);

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Enter Password");
        passwordField.setMaxWidth(250);
        
        // Label to display error messages
        Label errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: red; -fx-font-size: 12px;");


        Button loginButton = new Button("Login");
        
        loginButton.setOnAction(a -> {
        	// Retrieve user inputs
            String userName = userNameField.getText();
            String password = passwordField.getText();
            try {
            	ArrayList<String> emptyRole = new ArrayList<String>();
            	String emptyName = "{Full Name}";
            	String emptyEmail = "{Email}";
            	User user=new User(userName, password, emptyName, emptyEmail, emptyRole, false);
            	
            	// Retrieve the user's role from the database using userName
            	ArrayList<String> roles = (ArrayList<String>) databaseHelper.getUserRole(userName);
            	
            	if(roles!=null) {
            		user.setRole(roles);
            		int userId = databaseHelper.login(user);
            		if(userId != -1) {
            			// Continue to role page if user only has one role
            			if (user.getRole().size() == 1) {
            				String userRole = user.getRole().get(0);
            				
            				switch (userRole) {
            				case "admin":
            					new WelcomeLoginPage(databaseHelper, userId).show(primaryStage,user);
            					break;
            					
            				case "student":
            					new StudentHomePage(databaseHelper, userId).show(primaryStage);
            					break;
            					
            				case "instructor":
            					new InstructorHomePage(databaseHelper, userId).show(primaryStage);
            					break;
            					
            				case "staff":
            					new StaffHomePage(databaseHelper, userId).show(primaryStage);
            					break;
            					
            				case "reviewer":
            					new ReviewerHomePage(databaseHelper, userId).show(primaryStage);
            					break;
            				}
            			} else {
            				new WelcomeLoginPage(databaseHelper, userId).show(primaryStage,user);
            			}
            		}
            		else {
            			// Display an error if the login fails
                        errorLabel.setText("Error logging in");
            		}
            	}
            	else {
            		// Display an error if the account does not exist
                    errorLabel.setText("user account doesn't exist");
            	}
            	
            } catch (SQLException e) {
                System.err.println("Database error: " + e.getMessage());
                e.printStackTrace();
            } 
        });
        
        Button backButton = new Button("Back");
        backButton.setOnAction(a -> {
        	new SetupLoginSelectionPage(databaseHelper).show(primaryStage);
        });

        VBox layout = new VBox(10);
        layout.setStyle("-fx-padding: 20; -fx-alignment: center;");
        layout.getChildren().addAll(userNameField, passwordField, loginButton, backButton, errorLabel);

        primaryStage.setScene(new Scene(layout, 800, 400));
        primaryStage.setTitle("User Login");
        primaryStage.show();
    }
}
