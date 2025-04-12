package application;

import java.util.List;

import database.DatabaseHelper;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class MessageListPage {
	
	private DatabaseHelper databaseHelper;
	private int userId;
	private String parentType;
	private int parentId;
	
	private List<Integer> commenters;
	
	private ListView<Integer> commenterList;
	private HBox actionBar;
	
	private Scene previousScene;
	private String previousTitle;
	
	public MessageListPage(DatabaseHelper databaseHelper, int userId, String parentType, int parentId) {
		this.databaseHelper = databaseHelper;
		this.userId = userId;
		this.parentType = parentType;
		this.parentId = parentId;
	}
	
	public void show(Stage primaryStage) {
		previousScene = primaryStage.getScene();
		previousTitle = primaryStage.getTitle();
		
    	VBox layout = new VBox();
	    layout.setStyle("-fx-alignment: top-center; -fx-padding: 20;");
	    layout.setSpacing(10);
	    
	    // Commenters list
	    createCommenterListView(primaryStage);
	    layout.getChildren().add(commenterList);
	    searchCommenters(); // Run to get list
	    
	    // Action Bar
	    createActionBar(primaryStage, layout);
	    layout.getChildren().add(actionBar);
	    
	    // Set list expandable
	    VBox.setVgrow(commenterList, Priority.ALWAYS);
	    
	    // Set the scene to primary stage
	    Scene questionScene = new Scene(layout, 800, 400);
	    primaryStage.setScene(questionScene);
	    primaryStage.setTitle("Messages");
    	
    }
	
	// Find commenters
	public void searchCommenters() {
		commenters = databaseHelper.getAllMessages(userId, parentType, parentId);
		loadCommenters();
	}
	
	// Create the list view for all commenters
	public void createCommenterListView(Stage primaryStage) {
		commenterList = new ListView<>();
		commenterList.setFocusTraversable(true);
		setRequestListCellFactory();
		
		// Set placeholder for event there are no commenters
		Label noResults = new Label("No commenters");
		noResults.setStyle("-fx-alignment: center; -fx-font-size: 20px; -fx-text-fill: gray;");
		commenterList.setPlaceholder(noResults);
		
		// Create event handler for when commenter is double clicked
		commenterList.setOnMouseClicked(event -> {
			if (event.getClickCount() == 2) { // Double click detected
				Integer selectedRequest = commenterList.getSelectionModel().getSelectedItem();
				if (selectedRequest != null) {
					loadSelectedReview(selectedRequest, primaryStage);
				}
			}
		});
		
		// Create event handler for when enter key is pressed
		commenterList.setOnKeyPressed(event -> {
			if (event.getCode().toString().equals("ENTER")) { // Enter key detected
				Integer selectedRequest = commenterList.getSelectionModel().getSelectedItem();
				if (selectedRequest != null) {
					loadSelectedReview(selectedRequest, primaryStage);
				}
			}
		});
	}
	
	// Set custom cell factory format for listed requests
	public void setRequestListCellFactory() {
	    commenterList.setCellFactory(parameter -> new ListCell<Integer>() {
	        @Override
	        protected void updateItem(Integer id, boolean empty) {
	            super.updateItem(id, empty);
	            if (empty) {
	                setGraphic(null);
	            } else {
	                // Create layout for each commenter
	                VBox layout = new VBox();
	                layout.setStyle("-fx-alignment: top-left; -fx-padding: 5;");

	                // User Label
	                UserLightweightDTO user = databaseHelper.getUser(id);
	                Label userLabel = new Label(user.getUserName());
	                userLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: bold;");

	                layout.getChildren().addAll(userLabel);
	                setGraphic(layout);
	            }
	        }
	    });
	}
	
	// Load commenters into commenter list
	public void loadCommenters() {
		commenterList.getItems().clear();
		commenterList.getItems().addAll(commenters);
	}
	
	// Load selected request on select event
	public void loadSelectedReview(Integer commenterId, Stage primaryStage) {
		if (commenterId == userId) {
			new PrivateMessagePage(databaseHelper, userId, commenterId, userId, parentId, parentType).show(primaryStage);
		} else {
			new PrivateMessagePage(databaseHelper, userId, userId, commenterId, parentId, parentType).show(primaryStage);
		}
	}
	
	// Create action bar
	public void createActionBar(Stage primaryStage, VBox parent) {
		actionBar = new HBox(10);
		actionBar.setStyle("-fx-alignment: center-left;");
		actionBar.maxWidthProperty().bind(parent.widthProperty().subtract(parent.getPadding().getLeft() + parent.getPadding().getRight()));		
		
		// Add a spacer that will push the following buttons to the right
		Region spacer = new Region();
		HBox.setHgrow(spacer, Priority.ALWAYS); // Forces elements to the right
		
		Button backButton = new Button("Back");
		backButton.setOnAction(a -> {
			primaryStage.setScene(previousScene);
			primaryStage.setTitle(previousTitle);
		});
		
		actionBar.getChildren().addAll(spacer, backButton);
	}
}
