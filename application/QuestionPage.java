package application;

import java.util.List;

import database.DatabaseHelper;
import javafx.scene.Scene;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

public class QuestionPage {
	
	private DatabaseHelper databaseHelper;
	private QuestionSearchPage searchPage; // For refreshing when returning to search page
	private int userId;
	private int questionId;
	
	private Question question;
	private List<AnswerLightweightDTO> answers;
	private Button currentStarredAnswer;

	// Icons
	private final int ICON_SIZE = 16;
	private Image starUnfilled = new Image(getClass().getResource("/images/starUnfilled.png").toExternalForm());
	private Image starFilled = new Image(getClass().getResource("/images/starFilled.png").toExternalForm());
	private Image upvoteUnfilled = new Image(getClass().getResource("/images/upvoteUnfilled.png").toExternalForm());
	private Image downvoteUnfilled = new Image(getClass().getResource("/images/downvoteUnfilled.png").toExternalForm());
	private Image upvoteFilled = new Image(getClass().getResource("/images/upvoteFilled.png").toExternalForm());
	private Image downvoteFilled = new Image(getClass().getResource("/images/downvoteFilled.png").toExternalForm());
	private Image editIcon = new Image(getClass().getResource("/images/editIcon.png").toExternalForm());
	private Image trashIcon = new Image(getClass().getResource("/images/trashIcon.png").toExternalForm());
	private Image messageIcon = new Image(getClass().getResource("/images/messageIcon.png").toExternalForm());
	
	private VBox questionDisplay;
	
	private Stage primaryStage;
	private Scene previousScene;
	private String previousTitle;
	
	public QuestionPage(DatabaseHelper databaseHelper, QuestionSearchPage searchPage, int userId, int questionId) {
		this.questionId = questionId;
		this.searchPage = searchPage;
		this.userId = userId;
		this.databaseHelper = databaseHelper;
		
		// Load question
		question = databaseHelper.getQuestion(questionId);
		
		// Load answers
		answers = databaseHelper.getAllAnswers(questionId);
	}
	
	public void show(Stage primaryStage) {
		this.primaryStage = primaryStage;
		previousScene = primaryStage.getScene();
		previousTitle = primaryStage.getTitle();
		
		load();
	}
	
	// Loads screen contents
	public void load() {
		// Load question
		question = databaseHelper.getQuestion(questionId);
		
		// Load answers
		answers = databaseHelper.getAllAnswers(questionId);
		
		VBox layout = new VBox();
		layout.setStyle("-fx-alignment: top-center;");
	    layout.setSpacing(10);
		
		VBox scrollLayout = new VBox();
	    scrollLayout.setStyle("-fx-alignment: top-center; -fx-padding: 20;");
	    scrollLayout.setSpacing(10);
	    
	    ScrollPane layoutScrollPane = new ScrollPane();
	    layoutScrollPane.setContent(scrollLayout);
	    layoutScrollPane.setFitToWidth(true);
	    VBox.setVgrow(layoutScrollPane, Priority.ALWAYS);
	    
	    // Format question
	    formatQuestion(scrollLayout);
	    scrollLayout.getChildren().add(questionDisplay);
	    
	    // Add separators and resolved answer label if there is a resolved answer
	    boolean isResolved = databaseHelper.isResolved(questionId);
	    int resolvedAnswerId = databaseHelper.getResolvedAnswerId(questionId);
	    if (isResolved) {
	    	// Add separators and resolved answer label
	    	Label resolvedLabel = new Label("Resolved Answer");
		    resolvedLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
		    
		    // Add resolved answer
		    AnswerLightweightDTO resolvedDTO = databaseHelper.getAnswerDTO(resolvedAnswerId);
		    VBox answerDisplay = formatAnswer(scrollLayout, resolvedDTO);
	    	if(resolvedDTO != null) {
	    		scrollLayout.getChildren().addAll(new Separator(), resolvedLabel, new Separator(), answerDisplay);
	    	} else {
	    		isResolved = false; // print as normal is resolved print is unsuccessful
	    	}
	    }
	    
	    // Add separators and answer label
	    Label answerLabel = new Label(answers.size() + " Answer" + (answers.size() == 1 ? "" : "s"));
	    answerLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
	    scrollLayout.getChildren().addAll(new Separator(), answerLabel, new Separator());
	    
	    // Format answers
	    for (int i = 0; i < answers.size(); i++) {
	    	if (isResolved && answers.get(i).getAnswerId() == resolvedAnswerId) {
	    		continue; // Skip answer if it is the resolved answer (already printed)
	    	}
	    	
	    	VBox answerDisplay = formatAnswer(scrollLayout, answers.get(i));
	    	if(answerDisplay != null) {
	    		scrollLayout.getChildren().addAll(answerDisplay, new Separator());
	    	}
	    }
	    
	    // Add pageBar
	    HBox pageBar = createPageBar(layout);
	    
	    // Set layout
	    layout.getChildren().addAll(layoutScrollPane, pageBar);
	    
		// Set the scene to primary stage
	    Scene questionPageScene = new Scene(layout, 800, 400);
	    primaryStage.setScene(questionPageScene);
	    primaryStage.setTitle("Question Page");
	}
	
