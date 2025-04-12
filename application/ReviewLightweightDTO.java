package application;

/**
 * The ReviewLightweightDTO (Data Transfer Object) class represents a 
 * review in the database, but it removes unnecessary data in order to 
 * allow less memory intensive queries. 
 */
public class ReviewLightweightDTO {
	private int reviewId; // Stores the reviewId instead of the questionId/answerId like in Review.java because method is called using the questionId/answerId as the parameter
	private int userId;
	private String content;
	
	public ReviewLightweightDTO(int reviewId, int userId, String content) {
		this.reviewId = reviewId;
		this.userId = userId;
		this.content = content;
	}
	
	public int getReviewId() { return reviewId; }
	public int getUserId() { return userId; }
	public String getContent() { return content; }
}
