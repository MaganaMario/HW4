package application;

import database.DatabaseHelper;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextFormatter;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class ReviewDraftPage {

	private DatabaseHelper databaseHelper;
	private ReviewPage reviewPage; // For refreshing when returning to review page
	private int userId;
	private int qaId;
	private boolean forQuestion;
	private int reviewIdToUpdate;
	
	private final int MAX_CONTENT_SIZE = 65535;
	
	private TextArea contentField;
	private Label contentErrorLabel;
	
	private Stage primaryStage;
	private Scene previousScene;
	private String previousTitle;
	
	public ReviewDraftPage(DatabaseHelper databaseHelper, ReviewPage reviewPage, int userId, int qaId, boolean forQuestion) {
		this.databaseHelper = databaseHelper;
		this.reviewPage = reviewPage;
		this.userId = userId;
		this.qaId = qaId;
		this.forQuestion = forQuestion;
		this.reviewIdToUpdate = -1;
	}
	
	public ReviewDraftPage(DatabaseHelper databaseHelper, ReviewPage reviewPage, int userId, int qaId, boolean forQuestion, int reviewIdToUpdate) {
		this.databaseHelper = databaseHelper;
		this.reviewPage = reviewPage;
		this.userId = userId;
		this.qaId = qaId;
		this.forQuestion = forQuestion;
		this.reviewIdToUpdate = reviewIdToUpdate;
	}
	
	public void show(Stage primaryStage) {
		this.primaryStage = primaryStage;
		previousScene = primaryStage.getScene();
		previousTitle = primaryStage.getTitle();
		
		VBox layout = new VBox(10);
	    layout.setStyle("-fx-alignment: center;");
	    
	    VBox scrollLayout = new VBox();
	    scrollLayout.setStyle("-fx-alignment: top-center; -fx-padding: 20;");
	    scrollLayout.setSpacing(10);
	    
	    ScrollPane layoutScrollPane = new ScrollPane();
	    layoutScrollPane.setContent(scrollLayout);
	    layoutScrollPane.setFitToWidth(true);
	    VBox.setVgrow(layoutScrollPane, Priority.ALWAYS);
	    
	    // Create qa display
	    if (forQuestion) {
	    	scrollLayout.getChildren().add(createQuestionDisplay(scrollLayout));
	    } else {
	    	scrollLayout.getChildren().add(createAnswerDisplay(scrollLayout));
	    }
	    
	    // Create content editor
	    VBox contentEditor = createContentEditor(scrollLayout);
	    
	    // Create action bar
	    HBox actionBar = createActionBar(scrollLayout);
	    
	    // Set scroll layout
	    scrollLayout.getChildren().addAll(new Separator(), contentEditor);
	    
	    layout.getChildren().addAll(layoutScrollPane, actionBar);
	    Scene reviewDraftScene = new Scene(layout, 800, 400);

	    // Set the scene to primary stage
	    primaryStage.setScene(reviewDraftScene);
	    primaryStage.setTitle("Review Draft Page");
	}
	
	public VBox createQuestionDisplay(VBox parent) {
		VBox questionDisplay = new VBox(5);
	    questionDisplay.setStyle("-fx-alignment: top-left; -fx-padding: 0px;");
		questionDisplay.setSpacing(5);
		
		// Title
		Label title = new Label(databaseHelper.getQuestionTitle(qaId));
	    title.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
	    title.setWrapText(true);
	    title.maxWidthProperty().bind(parent.widthProperty().subtract(parent.getPadding().getLeft() + parent.getPadding().getRight()));
	    title.setFocusTraversable(true); // Set focusable to ensure the user opens to the start of the page
	    questionDisplay.getChildren().add(title);
	    
	    // Question Info
	    VBox info = new VBox();
	    info.setStyle("-fx-alignment: center-left");
	    info.maxWidthProperty().bind(parent.widthProperty().subtract(parent.getPadding().getLeft() + parent.getPadding().getRight()));
	    formatQuestionInfo(info);
	    questionDisplay.getChildren().add(info);
	    
	    // Add separator
	    questionDisplay.getChildren().add(new Separator());
	    
	    // Description
	    Label description = new Label(databaseHelper.getQuestionDescription(qaId));
	    description.setStyle("-fx-font-size: 16px;");
	    description.setWrapText(true);
	    description.maxWidthProperty().bind(parent.widthProperty().subtract(parent.getPadding().getLeft() + parent.getPadding().getRight()));
	    questionDisplay.getChildren().add(description);
		
		return questionDisplay;
	}
	
	public void formatQuestionInfo(VBox info) {
		// Add Author
		Label author = new Label("Author: " + databaseHelper.getQuestionUsername(qaId));
		author.setStyle("-fx-font-size: 12px;");
		author.setWrapText(true);
		author.maxWidthProperty().bind(info.widthProperty().subtract(info.getPadding().getLeft() + info.getPadding().getRight()));
		
		info.getChildren().addAll(author);
	}
	
	public VBox createAnswerDisplay(VBox parent) {
		VBox answerDisplay = new VBox(5);
	    answerDisplay.setStyle("-fx-alignment: top-left; -fx-padding: 0px;");
	    
	    // Answer Info
	    VBox info = new VBox();
	    info.setStyle("-fx-alignment: center-left");
	    info.maxWidthProperty().bind(parent.widthProperty().subtract(parent.getPadding().getLeft() + parent.getPadding().getRight()));
	    formatAnswerInfo(info);
	    answerDisplay.getChildren().add(info);
	    
	    // Add separator
	    answerDisplay.getChildren().add(new Separator());
	    
	    // Content
	    Label content = new Label(databaseHelper.getAnswerContent(qaId));
	    content.setStyle("-fx-font-size: 16px;");
	    content.setWrapText(true);
	    content.maxWidthProperty().bind(parent.widthProperty().subtract(parent.getPadding().getLeft() + parent.getPadding().getRight()));
	    answerDisplay.getChildren().add(content);
		
		return answerDisplay;
	}
	
	public void formatAnswerInfo(VBox info) {
		// Add Author
		Label author = new Label("Author: " + databaseHelper.getAnswerUsername(qaId));
		author.setStyle("-fx-font-size: 12px;");
		author.setWrapText(true);
		author.maxWidthProperty().bind(info.widthProperty().subtract(info.getPadding().getLeft() + info.getPadding().getRight()));
		
		info.getChildren().addAll(author);
	}
	
	public VBox createContentEditor(VBox parent) {
		VBox contentEditor = new VBox(5);
		contentEditor.setStyle("-fx-alignment: top-left; -fx-padding: 0px;");
		contentEditor.setSpacing(5);

		// Header
	    Label header = new Label("Review Content");
	    header.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
		header.setWrapText(true);
	    header.maxWidthProperty().bind(parent.widthProperty().subtract(parent.getPadding().getLeft() + parent.getPadding().getRight()));
	    
	    // Explain to write
	    Label description = new Label("Enter all the information someone would need to improve their response.");
        description.setStyle("-fx-font-size: 12px; -fx-text-fill: gray;");
		description.setWrapText(true);
		description.maxWidthProperty().bind(parent.widthProperty().subtract(parent.getPadding().getLeft() + parent.getPadding().getRight()));
	    
	    // Add title text field
	    contentField = new TextArea();
	    contentField.setPromptText("Enter your review here...");
	    contentField.setStyle("-fx-font-size: 14px;");
	    contentField.setWrapText(true);
	    contentField.maxWidthProperty().bind(parent.widthProperty().subtract(parent.getPadding().getLeft() + parent.getPadding().getRight()));
	    
	    // Set content field text to current answer
	    if (reviewIdToUpdate != -1) {
	    	contentField.setText(databaseHelper.getReview(reviewIdToUpdate).getContent());
	    }

	    // Set max characters for content
	    contentField.setTextFormatter(new TextFormatter<>(a -> 
	    	a.getControlNewText().length() <= MAX_CONTENT_SIZE ? a : null
	    ));
	    
	    // Label to display error messages
	    contentErrorLabel = new Label();
	    contentErrorLabel.setStyle("-fx-text-fill: red; -fx-font-size: 12px;");
	    
	    // Make editor take up as much space as possible
	    contentField.setMaxHeight(Double.MAX_VALUE);
	    VBox.setVgrow(contentField, Priority.ALWAYS);
	    VBox.setVgrow(contentEditor, Priority.ALWAYS);

	    contentEditor.getChildren().addAll(header, description, contentField, contentErrorLabel);
		
		return contentEditor;
	}
	
	public HBox createActionBar(VBox parent) {
		HBox actionBar = new HBox(10);
		actionBar.setStyle("-fx-alignment: center-left; -fx-padding: 0 20 20 20;");
		actionBar.maxWidthProperty().bind(parent.widthProperty().subtract(parent.getPadding().getLeft() + parent.getPadding().getRight()));
		
		Button postButton = new Button("Post");
		postButton.setOnAction(a -> {
			Review review = new Review(userId, forQuestion ? qaId : null, forQuestion ? null : qaId, contentField.getText());
			
			String errorMessage = Review.validate(review);
			if (errorMessage == "") {
				if (reviewIdToUpdate != -1) {
					// Update answer
					databaseHelper.updateReviewContent(reviewIdToUpdate, review.getContent());
					System.out.println("Updated review!");
				} else {
					// Post new answer
					databaseHelper.addReview(review);
					System.out.println("Posted review!");
				}
				
				// Back to question search page
				backToQuestion();
			} else {
				// Set error message
				contentErrorLabel.setText(errorMessage);
			}
		});
		
		// Add a spacer that will push the following buttons to the right
		Region spacer = new Region();
		HBox.setHgrow(spacer, Priority.ALWAYS); // Forces elements to the right
		
		Button backButton = new Button("Cancel");
		backButton.setOnAction(a -> {
			// Ask if user if sure
			if (new ConfirmDelete().show("Are you sure you want to delete this draft?")) {
				backToQuestion();
			}
		});
		
		actionBar.getChildren().addAll(postButton, spacer, backButton);
		
		return actionBar;
	}
	
	public void backToQuestion() {
		primaryStage.setScene(previousScene);
		primaryStage.setTitle(previousTitle);
		/*
		 * There is a warning when trying to load previous
		 * scenes where the style is different. I tried storing the previous
		 * style but that didn't work, it is most likely something that
		 * is not worth fixing unless it causes errors down the road.
		 */
		
		// Reload question page
		reviewPage.load();
	}
}
