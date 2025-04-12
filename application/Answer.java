package application;

/**
 * The Answer class represents a answer in the system.
 * It contains the answer details such as user (creator of answer) and content.
 */
public class Answer {
	private int userId;
	private int questionId;
	private String content;
	
	// Constructor to initialize a new Answer object with user and content.
    public Answer(int userId, int questionId, String content) {
    	this.userId = userId;
    	this.questionId = questionId;
    	this.content = content;
    }
    
    // Set content
    public void setContent(String content) {
    	this.content = content;
    }
    
    public int getUserId() { return userId; }
    public int getQuestionId() { return questionId; }
    public String getContent() { return content; }
    
    /**
     * This method validates an Answer object for the requirements needs for the database
     */
    public static String validate(Answer answer) {
    	if (answer == null) {
    		return "*** Error *** Answer cannot be null";
    	}
    	
    	if (answer.content == null || answer.content.trim().isEmpty()) {
    		return "*** Error *** Answer content cannot be empty";
    	}
    	
    	if (answer.content.length() > 65535) { // 65535 is the max set in DatabaseHelper
    		return "*** Error *** Answer content cannot exceed maximum length (65535 characters)";
    	}
    	
    	return ""; // Passes all conditions
    }
}
