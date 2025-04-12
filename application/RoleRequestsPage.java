package application;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import database.DatabaseHelper;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class RoleRequestsPage {
	
	private DatabaseHelper databaseHelper;
	
	private List<Map<String, Object>> roleRequests;
	
	private ListView<Map<String, Object>> requestList;
	private HBox actionBar;
	
	private String sort = "ORDER BY id ASC";
	private List<String> filter = Arrays.asList("reviewer"); // All roles that can be requested
	
	private Scene previousScene;
	private String previousTitle;
	
	public RoleRequestsPage(DatabaseHelper databaseHelper) {
		this.databaseHelper = databaseHelper;
	}
	
	public void show(Stage primaryStage) {
		previousScene = primaryStage.getScene();
		previousTitle = primaryStage.getTitle();
		
    	VBox layout = new VBox();
	    layout.setStyle("-fx-alignment: top-center; -fx-padding: 20;");
	    layout.setSpacing(10);
	    
	    // Search Box
	    HBox searchBox = createSearchBox(layout);
	    layout.getChildren().add(searchBox);
	    
	    // Request list
	    createRequestListView(primaryStage);
	    layout.getChildren().add(requestList);
	    searchRequests(); // Run to get list
	    
	    // Action Bar
	    createActionBar(primaryStage, layout);
	    layout.getChildren().add(actionBar);
	    
	    // Set list expandable
	    VBox.setVgrow(requestList, Priority.ALWAYS);
	    
	    // Set the scene to primary stage
	    Scene questionScene = new Scene(layout, 800, 400);
	    primaryStage.setScene(questionScene);
	    primaryStage.setTitle("Role Request Page");
    	
    }
	
	// Create search box
	public HBox createSearchBox(VBox parent) {
		HBox searchBox = new HBox(5);
		searchBox.maxWidthProperty().bind(parent.widthProperty().subtract(parent.getPadding().getLeft() + parent.getPadding().getRight()));
		
		// Expand search bar to take up extra room
		//HBox.setHgrow(searchBar, Priority.ALWAYS);
		
		Region spacer = new Region();
		HBox.setHgrow(spacer, Priority.ALWAYS);
		
		// Create search filter
		ComboBox<String> sortFilter = createSortFilter();
		
		// Create status filter
		ComboBox<String> statusFilter = createStatusFilter();
		
		// Add items
		searchBox.getChildren().addAll(spacer, sortFilter, statusFilter);
		
		return searchBox;
	}
	
	// Create search filter
	public ComboBox<String> createSortFilter() {
		ComboBox<String> sortFilter = new ComboBox<>();
		
		// Create options and set default
		sortFilter.getItems().addAll("Most Recent", "Oldest");
		sortFilter.setValue("Oldest");
		
		// Event handler
		sortFilter.setOnAction(a -> {
			setSort(sortFilter.getValue());
		});
		
		return sortFilter;
	}
	
	// Create search filter
	public ComboBox<String> createStatusFilter() {
		ComboBox<String> statusFilter = new ComboBox<>();
		
		// Create options and set default
		statusFilter.getItems().addAll("All", "Reviewer");
		statusFilter.setValue("All");
		
		// Event handler
		statusFilter.setOnAction(a -> {
			setFilter(statusFilter.getValue());
		});
		
		return statusFilter;
	}
	
	// Set sort and search
	public void setSort(String sortType) {
		switch (sortType) {
		case "Most Recent":
			sort = "ORDER BY id DESC";
			break;
		case "Oldest":
			sort = "ORDER BY id ASC";
			break;
		default:
			sort = "ORDER BY id DESC";
			break;
		}
		
		searchRequests();
	}
	
	// Set filter and search
	public void setFilter(String filterType) {
		switch (filterType) {
		case "All":
			filter = Arrays.asList("reviewer");
			break;
		case "Reviewer":
			filter = Arrays.asList("reviewer");
			break;
		default:
			filter = Arrays.asList("reviewer");
			break;
		}
		
		searchRequests();
	}
	
	// Find requests
	public void searchRequests() {
		roleRequests = databaseHelper.getRoleRequests(filter, sort);
		loadRequests();
	}
	
	// Create the list view for all requests
	public void createRequestListView(Stage primaryStage) {
		requestList = new ListView<>();
		requestList.setFocusTraversable(true);
		setRequestListCellFactory();
		
		// Set placeholder for event there are no requests
		Label noResults = new Label("No requests");
		noResults.setStyle("-fx-alignment: center; -fx-font-size: 20px; -fx-text-fill: gray;");
		requestList.setPlaceholder(noResults);
		
		// Create event handler for when request is double clicked
		requestList.setOnMouseClicked(event -> {
			if (event.getClickCount() == 2) { // Double click detected
				Map<String, Object> selectedRequest = requestList.getSelectionModel().getSelectedItem();
				if (selectedRequest != null) {
					loadSelectedReview(selectedRequest, primaryStage);
				}
			}
		});
		
		// Create event handler for when enter key is pressed
		requestList.setOnKeyPressed(event -> {
			if (event.getCode().toString().equals("ENTER")) { // Enter key detected
				Map<String, Object> selectedRequest = requestList.getSelectionModel().getSelectedItem();
				if (selectedRequest != null) {
					loadSelectedReview(selectedRequest, primaryStage);
				}
			}
		});
	}
	
	// Set custom cell factory format for listed requests
	public void setRequestListCellFactory() {
	    requestList.setCellFactory(parameter -> new ListCell<Map<String, Object>>() {
	        @Override
	        protected void updateItem(Map<String, Object> request, boolean empty) {
	            super.updateItem(request, empty);
	            if (empty || request == null) {
	                setGraphic(null);
	            } else {
	                // Create layout for each role request
	                VBox layout = new VBox();
	                layout.setStyle("-fx-alignment: top-left; -fx-padding: 5;");

	                // User Label
	                UserLightweightDTO user = databaseHelper.getUser((int) request.get("userId"));
	                Label userIdLabel = new Label("User: " + user.getUserName());
	                userIdLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: gray;");

	                // Role Label
	                String role = (String) request.get("role");
	                Label roleLabel = new Label("Requested Role: " + role);
	                roleLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

	                layout.getChildren().addAll(userIdLabel, roleLabel);
	                setGraphic(layout);
	            }
	        }
	    });
	}

	// Load requests into request list
	public void loadRequests() {
		requestList.getItems().clear();
		requestList.getItems().addAll(roleRequests);
	}
	
	// Load selected request on select event
	public void loadSelectedReview(Map<String, Object> request, Stage primaryStage) {
		int requestId = (int) request.get("id");
		int requestingUserId = (int) request.get("userId");
		UserLightweightDTO user = databaseHelper.getUser(requestingUserId);
		String role = (String) request.get("role");
		if (role.equals("reviewer")) {
			Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
			alert.setTitle("Reviewer Role Request");
			alert.setHeaderText("User: " + user.getUserName());
			alert.setContentText("Do you want to approve, deny, or review the request?");
			
	        ButtonType approveButton = new ButtonType("Approve");
	        ButtonType denyButton = new ButtonType("Deny");
	        ButtonType reviewButton = new ButtonType("Review");
	        alert.getButtonTypes().setAll(approveButton, denyButton, reviewButton);

	        // Show the alert and wait for response
	        Optional<ButtonType> result = alert.showAndWait();
	        
	        if (result.isPresent()) {
	        	ButtonType clickedButton = result.get();
	        	
	            if (clickedButton == approveButton) {
	                // Approve
	            	System.out.println("Approving request.");
	            	databaseHelper.addUserRole(requestingUserId, role);
	            	databaseHelper.deleteRoleRequest(requestId);
	            } else if (clickedButton == denyButton) {
	                // Deny
	            	System.out.println("Denying request.");
	            	databaseHelper.deleteRoleRequest(requestId);
	            } else if (clickedButton == reviewButton) {
	                // Show the users review page
	                new MyReviewsPage(databaseHelper, requestingUserId, false).show(primaryStage);
	            }
	        }
		}
		
		// Update list
		searchRequests();
	}
	
	// Create action bar
	public void createActionBar(Stage primaryStage, VBox parent) {
		actionBar = new HBox(10);
		actionBar.setStyle("-fx-alignment: center-left;");
		actionBar.maxWidthProperty().bind(parent.widthProperty().subtract(parent.getPadding().getLeft() + parent.getPadding().getRight()));		
		
		// Add a spacer that will push the following buttons to the right
		Region spacer = new Region();
		HBox.setHgrow(spacer, Priority.ALWAYS); // Forces elements to the right
		
		Button backButton = new Button("Back to home");
		backButton.setOnAction(a -> {
			primaryStage.setScene(previousScene);
			primaryStage.setTitle(previousTitle);
		});
		
		actionBar.getChildren().addAll(spacer, backButton);
	}
}
