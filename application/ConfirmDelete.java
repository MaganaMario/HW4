package application;

import java.util.Optional;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;

public class ConfirmDelete {
	
	// Ask the user if they are sure they want to delete, return true if they want to
	public boolean show(String message) {
		// Create an alert
		Alert alert = new Alert(AlertType.CONFIRMATION);
		alert.setTitle("Confirm Deletion");
		alert.setHeaderText(null); // remove header
		alert.setContentText(message);
		
		// Buttons
		ButtonType deleteButton = new ButtonType("Delete");
		ButtonType cancelButton = new ButtonType("Cancel");
		alert.getButtonTypes().setAll(deleteButton, cancelButton);
		
		// Wait for button press
		Optional<ButtonType> result = alert.showAndWait();
		
		return result.isPresent() && result.get() == deleteButton;
	}

}
