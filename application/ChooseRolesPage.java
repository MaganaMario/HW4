package application;

import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
//import javafx.scene.layout.TilePane;
// import TilePane class to organize role checklist
import javafx.stage.Stage;

import java.sql.SQLException;

//import EventHandler from javafx event class to enable
import javafx.event.ActionEvent;
import javafx.event.EventHandler;

import java.util.ArrayList;

import database.*;


public class ChooseRolesPage {
	
	private final DatabaseHelper databaseHelper;
    // DatabaseHelper to handle database operations.
    
    public ChooseRolesPage(DatabaseHelper databaseHelper) {
        this.databaseHelper = databaseHelper;
    }

	
	public void show(Stage primaryStage, String username, String password, String fullName, String email) {
		VBox layout = new VBox();
		layout.setStyle("-fx-padding: 20; -fx-alignment: center;");
		
		Label roleQuestion = new Label("What type of user are you? Pick one or more roles from the list below!");
		layout.getChildren().add(roleQuestion);
		
		String roles[] = {"student", "instructor", "staff", "reviewer"};
		ArrayList<String> usersRoles = new ArrayList<String>();
		
		
		for (int i = 0; i < roles.length; i++) {
			CheckBox c = new CheckBox(roles[i]);
			layout.getChildren().add(c);
			c.setIndeterminate(true);
			
			EventHandler<ActionEvent> event = new EventHandler<ActionEvent>() { 
				  
	            public void handle(ActionEvent e) 
	            { 
	                if (c.isSelected()) 
	                    usersRoles.add(c.getText()); 
	                else
	                	usersRoles.remove(c.getText()); 
	            } 

	        };
	        
	        c.setOnAction(event);
	        
	        
		}
		
		Button confirmButton = new Button("Confirm your role choice");
		
		
		layout.getChildren().add(confirmButton);
		
		confirmButton.setOnAction(a -> {
			try {
				if (!usersRoles.isEmpty()) {
					User user = new User(username, password, fullName, email, usersRoles, false);
					int userId = databaseHelper.register(user);
					
					new WelcomeLoginPage(databaseHelper, userId).show(primaryStage,user);
				} else {
					Label errorLabel = new Label("Please select one or more roles");
					errorLabel.setStyle("-fx-text-fill: red; -fx-font-size: 12px;");
					layout.getChildren().add(errorLabel);
				}
			} catch (SQLException e) {
                System.err.println("Database error: " + e.getMessage());
                e.printStackTrace();
            }
		});
		
		
		
		primaryStage.setScene(new Scene(layout, 800, 400));
        primaryStage.setTitle("Role Choice Page");
        primaryStage.show();
	}
	
}