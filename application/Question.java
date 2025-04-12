package application;

/**
 * The Question class represents a question in the system.
 * It contains the question details such as user (creator of question), title, and description.
 */
public class Question {
	private int userId;
	private String title;
	private String description;
	private int parentQuestionId;
	
	
	// Constructor to initialize a new Question object with user, title, and description.
    public Question(int userId, String title, String description, int parentQuestionId) {
    	this.userId = userId;
    	this.title = title;
    	this.description = description;
    	this.parentQuestionId = parentQuestionId;
    }
    
    // Set title
    public void setTitle(String title) {
    	this.title = title;
    }
    
    // Set description
    public void setDescription(String description) {
    	this.description = description;
    }
    
    public int getUserId() { return userId; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public int getParentQuestionId() { return parentQuestionId; }
    
    /**
     * This method validates an Question object for the requirements needs for the database
     */
    public static String validate(Question question) {
    	if (question == null) {
    		return "*** Error *** Question cannot be null";
    	}
    	
    	if (question.title == null || question.title.trim().isEmpty()) {
    		return "*** Error *** Question title cannot be empty";
    	}
    	
    	if (question.title.length() > 255) { // 255 is the max set in DatabaseHelper
    		return "*** Error *** Question title cannot exceed maximum length (255 characters)";
    	}
    	
    	if (question.description == null || question.description.trim().isEmpty()) {
    		return "*** Error *** Question description cannot be empty";
    	}
    	
    	if (question.description.length() > 65535) { // 65535 is the max set in DatabaseHelper
    		return "*** Error *** Question description cannot exceed maximum length (65535 characters)";
    	}
    	
    	return ""; // Passes all conditions
    }
}