	public void formatQuestion(VBox parent) {
		questionDisplay = new VBox();
	    questionDisplay.setStyle("-fx-alignment: top-left; -fx-padding: 0px;");
		questionDisplay.setSpacing(5);
		
		// Check if question exists
		if (question == null) {
			Label notFound = new Label("Question not found.");
		    notFound.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
		    questionDisplay.getChildren().add(notFound);
			return;
		}
		
		// Title
		Label title = new Label(question.getTitle());
	    title.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
	    title.setWrapText(true);
	    title.maxWidthProperty().bind(parent.widthProperty().subtract(parent.getPadding().getLeft() + parent.getPadding().getRight()));
	    title.setFocusTraversable(true); // Set focusable to ensure the user opens to the start of the page
	    questionDisplay.getChildren().add(title);
	    
	    // Question Info
	    VBox info = new VBox(10);
	    info.setStyle("-fx-alignment: center-left");
	    info.maxWidthProperty().bind(parent.widthProperty().subtract(parent.getPadding().getLeft() + parent.getPadding().getRight()));
	    formatQuestionInfo(info);
	    questionDisplay.getChildren().add(info);
	    
	    // Add separator
	    questionDisplay.getChildren().add(new Separator());
	    
	    // Description
	    Label description = new Label(question.getDescription());
	    description.setStyle("-fx-font-size: 16px;");
	    description.setWrapText(true);
	    description.maxWidthProperty().bind(parent.widthProperty().subtract(parent.getPadding().getLeft() + parent.getPadding().getRight()));
	    questionDisplay.getChildren().add(description);
	    
	    // Add question action bar
	    HBox questionActionBar = createQuestionActionBar(parent);
	    questionDisplay.getChildren().addAll(questionActionBar);
		
	}
	
	public void formatQuestionInfo(VBox info) {
		// Add Author
		Label author = new Label("Author: " + databaseHelper.getQuestionUsername(questionId));
		author.setStyle("-fx-font-size: 12px;");
		author.setWrapText(true);
		author.maxWidthProperty().bind(info.widthProperty().subtract(info.getPadding().getLeft() + info.getPadding().getRight()));
		
		info.getChildren().addAll(author);
	}
	
	public VBox formatAnswer(VBox parent, AnswerLightweightDTO answer) {
		VBox answerDisplay = new VBox();
		answerDisplay.setStyle("-fx-alignment: top-left; -fx-padding: 0px;");
		answerDisplay.setSpacing(5);
		
		// Check if answer exists
		if (answer == null) {
			return null;
		}
	    
		// Answer content
	    Label content = new Label(answer.getContent());
	    content.setStyle("-fx-font-size: 16px;");
	    content.setWrapText(true);
	    content.maxWidthProperty().bind(parent.widthProperty().subtract(parent.getPadding().getLeft() + parent.getPadding().getRight()));
	    answerDisplay.getChildren().add(content);
	    
	    // Answer Info
	    VBox info = new VBox();
	    info.setStyle("-fx-alignment: center-left");
	    info.maxWidthProperty().bind(parent.widthProperty().subtract(parent.getPadding().getLeft() + parent.getPadding().getRight()));
	    formatAnswerInfo(info, answer);
	    answerDisplay.getChildren().add(info);
	    
	    // Action bar
	    HBox actionBar = createActionBar(parent, answer);
	    answerDisplay.getChildren().add(actionBar);
		
		return answerDisplay;
	}
	
