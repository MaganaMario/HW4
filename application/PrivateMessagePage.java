package application;

import java.util.List;

import database.DatabaseHelper;

import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextFormatter;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class PrivateMessagePage {
	private DatabaseHelper databaseHelper;
	private int authorId;
	private int commenterId;
	private int userId;
	private String parentType;
	private int parentId;
	
	private final int MAX_CONTENT_SIZE = 65535;

	private TextArea contentField;
	private Label contentErrorLabel;
	
	private Stage primaryStage;
	private Scene previousScene;
	private String previousTitle;
	
	public PrivateMessagePage(DatabaseHelper databaseHelper, int userId, int authorId, int commenterId, int parentId, String parentType) {
		this.databaseHelper = databaseHelper;
		this.userId = userId;
		this.authorId = authorId;
		this.commenterId = commenterId;
		this.parentId = parentId;
		this.parentType = parentType;
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
	    
	    // Create qar display
	    if (parentType == "question") {
	    	scrollLayout.getChildren().add(createQuestionDisplay(scrollLayout));
	    } else if (parentType == "answer") {
	    	scrollLayout.getChildren().add(createmessageDisplay(scrollLayout));
	    } else if (parentType == "review") {
	    	scrollLayout.getChildren().add(createReviewDisplay(scrollLayout));
	    }
	    
	    VBox messageDisplay = formatMessage(scrollLayout);
	    
	    // Create content editor
	    VBox contentEditor = createContentEditor(scrollLayout);
	    
	    // Create action bar
	    HBox actionBar = createActionBar(scrollLayout);
	    
	    // Set scroll layout
	    scrollLayout.getChildren().addAll(new Separator(), messageDisplay, contentEditor);
	    
	    layout.getChildren().addAll(layoutScrollPane, actionBar);
	    Scene reviewDraftScene = new Scene(layout, 800, 400);

	    // Set the scene to primary stage
	    primaryStage.setScene(reviewDraftScene);
	    primaryStage.setTitle("Message Page");
	}
	
	public VBox createQuestionDisplay(VBox parent) {
		VBox questionDisplay = new VBox(5);
	    questionDisplay.setStyle("-fx-alignment: top-left; -fx-padding: 0px;");
		questionDisplay.setSpacing(5);
		
		// Title
		Label title = new Label(databaseHelper.getQuestionTitle(parentId));
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
	    Label description = new Label(databaseHelper.getQuestionDescription(parentId));
	    description.setStyle("-fx-font-size: 16px;");
	    description.setWrapText(true);
	    description.maxWidthProperty().bind(parent.widthProperty().subtract(parent.getPadding().getLeft() + parent.getPadding().getRight()));
	    questionDisplay.getChildren().add(description);
		
		return questionDisplay;
	}
	
	public void formatQuestionInfo(VBox info) {
		// Add Author
		Label author = new Label(databaseHelper.getQuestionUsername(parentId));
		author.setStyle("-fx-font-size: 12px;");
		author.setWrapText(true);
		author.maxWidthProperty().bind(info.widthProperty().subtract(info.getPadding().getLeft() + info.getPadding().getRight()));
		
		info.getChildren().addAll(author);
	}
	
	public VBox createmessageDisplay(VBox parent) {
		VBox messageDisplay = new VBox(5);
	    messageDisplay.setStyle("-fx-alignment: top-left; -fx-padding: 0px;");
	    
	    // Answer Info
	    VBox info = new VBox();
	    info.setStyle("-fx-alignment: center-left");
	    info.maxWidthProperty().bind(parent.widthProperty().subtract(parent.getPadding().getLeft() + parent.getPadding().getRight()));
	    formatAnswerInfo(info);
	    messageDisplay.getChildren().add(info);
	    
	    // Add separator
	    messageDisplay.getChildren().add(new Separator());
	    
	    // Content
	    Label content = new Label(databaseHelper.getAnswerContent(parentId));
	    content.setStyle("-fx-font-size: 16px;");
	    content.setWrapText(true);
	    content.maxWidthProperty().bind(parent.widthProperty().subtract(parent.getPadding().getLeft() + parent.getPadding().getRight()));
	    messageDisplay.getChildren().add(content);
		
		return messageDisplay;
	}
	
	public void formatAnswerInfo(VBox info) {
		// Add Author
		Label author = new Label(databaseHelper.getAnswerUsername(authorId));
		author.setStyle("-fx-font-size: 12px;");
		author.setWrapText(true);
		author.maxWidthProperty().bind(info.widthProperty().subtract(info.getPadding().getLeft() + info.getPadding().getRight()));
		
		info.getChildren().addAll(author);
	}
	
	public VBox createReviewDisplay(VBox parent) {
		VBox reviewDisplay = new VBox(5);
		reviewDisplay.setStyle("-fx-alignment: top-left; -fx-padding: 0px;");
		
		// Review Info
	    VBox info = new VBox();
	    info.setStyle("-fx-alignment: center-left");
	    info.maxWidthProperty().bind(parent.widthProperty().subtract(parent.getPadding().getLeft() + parent.getPadding().getRight()));
	    formatReviewInfo(info);
	    reviewDisplay.getChildren().add(info);

	    // Add separator
	    reviewDisplay.getChildren().add(new Separator());
	    
	    // Content
	    Label content = new Label(databaseHelper.getReviewContent(parentId));
	    
	    content.setStyle("-fx-font-size: 16px;");
	    content.setWrapText(true);
	    content.maxWidthProperty().bind(parent.widthProperty().subtract(parent.getPadding().getLeft() + parent.getPadding().getRight()));
	    reviewDisplay.getChildren().add(content);
	    
	    return reviewDisplay;


	}
	
	public void formatReviewInfo(VBox info) {
		// Add Author
		Label author = new Label(databaseHelper.getReviewUsername(parentId));
		// Label author = new Label("author test");
		author.setStyle("-fx-font-size: 12px;");
		author.setWrapText(true);
		
		info.getChildren().addAll(author);
	}
	
	public VBox createContentEditor(VBox parent) {
		VBox contentEditor = new VBox(5);
		contentEditor.setStyle("-fx-alignment: top-left; -fx-padding: 0px;");
		contentEditor.setSpacing(5);

		// Header
	    Label header = new Label("Message Content");
	    header.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
		header.setWrapText(true);
	    header.maxWidthProperty().bind(parent.widthProperty().subtract(parent.getPadding().getLeft() + parent.getPadding().getRight()));
	    
	    // Explain to write
	    Label description = new Label("Give the author your feedback on their response.");
        description.setStyle("-fx-font-size: 12px; -fx-text-fill: gray;");
		description.setWrapText(true);
		description.maxWidthProperty().bind(parent.widthProperty().subtract(parent.getPadding().getLeft() + parent.getPadding().getRight()));
	    
	    // Add title text field
	    contentField = new TextArea();
	    contentField.setPromptText("Enter your message here...");
	    contentField.setStyle("-fx-font-size: 14px;");
	    contentField.setWrapText(true);
	    contentField.maxWidthProperty().bind(parent.widthProperty().subtract(parent.getPadding().getLeft() + parent.getPadding().getRight()));
	    
	    // Set content field text to current answer
//	    if (reviewIdToUpdate != -1) {
//	    	contentField.setText(databaseHelper.getReview(reviewIdToUpdate).getContent());
//	    }

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
			String messageContent = contentField.getText();
			boolean isAuthor = (userId == authorId);
			PrivateMessage pm = new PrivateMessage(authorId, commenterId, isAuthor, messageContent, false, parentType, parentId);
			
			String errorMessage = PrivateMessage.validate(pm);
			if (errorMessage == "") {
				
					// Post new answer
					databaseHelper.addPrivateMessage(pm);
					System.out.println("Sent message!");
					
					// Remove text from field
					contentField.clear();
				
				// Display message as sent
				// StackPane newMessage = messageIcon(messageContent);
				parent.getChildren().remove(2); // should be messageDisplay
				parent.getChildren().add(2, formatMessage(parent));
				// databaseHelper.setMessageUnreadFlag(commenterId);
			} else {
				// Set error message
				contentErrorLabel.setText(errorMessage);
			}
		});
		
		// Add a spacer that will push the following buttons to the right
		Region spacer = new Region();
		HBox.setHgrow(spacer, Priority.ALWAYS); // Forces elements to the right
		
		Button backButton = new Button("Back");
		backButton.setOnAction(a -> {
			backToQuestion();
		});
		
		actionBar.getChildren().addAll(postButton, spacer, backButton);
		
		return actionBar;
	}
	
	
	public VBox formatMessage(VBox parent) {
		//what is needed is a list of messages that refreshes the database on the action of a button
		VBox messageDisplay = new VBox();
	    messageDisplay.setStyle("-fx-alignment: top-left; -fx-padding: 0px;");
	    messageDisplay.setSpacing(5);
		
		List<PrivateMessageLightweightDTO> messageList = databaseHelper.getAllPrivateMessages(authorId, commenterId, parentType, parentId);
		
		
		// Check if answer exists
		if (messageList.isEmpty()) {
			Label notFound = new Label("No messages.");
		    notFound.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
		    messageDisplay.getChildren().add(notFound);
			return messageDisplay;
		}
		
		
	    
		for (int i = 0; i < messageList.size(); i++) {
			PrivateMessageLightweightDTO currentMessage = messageList.get(i);
			Label content = new Label(currentMessage.getContent());
		    content.setStyle("-fx-font-size: 16px;");
		    content.setWrapText(true);
		    content.maxWidthProperty().bind(parent.widthProperty().subtract(parent.getPadding().getLeft() + parent.getPadding().getRight()));
		    messageDisplay.getChildren().add(content);
		    
		    VBox info = new VBox(10);
		    info.setStyle("-fx-alignment: center-left");
		    info.maxWidthProperty().bind(parent.widthProperty().subtract(parent.getPadding().getLeft() + parent.getPadding().getRight()));
		    formatMessageInfo(info, currentMessage);
		    messageDisplay.getChildren().add(info);
		    
		    messageDisplay.getChildren().add(new Separator());
		}
	    
	    return messageDisplay;
	}
	
	public void formatMessageInfo(VBox info, PrivateMessageLightweightDTO message) {
	    String username = databaseHelper.getUser(message.isByAuthor() ? authorId : commenterId).getUserName();

	    // Author
	    Label author = new Label(username);
	    author.setStyle("-fx-font-size: 12px;");
	    author.setWrapText(true);
	    author.maxWidthProperty().bind(info.widthProperty().subtract(info.getPadding().getLeft() + info.getPadding().getRight()));

	    info.getChildren().add(author);
	}


	
	public StackPane messageIcon(String message) {
		StackPane stackpane = new StackPane(); 
		Text text = new Text(0, 0, message);
		text.setWrappingWidth(30);
		Rectangle rectangle = new Rectangle(0, 0, 30, 50);
		rectangle.setStroke(Color.BLACK);
		rectangle.setFill(Color.WHITE);
		
		stackpane.getChildren().addAll(rectangle, text);
		
		return stackpane;
		
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
	}

	
}
