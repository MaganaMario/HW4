package application;

import java.util.List;

import database.DatabaseHelper;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class ReviewPage {
	private DatabaseHelper databaseHelper;
	private int userId;
	private int qaId;
	private boolean forQuestion;
	
	private List<ReviewLightweightDTO> reviews;
	
	// Icons
	private final int ICON_SIZE = 16;
	private Image editIcon = new Image(getClass().getResource("/images/editIcon.png").toExternalForm());
	private Image trashIcon = new Image(getClass().getResource("/images/trashIcon.png").toExternalForm());
	private Image messageIcon = new Image(getClass().getResource("/images/messageIcon.png").toExternalForm());
	
	private VBox scrollLayout;
	private boolean sortByTrusted;
	
	private Stage primaryStage;
	private Scene previousScene;
	private String previousTitle;

	public ReviewPage(DatabaseHelper databaseHelper, int userId, int qaId, boolean forQuestion) {
		this.databaseHelper = databaseHelper;
		this.userId = userId;
		this.qaId = qaId;
		this.forQuestion = forQuestion;
		
		sortByTrusted = false;
	}
	
	public void show(Stage primaryStage) {
		this.primaryStage = primaryStage;
		previousScene = primaryStage.getScene();
		previousTitle = primaryStage.getTitle();
		
		load();
	}
	
	public void load() {
		VBox layout = new VBox();
		layout.setStyle("-fx-alignment: top-center;");
	    layout.setSpacing(10);
		
		scrollLayout = new VBox();
	    scrollLayout.setStyle("-fx-alignment: top-center; -fx-padding: 20;");
	    scrollLayout.setSpacing(10);
	    
	    ScrollPane layoutScrollPane = new ScrollPane();
	    layoutScrollPane.setContent(scrollLayout);
	    layoutScrollPane.setFitToWidth(true);
	    VBox.setVgrow(layoutScrollPane, Priority.ALWAYS);
	    
	    loadReviews();
	    
	    // Add pageBar
	    HBox pageBar = createPageBar(layout);
	    
	    // Set layout
	    layout.getChildren().addAll(layoutScrollPane, pageBar);
	    
		// Set the scene to primary stage
	    Scene reviewPageScene = new Scene(layout, 800, 400);
	    primaryStage.setScene(reviewPageScene);
	    primaryStage.setTitle("Review Page");
	}
	
	// Load the reviews into the scroll layout
	public void loadReviews() {
		scrollLayout.getChildren().clear();
		
		if (sortByTrusted) {
			reviews = databaseHelper.getAllTrustedReviews(qaId, forQuestion, userId);
		} else {
			reviews = databaseHelper.getAllReviews(qaId, forQuestion);
		}
		
		// Format question or answer
	    if (forQuestion) {
	    	scrollLayout.getChildren().add(formatQuestion(scrollLayout));
	    } else {
	    	scrollLayout.getChildren().add(formatAnswer(scrollLayout));
	    }
	    
	    // Add separators and reviews
	    Label reviewLabel = new Label(reviews.size() + " Review" + (reviews.size() == 1 ? "" : "s"));
	    reviewLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
	    scrollLayout.getChildren().addAll(new Separator(), reviewLabel, new Separator());
	    
	    // Format reviews
	    for (int i = 0; i < reviews.size(); i++) {
	    	VBox reviewDisplay = formatReview(scrollLayout, reviews.get(i));
	    	if(reviewDisplay != null) {
	    		scrollLayout.getChildren().addAll(reviewDisplay, new Separator());
	    	}
	    }
	}
	
	public VBox formatQuestion(VBox parent) {
		VBox questionDisplay = new VBox();
	    questionDisplay.setStyle("-fx-alignment: top-left; -fx-padding: 0px;");
		questionDisplay.setSpacing(5);
		
		Question question = databaseHelper.getQuestion(qaId);
		
		// Check if question exists
		if (question == null) {
			Label notFound = new Label("Question not found.");
		    notFound.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
		    questionDisplay.getChildren().add(notFound);
			return questionDisplay;
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
	
	public VBox formatAnswer(VBox parent) {
		VBox answerDisplay = new VBox();
	    answerDisplay.setStyle("-fx-alignment: top-left; -fx-padding: 0px;");
	    answerDisplay.setSpacing(5);
		
		Answer answer = databaseHelper.getAnswer(qaId);
		
		// Check if answer exists
		if (answer == null) {
			Label notFound = new Label("Answer not found.");
		    notFound.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
		    answerDisplay.getChildren().add(notFound);
			return answerDisplay;
		}
	    
	    // Answer Info
	    VBox info = new VBox(10);
	    info.setStyle("-fx-alignment: center-left");
	    info.maxWidthProperty().bind(parent.widthProperty().subtract(parent.getPadding().getLeft() + parent.getPadding().getRight()));
	    formatAnswerInfo(info);
	    answerDisplay.getChildren().add(info);
	    
	    // Add separator
	    answerDisplay.getChildren().add(new Separator());
	    
	    // Content
	    Label content = new Label(answer.getContent());
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
	
	public VBox formatReview(VBox parent, ReviewLightweightDTO review) {
		VBox reviewDisplay = new VBox();
		reviewDisplay.setStyle("-fx-alignment: top-left; -fx-padding: 0px;");
		reviewDisplay.setSpacing(5);
		
		// Check if review exists
		if (review == null) {
			return null;
		}
	    
		// Review content
	    Label content = new Label(review.getContent());
	    content.setStyle("-fx-font-size: 16px;");
	    content.setWrapText(true);
	    content.maxWidthProperty().bind(parent.widthProperty().subtract(parent.getPadding().getLeft() + parent.getPadding().getRight()));
	    reviewDisplay.getChildren().add(content);
	    
	    // Review Info
	    VBox info = new VBox();
	    info.setStyle("-fx-alignment: center-left");
	    info.maxWidthProperty().bind(parent.widthProperty().subtract(parent.getPadding().getLeft() + parent.getPadding().getRight()));
	    formatReviewInfo(info, review);
	    reviewDisplay.getChildren().add(info);
	    
	    // Action bar
	    HBox actionBar = createReviewActionBar(parent, review);
	    reviewDisplay.getChildren().add(actionBar);
		
		return reviewDisplay;
	}
	
	public void formatReviewInfo(VBox info, ReviewLightweightDTO review) {
		// Add Author
		Label author = new Label("Author: " + databaseHelper.getUser(review.getUserId()).getUserName());
		author.setStyle("-fx-font-size: 12px;");
		author.setWrapText(true);
		author.maxWidthProperty().bind(info.widthProperty().subtract(info.getPadding().getLeft() + info.getPadding().getRight()));
		
		info.getChildren().addAll(author);
	}
	
	public HBox createReviewActionBar(VBox parent, ReviewLightweightDTO review) {
		int reviewId = review.getReviewId();
		
		// Create bar
		HBox actionBar = new HBox(4);
		actionBar.setStyle("-fx-alignment: center-left;");
		actionBar.maxWidthProperty().bind(parent.widthProperty().subtract(parent.getPadding().getLeft() + parent.getPadding().getRight()));
		
		// Left button flags
		boolean canSetTrustedReviewer = userId != review.getUserId()
				&& databaseHelper.getUserRole(userId).contains("student");
		
		if (canSetTrustedReviewer) {
			Button setTrustedButton;
			if (!databaseHelper.studentTrustsReviewer(userId, review.getUserId())) {
				// User does not currently trust reviewer
				setTrustedButton = new Button("Trust Reviewer");
				setTrustedButton.setOnAction(a -> {
					// Add trusted reviewer
					addTrustedReviewer(userId, review.getUserId(), setTrustedButton);
					loadReviews();
				});
			} else {
				// User trusts reviewer already
				setTrustedButton = new Button("Untrust Reviewer");
				setTrustedButton.setOnAction(a -> {
					// Delete trusted reviewer
					databaseHelper.deleteTrustedReviewer(userId, review.getUserId());
					loadReviews();
				});
			}
			actionBar.getChildren().add(setTrustedButton);
		}
		
		// Add a spacer that will push the following buttons to the right
		Region spacer = new Region();
		HBox.setHgrow(spacer, Priority.ALWAYS); // Forces elements to the right
		
		actionBar.getChildren().addAll(spacer);
		
		// Editor Section
		
		// Right button flags
		boolean isCreator = userId == review.getUserId();
		
		if (isCreator) {
			Button editButton = createEditButton(reviewId);
			Button trashButton = createTrashButton(reviewId);
			
			actionBar.getChildren().addAll(editButton, trashButton);
		}
		
		Button pmButton = createMessageButton();
 		pmButton.setOnAction(a -> {
 			// Load review page
 			messageClicked("review", reviewId, review.getUserId());
 		});
	 	actionBar.getChildren().add(pmButton);
		
		return actionBar;
	}
	
	// Prompt user for weighted value to apply to reviewer
	private void addTrustedReviewer(int studentId, int reviewerId, Button button) {
		// Create the alert to ask for the weight
        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle("Trust Reviewer");
        alert.setHeaderText("Set Weight for Trusted Reviewer");
        alert.setContentText("Enter the weight value for reviewer ID " + reviewerId);
        
        // Text field
        TextField weightInput = new TextField();
        weightInput.setPromptText("Enter weight (1 to 10)");
        alert.getDialogPane().setContent(weightInput);
        
        alert.showAndWait().ifPresent(response -> {
            try {
                // Try to parse the weight value from the input
                int weight = Integer.parseInt(weightInput.getText());

                if (weight < 1 || weight > 10) {
                    showErrorAlert("Invalid Weight", "The weight must be between 1 and 10.");
                } else {
                    databaseHelper.addTrustedReviewer(studentId, reviewerId, weight); // Add trusted reviewer
                }
            } catch (NumberFormatException e) {
                showErrorAlert("Invalid Input", "Please enter a valid integer for the weight.");
            }
        });
	}
	
	private void showErrorAlert(String title, String message) {
        Alert errorAlert = new Alert(AlertType.ERROR);
        errorAlert.setTitle(title);
        errorAlert.setHeaderText(null);
        errorAlert.setContentText(message);
        errorAlert.showAndWait();
    }
	
	// Create edit button
	public Button createEditButton(int reviewId) {
		ImageView editImage = new ImageView(editIcon);
		editImage.setFitWidth(ICON_SIZE);
		editImage.setFitHeight(ICON_SIZE);
		editImage.setPreserveRatio(true);
		
		Button editButton = new Button();
		editButton.setGraphic(editImage);
		
		// Add edit event handler
		editButton.setOnAction(a -> {
			editClicked(reviewId);
		});
		
		return editButton;
	}
	
	// Create trash button
	public Button createTrashButton(int reviewId) {
		ImageView trashImage = new ImageView(trashIcon);
		trashImage.setFitWidth(ICON_SIZE);
		trashImage.setFitHeight(ICON_SIZE);
		trashImage.setPreserveRatio(true);
		
		Button trashButton = new Button();
		trashButton.setGraphic(trashImage);
		
		// Add edit event handler
		trashButton.setOnAction(a -> {
			trashClicked(reviewId);
		});
		
		return trashButton;
	}
	
	// Open answer in draft editor
	public void editClicked(int reviewId) {
		new ReviewDraftPage(databaseHelper, this, userId, qaId, forQuestion, reviewId).show(primaryStage);
	}
	
	// Delete answer if author confirms
	public void trashClicked(int reviewId) {
		if (new ConfirmDelete().show("Are you sure you want to delete this answer?")) {
			databaseHelper.deleteReview(reviewId);
			load();
		}
	}
	
	// Page Bar
	public HBox createPageBar(VBox parent) {
		HBox pageBar = new HBox(10);
		pageBar.setStyle("-fx-alignment: center-left; -fx-padding: 0 20 20 20;");
		pageBar.maxWidthProperty().bind(parent.widthProperty().subtract(parent.getPadding().getLeft() + parent.getPadding().getRight()));
		
		boolean canReview = databaseHelper.getUserRole(userId).contains("student")
				|| databaseHelper.getUserRole(userId).contains("reviewer");
		boolean canRequestReviewerRole = databaseHelper.getUserRole(userId).contains("student") &&
				!databaseHelper.getUserRole(userId).contains("reviewer");
		boolean canHaveTrustedReviewers = databaseHelper.getUserRole(userId).contains("student");
		
		if (canReview) {
			// New Review button
			Button newReviewButton = new Button("New Review");
			newReviewButton.setOnAction(a -> {
				// Load review draft page
				new ReviewDraftPage(databaseHelper, this, userId, qaId, forQuestion).show(primaryStage);
			});
			pageBar.getChildren().add(newReviewButton);
		}
		
		if (canRequestReviewerRole) {
			// Request Review Role button
			Button requestReviewRoleButton = new Button("Request Reviewer Role");
			if (databaseHelper.hasRequestedRole(userId, "reviewer")) {
				requestReviewRoleButton.setDisable(true);
				requestReviewRoleButton.setText("Role Requested: pending");
			} else {
				requestReviewRoleButton.setOnAction(a -> {
					databaseHelper.addRoleRequest(userId, "reviewer");
					
					requestReviewRoleButton.setDisable(true);
					requestReviewRoleButton.setText("Role Requested: pending");
					
					Alert alert = new Alert(Alert.AlertType.INFORMATION);
					alert.setTitle("Request Submitted");
					alert.setHeaderText(null);
					alert.setContentText("Your request to become a reviewer has been submitted for approval.");
					alert.showAndWait();
				});
			}
			
			pageBar.getChildren().add(requestReviewRoleButton);
		}
		
		if (canHaveTrustedReviewers) {
			// Sort by trusted toggle button
			Button trustButton = new Button("Sort by Trusted");
			trustButton.setOnAction(a -> {
				sortByTrusted = !sortByTrusted; // Toggle
				
				// Fix button text
				if (sortByTrusted) {
					trustButton.setText("All Reviews");
				} else {
					trustButton.setText("Sort by Trusted");
				}
				
				loadReviews(); // reload
			});
			pageBar.getChildren().add(trustButton);
		}
		
		// Add a spacer that will push the following buttons to the right
		Region spacer = new Region();
		HBox.setHgrow(spacer, Priority.ALWAYS); // Forces elements to the right
		
		Button backButton = new Button("Back");
		backButton.setOnAction(a -> {
			back();
		});
		
		pageBar.getChildren().addAll(spacer, backButton);
		
		return pageBar;
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
	
	public void messageClicked(String parentType, int parentId, int authorId) {
		if (authorId == userId) {
			// Author is clicking
			new MessageListPage(databaseHelper, userId, parentType, parentId).show(primaryStage);
		} else {
			// Commenter is clicking
			new PrivateMessagePage(databaseHelper, userId, authorId, userId, parentId, parentType).show(primaryStage);
		}
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
	}
}
