package application;

import database.DatabaseHelper;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class QuestionDraftPage {
	
	private DatabaseHelper databaseHelper;
	private QuestionSearchPage searchPage; // For refreshing when returning to search page
	private QuestionPage questionPage; // For reloading the question page
	private int userId;
	private int parentQuestionId;
	private int questionIdToUpdate;
	
	private final int MAX_TITLE_SIZE = 255;
	private final int MAX_DESCRIPTION_SIZE = 65535;
	
	private TextField titleField;
	private TextArea descriptionField;
	private Label titleErrorLabel;
	private Label descriptionErrorLabel;
	
	private Stage primaryStage;
	private Scene previousScene;
	private String previousTitle;
	
	public QuestionDraftPage(DatabaseHelper databaseHelper, QuestionSearchPage searchPage, int userId, int parentQuestionId) {
		this.databaseHelper = databaseHelper;
		this.searchPage = searchPage;
		this.questionPage = null;
		this.userId = userId;
		this.parentQuestionId = parentQuestionId;
		this.questionIdToUpdate = -1;
	}
	
	public QuestionDraftPage(DatabaseHelper databaseHelper, QuestionSearchPage searchPage, QuestionPage questionPage, int userId, int parentQuestionId, int questionIdToUpdate) {
		this.databaseHelper = databaseHelper;
		this.searchPage = searchPage;
		this.questionPage = questionPage;
		this.userId = userId;
		this.parentQuestionId = parentQuestionId;
		this.questionIdToUpdate = questionIdToUpdate;
	}

	public void show(Stage primaryStage) {
		this.primaryStage = primaryStage;
		previousScene = primaryStage.getScene();
		previousTitle = primaryStage.getTitle();
		
		VBox layout = new VBox(5);
	    layout.setStyle("-fx-alignment: center; -fx-padding: 20;");
	    
	    // Create title editor
	    VBox titleEditor = createTitleEditor(layout);
	    
	    // Create description editor
	    VBox descriptionEditor = createDescriptionEditor(layout);
	    
	    // Create action bar
	    HBox actionBar = createActionBar(layout);

	    layout.getChildren().addAll(titleEditor, new Separator(), descriptionEditor, new Separator(), actionBar);
	    Scene questionDraftScene = new Scene(layout, 800, 400);

	    // Set the scene to primary stage
	    primaryStage.setScene(questionDraftScene);
	    primaryStage.setTitle("Question Draft Page");
	}
	
	// Title editor section
	public VBox createTitleEditor(VBox parent) {
		VBox titleEditor = new VBox();
		titleEditor.setStyle("-fx-alignment: top-left;");
		titleEditor.setSpacing(5);

		// Header
	    Label header = new Label("Question Title");
	    header.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
		header.setWrapText(true);
	    header.maxWidthProperty().bind(parent.widthProperty().subtract(parent.getPadding().getLeft() + parent.getPadding().getRight()));
	    header.setFocusTraversable(true); // Default focus
	    
	    // Explain what to write
	    Label description = new Label("Enter a descriptive title using keywords so other users can find your question.");
        description.setStyle("-fx-font-size: 12px; -fx-text-fill: gray;");
		description.setWrapText(true);
		description.maxWidthProperty().bind(parent.widthProperty().subtract(parent.getPadding().getLeft() + parent.getPadding().getRight()));
	    
	    // Add title text field
	    titleField = new TextField();
	    titleField.setPromptText("Enter your question title here...");
	    titleField.setStyle("-fx-font-size: 14px;");
	    titleField.maxWidthProperty().bind(parent.widthProperty().subtract(parent.getPadding().getLeft() + parent.getPadding().getRight()));
	    
	    // Set title field text to current title
	    if (questionIdToUpdate != -1) {
	    	titleField.setText(databaseHelper.getQuestionTitle(questionIdToUpdate));
	    }
	    
	    // Set max characters for title
	    titleField.setTextFormatter(new TextFormatter<>(a -> 
	    	a.getControlNewText().length() <= MAX_TITLE_SIZE ? a : null
	    ));
	    
	    // Label to display error messages
        titleErrorLabel = new Label();
        titleErrorLabel.setStyle("-fx-text-fill: red; -fx-font-size: 12px;");
	    
	    titleEditor.getChildren().addAll(header, description, titleField, titleErrorLabel);
		return titleEditor;
	}
	
	// Description editor
	public VBox createDescriptionEditor(VBox parent) {
		VBox descriptionEditor = new VBox();
		descriptionEditor.setStyle("-fx-alignment: top-left; -fx-padding: 0px;");
		descriptionEditor.setSpacing(5);

		// Header
	    Label header = new Label("Question Description");
	    header.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
		header.setWrapText(true);
	    header.maxWidthProperty().bind(parent.widthProperty().subtract(parent.getPadding().getLeft() + parent.getPadding().getRight()));
	    
	    // Explain to write
	    Label description = new Label("Enter all the information someone would need to potentially answer your question.");
        description.setStyle("-fx-font-size: 12px; -fx-text-fill: gray;");
		description.setWrapText(true);
		description.maxWidthProperty().bind(parent.widthProperty().subtract(parent.getPadding().getLeft() + parent.getPadding().getRight()));
	    
	    // Add title text field
	    descriptionField = new TextArea();
	    descriptionField.setPromptText("Enter your question here...");
	    descriptionField.setStyle("-fx-font-size: 14px;");
	    descriptionField.setWrapText(true);
	    descriptionField.maxWidthProperty().bind(parent.widthProperty().subtract(parent.getPadding().getLeft() + parent.getPadding().getRight()));
	    
	    // Set description field text to current title
	    if (questionIdToUpdate != -1) {
	    	descriptionField.setText(databaseHelper.getQuestionDescription(questionIdToUpdate));
	    }
	    
	    // Set max characters for title
	    descriptionField.setTextFormatter(new TextFormatter<>(a -> 
	    	a.getControlNewText().length() <= MAX_DESCRIPTION_SIZE ? a : null
	    ));
	    
	    // Label to display error messages
        descriptionErrorLabel = new Label();
        descriptionErrorLabel.setStyle("-fx-text-fill: red; -fx-font-size: 12px;");
	    
	    // Make editor take up as much space as possible
	    descriptionField.setMaxHeight(Double.MAX_VALUE);
	    VBox.setVgrow(descriptionField, Priority.ALWAYS);
	    VBox.setVgrow(descriptionEditor, Priority.ALWAYS);

	    descriptionEditor.getChildren().addAll(header, description, descriptionField, descriptionErrorLabel);
		return descriptionEditor;
	}
	
	public HBox createActionBar(VBox parent) {
		HBox actionBar = new HBox(10);
		actionBar.setStyle("-fx-alignment: center-left;");
		actionBar.maxWidthProperty().bind(parent.widthProperty().subtract(parent.getPadding().getLeft() + parent.getPadding().getRight()));
		
		Button postButton = new Button("Post");
		postButton.setOnAction(a -> {
			Question question = new Question(userId, titleField.getText(), descriptionField.getText(), parentQuestionId);
			
			String errorMessage = Question.validate(question);
			if (errorMessage == "") {
				if (questionIdToUpdate != -1) {
					// Update question
					databaseHelper.updateQuestionTitle(questionIdToUpdate, question.getTitle());
					databaseHelper.updateQuestionDescription(questionIdToUpdate, question.getDescription());
					System.out.println("Updated question!");
				} else {
					// Post new question
					databaseHelper.addQuestion(question);
					System.out.println("Posted question!");
				}
				
				// Back to question search page
				backToSearch();
				
				if (questionPage != null) {
					// Reload question page
					questionPage.load();
				}
			} else {
				// Clear previous errors
				titleErrorLabel.setText("");
				descriptionErrorLabel.setText("");
				
				// Check which field causes error
				if (errorMessage == "*** Error *** Question title cannot be empty" || errorMessage == "*** Error *** Question title cannot exceed maximum length (255 characters)") {
					titleErrorLabel.setText(errorMessage);
				} else if (errorMessage == "*** Error *** Question description cannot be empty" || errorMessage == "*** Error *** Question description cannot exceed maximum length (65535 characters)") {
					descriptionErrorLabel.setText(errorMessage);
				}
			}
		});
		
		// Add a spacer that will push the following buttons to the right
		Region spacer = new Region();
		HBox.setHgrow(spacer, Priority.ALWAYS); // Forces elements to the right
		
		Button backButton = new Button("Cancel");
		backButton.setOnAction(a -> {
			// Ask if user if sure
			if (new ConfirmDelete().show("Are you sure you want to delete this draft?")) {
				backToSearch();
			}
		});
		
		actionBar.getChildren().addAll(postButton, spacer, backButton);
		
		return actionBar;
	}
	
	public void backToSearch() {
		primaryStage.setScene(previousScene);
		primaryStage.setTitle(previousTitle);
		/*
		 * There is a warning when trying to load previous
		 * scenes where the style is different. I tried storing the previous
		 * style but that didn't work, it is most likely something that
		 * is not worth fixing unless it causes errors down the road.
		 */
		
		// Reload search page
		searchPage.searchQuestions();
	}
	
}