	public void formatAnswerInfo(VBox info, AnswerLightweightDTO answer) {
		// Add Author
		Label author = new Label("Author: " + databaseHelper.getAnswerUsername(answer.getAnswerId()));
		author.setStyle("-fx-font-size: 12px;");
		author.setWrapText(true);
		author.maxWidthProperty().bind(info.widthProperty().subtract(info.getPadding().getLeft() + info.getPadding().getRight()));
		
		info.getChildren().addAll(author);
	}
	
	public HBox createQuestionActionBar(VBox layout) {
		HBox questionActionBar = new HBox(4);
		
		// Add parent question if there is a parent question
	    int parentQuestionId = question.getParentQuestionId();
	    if (parentQuestionId != -1) {
	    	Button originalQuestionButton = new Button("Go To Original Question");
			originalQuestionButton.setOnAction(a -> {
				new QuestionPage(databaseHelper, searchPage, userId, parentQuestionId).show(primaryStage);
			});
			
	    	questionActionBar.getChildren().add(originalQuestionButton);
	    }
		
		// Add a spacer that will push the following buttons to the right
		Region spacer = new Region();
		HBox.setHgrow(spacer, Priority.ALWAYS); // Forces elements to the right
	    questionActionBar.getChildren().add(spacer);
	    
	    // Right button flags
	    boolean isQuestionAuthor = userId == question.getUserId();
	    
	    if (isQuestionAuthor) {
	    	Button editButton = createQuestionEditButton();
			Button trashButton = createQuestionTrashButton();
			
			questionActionBar.getChildren().addAll(editButton, trashButton);
	    }
	    
    	// Reviews Button
 		Button reviewsButton = new Button("Reviews");
 		reviewsButton.setOnAction(a -> {
 			// Load review page
 			new ReviewPage(databaseHelper, userId, questionId, true).show(primaryStage);
 		});
 		
 		questionActionBar.getChildren().addAll(reviewsButton);
 		
 		Button pmButton = createMessageButton();
 		pmButton.setOnAction(a -> {
 			// Load review page
 			messageClicked("question", questionId, question.getUserId());
 		});
	 	questionActionBar.getChildren().addAll(pmButton);
 		
	    return questionActionBar;
	}
	
	public HBox createActionBar(VBox parent, AnswerLightweightDTO answer) {
		int answerId = answer.getAnswerId();
		
		// Left button flags
		boolean isQuestionAuthor = userId == question.getUserId();
		
		// Create bar
		HBox actionBar = new HBox(4);
		actionBar.setStyle("-fx-alignment: center-left;");
		actionBar.maxWidthProperty().bind(parent.widthProperty().subtract(parent.getPadding().getLeft() + parent.getPadding().getRight()));
		
		// Add star button for question author
		if (isQuestionAuthor) {
			Button starButton = createStarButton(answerId);
			actionBar.getChildren().add(starButton);
		}
		
		// Add upvote and downvote buttons
		Button upvoteButton = createUpvoteButton(answerId);
		Button downvoteButton = createDownvoteButton(answerId);
		
		// Add vote
		int voteSum = databaseHelper.getAnswerVoteCount(answerId);
		Label voteCount = new Label(formatVote(voteSum));
		
		// Add upvote event handler
		upvoteButton.setOnAction(a -> {
			upvoteClicked(answerId, upvoteButton, downvoteButton, voteCount);
		});
		
		// Add downvote even handler
		downvoteButton.setOnAction(a -> {
			downvoteClicked(answerId, upvoteButton, downvoteButton, voteCount);
		});
		
		updateVote(answerId, upvoteButton, downvoteButton, voteCount);
		
		// Add a spacer that will push the following buttons to the right
		Region spacer = new Region();
		HBox.setHgrow(spacer, Priority.ALWAYS); // Forces elements to the right
		
		actionBar.getChildren().addAll(upvoteButton, downvoteButton, voteCount, spacer);
		
		// Editor Section
		
		// Right button flags
		boolean isCreator = userId == answer.getUserId();
		
		if (isCreator) {
			Button editButton = createEditButton(answerId);
			Button trashButton = createTrashButton(answerId);
			
			actionBar.getChildren().addAll(editButton, trashButton);
		}
		
		// Reviews Button
		Button reviewsButton = new Button("Reviews");
		reviewsButton.setOnAction(a -> {
			// Load review page
			new ReviewPage(databaseHelper, userId, answerId, false).show(primaryStage);
		});
		
		Button pmButton = createMessageButton();
 		pmButton.setOnAction(a -> {
 			// Load review page
 			messageClicked("answer", answerId, answer.getUserId());
 		});
		
		actionBar.getChildren().addAll(reviewsButton, pmButton);
		
		return actionBar;
	}
	
