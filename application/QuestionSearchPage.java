package application;

import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ListCell;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.List;

import database.DatabaseHelper;

public class QuestionSearchPage {
	
	private DatabaseHelper databaseHelper;
	private int userId;
	
	private List<QuestionLightweightDTO> questions;
	
	private TextField searchBar;
	private ListView<QuestionLightweightDTO> questionList;
	private HBox actionBar;
	
	private String sort = "ORDER BY id DESC";
	private String filter = "";
	
	private Scene previousScene;
	private String previousTitle;
	
	public QuestionSearchPage(DatabaseHelper databaseHelper, int userId) {
		this.databaseHelper = databaseHelper;
		this.userId = userId;
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
	    
	    // Question list
	    createQuestionListView(primaryStage);
	    layout.getChildren().add(questionList);
	    searchQuestions(); // Run to get list
	    
	    // Action Bar
	    createActionBar(primaryStage, layout);
	    layout.getChildren().add(actionBar);
	    
	    // Set list expandable
	    VBox.setVgrow(questionList, Priority.ALWAYS);
	    
	    // Set the scene to primary stage
	    Scene questionScene = new Scene(layout, 800, 400);
	    primaryStage.setScene(questionScene);
	    primaryStage.setTitle("Question Search Page");
    	
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
		searchBar.setOnKeyReleased(a -> searchQuestions());
		
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
		statusFilter.getItems().addAll("All", "Unresolved", "Resolved", "My Questions", "My Unresolved", "My Resolved");
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
			sort = "ORDER BY title ASC";
			break;
		case "Z-A":
			sort = "ORDER BY title DESC";
			break;
		default:
			filter = "ORDER BY id DESC";
			break;
		}
		
