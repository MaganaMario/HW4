package application;

public class Review {
	private int userId;
	private Integer questionId;
	private Integer answerId;
	private String content;
	
	public Review(int userId, Integer questionId, Integer answerId, String content) {
		this.userId = userId;
		this.questionId = questionId;
		this.answerId = answerId;
		this.content = content;
	}
	
	// Set content
    public void setContent(String content) {
    	this.content = content;
    }
    
    public int getUserId() { return userId; }
    public Integer getQuestionId() { return questionId; }
    public Integer getAnswerId() { return answerId; }
    public String getContent() { return content; }
    
    /**
     * This method validates a Review object for the requirements needs for the database
     */
    public static String validate(Review review) {
    	if (review == null) {
    		return "*** Error *** Review cannot be null";
    	}
    	
    	// Check question and answer
    	if (review.getQuestionId() == null && review.getAnswerId() == null) {
    		return "*** Error *** Review must have a question or answer id";
    	}
    	
    	// Content
    	if (review.content == null || review.content.trim().isEmpty()) {
    		return "*** Error *** Review content cannot be empty";
    	}
    	
    	if (review.content.length() > 65535) { // 65535 is the max set in DatabaseHelper
    		return "*** Error *** Review content cannot exceed maximum length (65535 characters)";
    	}
    	
    	return ""; // Passes all conditions
    }
}