	// Create star button
	public Button createStarButton(int answerId) {
		ImageView starImage;
		boolean isStarred = false;
		if (answerId == databaseHelper.getResolvedAnswerId(questionId)) {
			starImage = new ImageView(starFilled);
			isStarred = true;
		} else {
			starImage = new ImageView(starUnfilled);
		}
		starImage.setFitWidth(ICON_SIZE);
		starImage.setFitHeight(ICON_SIZE);
		starImage.setPreserveRatio(true);
		
		// Add star button
		Button starButton = new Button();
		starButton.setGraphic(starImage);
		
		// Store currently starred button
		if (isStarred) {
			currentStarredAnswer = starButton;
		}
		
		// Add star event handler
		starButton.setOnAction(a -> {
			starClicked(answerId, starButton);
		});
		
		return starButton;
	}
		
	// Create up vote button
	public Button createUpvoteButton(int answerId) {
		ImageView upvoteImage = new ImageView(upvoteUnfilled); // defaults to unfilled (will be loaded later)
		upvoteImage.setFitWidth(ICON_SIZE);
		upvoteImage.setFitHeight(ICON_SIZE);
		upvoteImage.setPreserveRatio(true);
		
		// Add up vote button
		Button upvoteButton = new Button();
		upvoteButton.setGraphic(upvoteImage);
		
		return upvoteButton;
	}
	
	// Create down vote button
	public Button createDownvoteButton(int answerId) {
		ImageView downvoteImage = new ImageView(downvoteUnfilled); // defaults to unfilled (will be loaded later)
		downvoteImage.setFitWidth(ICON_SIZE);
		downvoteImage.setFitHeight(ICON_SIZE);
		downvoteImage.setPreserveRatio(true);
		
		// Add down vote button
		Button downvoteButton = new Button();
		downvoteButton.setGraphic(downvoteImage);
		
		return downvoteButton;
	}
	
	// Create edit button
	public Button createEditButton(int answerId) {
		ImageView editImage = new ImageView(editIcon);
		editImage.setFitWidth(ICON_SIZE);
		editImage.setFitHeight(ICON_SIZE);
		editImage.setPreserveRatio(true);
		
		Button editButton = new Button();
		editButton.setGraphic(editImage);
		
		// Add edit event handler
		editButton.setOnAction(a -> {
			editClicked(answerId);
		});
		
		return editButton;
	}
	
	// Create edit button
	public Button createQuestionEditButton() {
		ImageView editImage = new ImageView(editIcon);
		editImage.setFitWidth(ICON_SIZE);
		editImage.setFitHeight(ICON_SIZE);
		editImage.setPreserveRatio(true);
		
		Button editButton = new Button();
		editButton.setGraphic(editImage);
		
		// Add edit event handler
		editButton.setOnAction(a -> {
			editQuestionClicked();
		});
		
		return editButton;
	}
	