		searchQuestions();
	}
	
	// Set filter and search
	public void setFilter(String filterType) {
		switch (filterType) {
		case "All":
			filter = "";
			break;
		case "Unresolved":
			filter = "resolved = FALSE";
			break;
		case "Resolved":
			filter = "resolved = TRUE";
			break;
		case "My Questions":
			filter = "userId = " + userId;
			break;
		case "My Unresolved":
			filter = "resolved = FALSE AND userId = " + userId;
			break;
		case "My Resolved":
			filter = "resolved = TRUE AND userId = " + userId;
			break;
		default:
			filter = "";
			break;
		}
		
		searchQuestions();
	}
	
	// Find questions based on search bar input
	public void searchQuestions() {
		String searchText = searchBar.getText().trim();
		
		if (searchBar.getText().trim().isEmpty()) { // Show all questions
			if (filter != "") {
				questions = databaseHelper.getAllQuestions("WHERE " + filter + " " + sort);
			} else {
				questions = databaseHelper.getAllQuestions(sort);
			}
		} else {
			// Search using keywords
			if (filter != "") {
				questions = DatabaseHelper.searchQuestions(searchText, filter, sort);
			} else {
				questions = DatabaseHelper.searchQuestions(searchText, filter, sort); 
			}
	      }  
	
		loadQuestions();
	}
	
	// Create the list view for all questions
	public void createQuestionListView(Stage primaryStage) {
		questionList = new ListView<>();
		questionList.setFocusTraversable(true);
		setQuestionListCellFactory();
		
		// Set placeholder for event there are no questions for search
		Label noResults = new Label("No results");
		noResults.setStyle("-fx-alignment: center; -fx-font-size: 20px; -fx-text-fill: gray;");
		questionList.setPlaceholder(noResults);
		
		// Create event handler for when question is double clicked
		questionList.setOnMouseClicked(event -> {
			if (event.getClickCount() == 2) { // Double click detected
				QuestionLightweightDTO selectedQuestion = questionList.getSelectionModel().getSelectedItem();
				if (selectedQuestion != null) {
					loadSelectedQuestion(selectedQuestion, primaryStage);
				}
			}
		});
		
		// Create event handler for when enter key is pressed
		questionList.setOnKeyPressed(event -> {
			if (event.getCode().toString().equals("ENTER")) { // Enter key detected
				QuestionLightweightDTO selectedQuestion = questionList.getSelectionModel().getSelectedItem();
				if (selectedQuestion != null) {
					loadSelectedQuestion(selectedQuestion, primaryStage);
				}
			}
		});
	}
	
	// Set custom cell factory format for listed questions
	public void setQuestionListCellFactory() {
		questionList.setCellFactory(parameter -> new ListCell<QuestionLightweightDTO>() {
			@Override
			protected void updateItem(QuestionLightweightDTO question, boolean empty) {
				super.updateItem(question, empty);
				if (empty || question == null) {
					setGraphic(null);
				} else {
					// Create a layout for each question
	                VBox layout = new VBox();
	                layout.setStyle("-fx-alignment: top-left; -fx-padding: 5;");

	                // Title Label
	                Label titleLabel = new Label(question.getTitle());
	                titleLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
	                titleLabel.setWrapText(true);
	                titleLabel.setPrefWidth(questionList.getWidth() - 40);

	                // Wrap text (this is the only work around I found because wrapping the text like normal doesn't work well for a ListView)
	                questionList.widthProperty().addListener((observable, oldValue, newValue) -> {
	                    titleLabel.setPrefWidth(newValue.doubleValue() - 40); // Adjust width based on the ListView width minus padding
	                });

	                // User Label
	                Label userLabel = new Label("Author: " + databaseHelper.getQuestionUsername(question.getQuestionId())); // Display username of who created the question
	                userLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: gray;");
	                userLabel.setWrapText(true);
	                userLabel.setPrefWidth(questionList.getWidth() - 40);

	                // Wrap text (this is the only work around I found because wrapping the text like normal doesn't work well for a ListView)
	                questionList.widthProperty().addListener((observable, oldValue, newValue) -> {
	                	userLabel.setPrefWidth(newValue.doubleValue() - 40); // Adjust width based on the ListView width minus padding
	                });
	                
	                layout.getChildren().addAll(titleLabel, userLabel);
	                
	                // Add unread potential answer count if question is not resolved
	                if (question.getUserId() == userId && !databaseHelper.isResolved(question.getQuestionId())) {
	                	// Unread Label
		                Label unreadLabel = new Label("Unread: " + databaseHelper.getQuestionUnreadCount(question.getQuestionId())); // Display number of unread potential answers
		                unreadLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: gray;");
		                unreadLabel.setWrapText(true);
		                unreadLabel.setPrefWidth(questionList.getWidth() - 40);
	
		                // Wrap text (this is the only work around I found because wrapping the text like normal doesn't work well for a ListView)
		                questionList.widthProperty().addListener((observable, oldValue, newValue) -> {
		                	unreadLabel.setPrefWidth(newValue.doubleValue() - 40); // Adjust width based on the ListView width minus padding
		                });
		                
		                layout.getChildren().add(unreadLabel);
	                }
	                
	                setGraphic(layout);
				}
			}
		});
	}
	
	// Load questions into question list
	public void loadQuestions() {
		questionList.getItems().clear();
		questionList.getItems().addAll(questions);
	}
	
	// Load selected question on select event
	public void loadSelectedQuestion(QuestionLightweightDTO question, Stage primaryStage) {
		System.out.println("Loading question id: " + question.getQuestionId());
		
		// Reset unread count if author is opening question
		if (userId == question.getUserId()) {
			databaseHelper.resetQuestionUnreadCount(question.getQuestionId());
		}
		
		// Load question page
		new QuestionPage(databaseHelper, this, userId, question.getQuestionId()).show(primaryStage);
	}
	
	// Create action bar
	public void createActionBar(Stage primaryStage, VBox parent) {
		actionBar = new HBox(10);
		actionBar.setStyle("-fx-alignment: center-left;");
		actionBar.maxWidthProperty().bind(parent.widthProperty().subtract(parent.getPadding().getLeft() + parent.getPadding().getRight()));
		
		// Left button flags
		boolean canCreateQuestions = databaseHelper.getUserRole(userId).contains("student");

		if (canCreateQuestions) {
			// New question button
			Button newQuestion = new Button("New Question");
			newQuestion.setOnAction(a -> {
				// Load question draft page
				new QuestionDraftPage(databaseHelper, this, userId, -1).show(primaryStage); // -1 for no parent question
			});
			actionBar.getChildren().add(newQuestion);
		}
		
		
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

