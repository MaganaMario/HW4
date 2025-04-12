package application;

import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import database.*;

/**
 * SetupAccountPage class handles the account setup process for new users.
 * Users provide their userName, password, and a valid invitation code to register.
 */
public class SetupAccountPage {
	
    private final DatabaseHelper databaseHelper;
    // DatabaseHelper to handle database operations.
    
    public SetupAccountPage(DatabaseHelper databaseHelper) {
        this.databaseHelper = databaseHelper;
    }
    
    
    

    /**
     * Displays the Setup Account page in the provided stage.
     * @param primaryStage The primary stage where the scene will be displayed.
     */
    public void show(Stage primaryStage) {
    	// Input fields for userName, password, and invitation code
        TextField userNameField = new TextField();
        userNameField.setPromptText("Enter Username");
        userNameField.setMaxWidth(250);

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Enter Password");
        passwordField.setMaxWidth(250);

        TextField fullNameField = new TextField();
        fullNameField.setPromptText("Enter Full Name");
        fullNameField.setMaxWidth(250);

        TextField emailField = new TextField();
        emailField.setPromptText("Enter Email");
        emailField.setMaxWidth(250);
        
        TextField inviteCodeField = new TextField();
        inviteCodeField.setPromptText("Enter InvitationCode");
        inviteCodeField.setMaxWidth(250);
        
        // Label to display error messages for invalid input or registration issues
        Label errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: red; -fx-font-size: 12px;");
        

        Button setupButton = new Button("Setup");
        
        setupButton.setOnAction(a -> {
        	// Retrieve user input
            String userName = userNameField.getText();
            String password = passwordField.getText();
            String fullName = fullNameField.getText();
            String email = emailField.getText();
            String code = inviteCodeField.getText();
            	
        	String userNameErrMessage = UserNameRecognizer.checkForValidUserName(userName);
        	String passwordErrMessage = PasswordEvaluator.evaluatePassword(password);
        	String emailErrMessage = EmailVerifier.verifyEmail(email);
        	
        	// Check for a legitimate userName
        	if (userNameErrMessage == "") {
        		
        		// Check for a legitimate password
        		if (passwordErrMessage == "") {
        			
        			if (emailErrMessage == "") {
        				
	        			// Check if the user already exists
		            	if(!databaseHelper.doesUserExist(userName)) {
		            		
		            		// Validate the invitation code
		            		if(databaseHelper.validateInvitationCode(code)) {
		            			
		            			// Create a new user and register them in the database
				            	// User user=new User(userName, password, "user");
				                // databaseHelper.register(user);
				                
				             // Navigate to the Welcome Login Page
				                //new WelcomeLoginPage(databaseHelper).show(primaryStage,user);
				                new ChooseRolesPage(databaseHelper).show(primaryStage, userName, password, fullName, email);
		            		}
		            		else {
		            			errorLabel.setText("Please enter a valid invitation code");
		            		}
		            	}
		            	else {
		            		errorLabel.setText("This username is taken!!.. Please use another to setup an account");
		            	}
        			}
        			else {
        				errorLabel.setText(emailErrMessage);
        			}
        		}
        		else {
        			errorLabel.setText(passwordErrMessage);
        		}
        	}
        	else {
        		errorLabel.setText(userNameErrMessage);
        	}
            	
        });
        
        Button backButton = new Button("Back");
        backButton.setOnAction(a -> {
        	new SetupLoginSelectionPage(databaseHelper).show(primaryStage);
        });

        VBox layout = new VBox(10);
        layout.setStyle("-fx-padding: 20; -fx-alignment: center;");
        layout.getChildren().addAll(userNameField, passwordField, fullNameField, emailField, inviteCodeField, setupButton, backButton, errorLabel);

        primaryStage.setScene(new Scene(layout, 800, 400));
        primaryStage.setTitle("Account Setup");
        primaryStage.show();
    }
}