	// Create trash button
	public Button createTrashButton(int answerId) {
		ImageView trashImage = new ImageView(trashIcon);
		trashImage.setFitWidth(ICON_SIZE);
		trashImage.setFitHeight(ICON_SIZE);
		trashImage.setPreserveRatio(true);
		
		Button trashButton = new Button();
		trashButton.setGraphic(trashImage);
		
		// Add edit event handler
		trashButton.setOnAction(a -> {
			trashClicked(answerId);
		});
		
		return trashButton;
	}
	
	// Create trash button
	public Button createQuestionTrashButton() {
		ImageView trashImage = new ImageView(trashIcon);
		trashImage.setFitWidth(ICON_SIZE);
		trashImage.setFitHeight(ICON_SIZE);
		trashImage.setPreserveRatio(true);
		
		Button trashButton = new Button();
		trashButton.setGraphic(trashImage);
		
		// Add edit event handler
		trashButton.setOnAction(a -> {
			trashQuestionClicked();
		});
		
		return trashButton;
	}
	
	public Button createMessageButton() {
		ImageView messageImage = new ImageView(messageIcon);
		messageImage.setFitWidth(ICON_SIZE);
		messageImage.setFitHeight(ICON_SIZE);
		messageImage.setPreserveRatio(true);
		
		Button messageButton = new Button();
		messageButton.setGraphic(messageImage);
		// databaseHelper.getUser(userId).getHasUnreadMsgs()
		if (true) {
			messageButton.setTextFill(Color.CORNFLOWERBLUE);
		}
		
		return messageButton;
	}
	
	public void starClicked(int answerId, Button starButton) {
		int resolvedAnswerId = databaseHelper.getResolvedAnswerId(questionId);
		ImageView starImage = (ImageView) starButton.getGraphic();
		
		if (resolvedAnswerId == answerId) {
			// Remove star if currently set to answer
			databaseHelper.removeResolvedAnswerId(questionId);
			starImage.setImage(starUnfilled);
			
			currentStarredAnswer = null;
		} else {
			// Set resolvedAnswerId to clicked answerId
			databaseHelper.setResolvedAnswerId(questionId, answerId);
			starImage.setImage(starFilled);
			
			// If there was previously a resolved answer, remove star image
			if (resolvedAnswerId != -1 && currentStarredAnswer != null) {
				ImageView previousStarImage = (ImageView) currentStarredAnswer.getGraphic();
				previousStarImage.setImage(starUnfilled);
			}
			
			currentStarredAnswer = starButton;
		}
		
		load();
	}
	
	public void upvoteClicked(int answerId, Button upvoteButton, Button downvoteButton, Label voteCount) {
		int userVote = databaseHelper.getUserVoteForAnswer(userId, answerId);
		
		if (userVote == 1) {
			// Remove upvote
			databaseHelper.updateUserVoteForAnswer(userId, answerId, 0);
		} else {
			// Set upvote
			databaseHelper.updateUserVoteForAnswer(userId, answerId, 1);
		}
		
		updateVote(answerId, upvoteButton, downvoteButton, voteCount);
	}
	
	public void downvoteClicked(int answerId, Button upvoteButton, Button downvoteButton, Label voteCount) {
		int userVote = databaseHelper.getUserVoteForAnswer(userId, answerId);
		
		if (userVote == -1) {
			// Remove downvote
			databaseHelper.updateUserVoteForAnswer(userId, answerId, 0);
		} else {
			// Set downvote
			databaseHelper.updateUserVoteForAnswer(userId, answerId, -1);
		}
		
		updateVote(answerId, upvoteButton, downvoteButton, voteCount);
	}
	
	// Open answer in draft editor
	public void editClicked(int answerId) {
		new AnswerDraftPage(databaseHelper, this, userId, questionId, answerId).show(primaryStage);
	}
	
	// Open question in draft editor
	public void editQuestionClicked() {
		new QuestionDraftPage(databaseHelper, searchPage, this, question.getUserId(), question.getParentQuestionId(), questionId).show(primaryStage);
	}
	
