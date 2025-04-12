package application;

import java.util.List;

import database.DatabaseHelper;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class MyReviewsPage {

	private DatabaseHelper databaseHelper;
	private int userId;
	private boolean canEdit; // This is for when someone reviews a role request, as they should not be able to edit
	
	private List<ReviewLightweightDTO> myReviews;
	
	private TextField searchBar;
	private ListView<ReviewLightweightDTO> reviewList;
	private HBox actionBar;
	
	private String sort = "ORDER BY id DESC";
	private String filter = "";
	
	private Scene previousScene;
	private String previousTitle;
	
	public MyReviewsPage(DatabaseHelper databaseHelper, int userId, boolean canEdit) {
		this.databaseHelper = databaseHelper;
		this.userId = userId;
		this.canEdit = canEdit;
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
	    
	    // Review list
	    createReviewListView(primaryStage);
	    layout.getChildren().add(reviewList);
	    searchReviews(); // Run to get list
	    
	    // Action Bar
	    createActionBar(primaryStage, layout);
	    layout.getChildren().add(actionBar);
	    
	    // Set list expandable
	    VBox.setVgrow(reviewList, Priority.ALWAYS);
	    
	    // Set the scene to primary stage
	    Scene questionScene = new Scene(layout, 800, 400);
	    primaryStage.setScene(questionScene);
	    primaryStage.setTitle("My Reviews Page");
    	
    }
	
	// Create search box
	public HBox createSearchBox(VBox parent) {
		HBox searchBox = new HBox(5);
		searchBox.maxWidthProperty().bind(parent.widthProperty().subtract(parent.getPadding().getLeft() + parent.getPadding().getRight()));
		
		// Create search bar
		TextField searchBar = createSearchBar();
		
		// Expand search bar to take up extra room
		HBox.setHgrow(searchBar, Priority.ALWAYS);
		
		// Create search filter
		ComboBox<String> sortFilter = createSortFilter();
		
		// Create status filter
		ComboBox<String> statusFilter = createStatusFilter();
		
		// Add items
		searchBox.getChildren().addAll(searchBar, sortFilter, statusFilter);
		
		return searchBox;
	}
	
	// Create search bar
	public TextField createSearchBar() {
		searchBar = new TextField();
		searchBar.setPromptText("Search...");
		searchBar.setOnKeyReleased(a -> searchReviews());
		
		return searchBar;
	}
	
	// Create search filter
	public ComboBox<String> createSortFilter() {
		ComboBox<String> sortFilter = new ComboBox<>();
		
		// Create options and set default
		sortFilter.getItems().addAll("Most Recent", "Oldest", "A-Z", "Z-A");
		sortFilter.setValue("Most Recent");
		
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
		statusFilter.getItems().addAll("All");
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
		case "A-Z":
			sort = "ORDER BY content ASC";
			break;
		case "Z-A":
			sort = "ORDER BY content DESC";
			break;
		default:
			filter = "ORDER BY id DESC";
			break;
		}
		
		searchReviews();
	}
	
	// Set filter and search
	public void setFilter(String filterType) {
		switch (filterType) {
		case "All":
			filter = "";
			break;
		default:
			filter = "";
			break;
		}
		
		searchReviews();
	}
	
	// Find questions based on search bar input
	public void searchReviews() {
		String searchText = searchBar.getText().trim();
		
		if (searchBar.getText().trim().isEmpty()) { // Show all reviews
			if (filter != "") {
				myReviews = databaseHelper.getAllReviews(userId, filter + " " + sort);
			} else {
				myReviews = databaseHelper.getAllReviews(userId, sort);
			}
		} else {
			// Search using keywords
			if (filter != "") {
				myReviews = DatabaseHelper.searchReviews(userId, searchText, filter, sort);
			} else {
				myReviews = DatabaseHelper.searchReviews(userId, searchText, filter, sort); 
			}
	      }  
	
		loadReviews();
	}
	
	// Create the list view for all reviews
	public void createReviewListView(Stage primaryStage) {
		reviewList = new ListView<>();
		reviewList.setFocusTraversable(true);
		setReviewListCellFactory();
		
		// Set placeholder for event there are no reviews for search
		Label noResults = new Label("No results");
		noResults.setStyle("-fx-alignment: center; -fx-font-size: 20px; -fx-text-fill: gray;");
		reviewList.setPlaceholder(noResults);
		
		// Create event handler for when question is double clicked
		reviewList.setOnMouseClicked(event -> {
			if (event.getClickCount() == 2) { // Double click detected
				ReviewLightweightDTO selectedReview = reviewList.getSelectionModel().getSelectedItem();
				if (selectedReview != null) {
					loadSelectedReview(selectedReview, primaryStage);
				}
			}
		});
		
		// Create event handler for when enter key is pressed
		reviewList.setOnKeyPressed(event -> {
			if (event.getCode().toString().equals("ENTER")) { // Enter key detected
				ReviewLightweightDTO selectedReview = reviewList.getSelectionModel().getSelectedItem();
				if (selectedReview != null) {
					loadSelectedReview(selectedReview, primaryStage);
				}
			}
		});
	}
	
	// Set custom cell factory format for listed reviews
	public void setReviewListCellFactory() {
		reviewList.setCellFactory(parameter -> new ListCell<ReviewLightweightDTO>() {
			@Override
			protected void updateItem(ReviewLightweightDTO review, boolean empty) {
				super.updateItem(review, empty);
				if (empty || review == null) {
					setGraphic(null);
				} else {
					// Create a layout for each question
	                VBox layout = new VBox();
	                layout.setStyle("-fx-alignment: top-left; -fx-padding: 5;");

	                // Content Label
	                Label contentLabel = new Label(review.getContent());
	                contentLabel.setStyle("-fx-font-size: 14px;");
	                contentLabel.setWrapText(true);
	                contentLabel.setPrefWidth(reviewList.getWidth() - 40);

	                // Wrap text (this is the only work around I found because wrapping the text like normal doesn't work well for a ListView)
	                reviewList.widthProperty().addListener((observable, oldValue, newValue) -> {
	                	contentLabel.setPrefWidth(newValue.doubleValue() - 40); // Adjust width based on the ListView width minus padding
	                });

	                /* User Label // THIS COULD BE CONVERTED TO MESSAGE COUNT LATER
	                Label userLabel = new Label("Author: " + databaseHelper.getQuestionUsername(question.getQuestionId())); // Display username of who created the question
	                userLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: gray;");
	                userLabel.setWrapText(true);
	                userLabel.setPrefWidth(questionList.getWidth() - 40);*/

	                /* Wrap text (this is the only work around I found because wrapping the text like normal doesn't work well for a ListView)
	                reviewList.widthProperty().addListener((observable, oldValue, newValue) -> {
	                	userLabel.setPrefWidth(newValue.doubleValue() - 40); // Adjust width based on the ListView width minus padding
	                });*/
	                
	                layout.getChildren().addAll(contentLabel);
	                setGraphic(layout);
				}
			}
		});
	}
	
	// Load questions into question list
	public void loadReviews() {
		reviewList.getItems().clear();
		reviewList.getItems().addAll(myReviews);
	}
	
	// Load selected question on select event
	public void loadSelectedReview(ReviewLightweightDTO reviewDTO, Stage primaryStage) {
		if (canEdit) {
			System.out.println("Loading review id: " + reviewDTO.getReviewId());
		
			// Get review object
			Review review = databaseHelper.getReview(reviewDTO.getReviewId());
			boolean forQuestion = databaseHelper.reviewIsForQuestion(reviewDTO.getReviewId());
			int qaId = forQuestion ? review.getQuestionId() : review.getAnswerId();
			
			// Load review page
			new ReviewPage(databaseHelper, userId, qaId, forQuestion).show(primaryStage);
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
		
		Button backButton = new Button("Back to home");
		backButton.setOnAction(a -> {
			primaryStage.setScene(previousScene);
			primaryStage.setTitle(previousTitle);
		});
		
		actionBar.getChildren().addAll(spacer, backButton);
	}
}