	// Delete answer if author confirms
	public void trashClicked(int answerId) {
		if (new ConfirmDelete().show("Are you sure you want to delete this answer?")) {
			databaseHelper.deleteAnswer(answerId);
			load();
		}
	}
	
	// Delete question if author confirms
	public void trashQuestionClicked() {
		if (new ConfirmDelete().show("Are you sure you want to delete this question?")) {
			databaseHelper.deleteQuestion(questionId);
			back();
		}
	}
	
	public void messageClicked(String parentType, int parentId, int authorId) {
		if (authorId == userId) {
			// Author is clicking
			new MessageListPage(databaseHelper, userId, parentType, parentId).show(primaryStage);
		} else {
			// Commenter is clicking
			new PrivateMessagePage(databaseHelper, userId, authorId, userId, parentId, parentType).show(primaryStage);
		}
	}
	
	public void updateVote(int answerId, Button upvoteButton, Button downvoteButton, Label voteCount) {
		int userVote = databaseHelper.getUserVoteForAnswer(userId, answerId);
		
		// Get image views
		ImageView upvotedImage = (ImageView) upvoteButton.getGraphic();
		ImageView downvotedImage = (ImageView) downvoteButton.getGraphic();
		
		// Set button images
		if (userVote == 1) {
			upvotedImage.setImage(upvoteFilled);
			downvotedImage.setImage(downvoteUnfilled);
		} else if (userVote == -1) {
			upvotedImage.setImage(upvoteUnfilled);
			downvotedImage.setImage(downvoteFilled);
		} else {
			upvotedImage.setImage(upvoteUnfilled);
			downvotedImage.setImage(downvoteUnfilled);
		}
		
		// Set vote count
		int voteSum = databaseHelper.getAnswerVoteCount(answerId);
		voteCount.setText(formatVote(voteSum));
		
		// Set color
		if (voteSum > 0) {
			voteCount.setStyle("-fx-text-fill: #00ff00; -fx-font-weight: bold;");
		} else if (voteSum < 0) {
			voteCount.setStyle("-fx-text-fill: #ff0000; -fx-font-weight: bold;");
		} else {
			voteCount.setStyle("-fx-text-fill: #8c8c8c; -fx-font-weight: bold;");
		}
	}
	
	public String formatVote(int voteCount) {
		if (voteCount >= 0) {
			return "+" + voteCount;
		} 
		return "" + voteCount;
	}
	
	// Page Bar
	public HBox createPageBar(VBox parent) {
		HBox pageBar = new HBox(10);
		pageBar.setStyle("-fx-alignment: center-left; -fx-padding: 0 20 20 20;");
		pageBar.maxWidthProperty().bind(parent.widthProperty().subtract(parent.getPadding().getLeft() + parent.getPadding().getRight()));

		boolean canAnswer = databaseHelper.getUserRole(userId).contains("student")
				|| databaseHelper.getUserRole(userId).contains("instructor");
		boolean canFollowUp = databaseHelper.getUserRole(userId).contains("student");
		
		if (canAnswer) {
			// New Answer button
			Button newAnswerButton = new Button("New Answer");
			newAnswerButton.setOnAction(a -> {
				// Load answer draft page
				new AnswerDraftPage(databaseHelper, this, userId, questionId).show(primaryStage);
			});
			pageBar.getChildren().add(newAnswerButton);
		}
		
		if (canFollowUp) {
			Button followUpButton = new Button("Follow up");
			followUpButton.setOnAction(a -> {
				// Load question draft page
				new QuestionDraftPage(databaseHelper, searchPage, userId, questionId).show(primaryStage); // set parent to current question
			});
			pageBar.getChildren().add(followUpButton);
		}
		
		
		// Add a spacer that will push the following buttons to the right
		Region spacer = new Region();
		HBox.setHgrow(spacer, Priority.ALWAYS); // Forces elements to the right
		
		Button backButton = new Button("Back to questions");
		backButton.setOnAction(a -> {
			back();
		});
		
		pageBar.getChildren().addAll(spacer, backButton);
		
		return pageBar;
	}
	
	public void back() {
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
