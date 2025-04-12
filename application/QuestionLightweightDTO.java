package application;

/**
 * The QuestionLightweightDTO (Data Transfer Object) class represents a 
 * question in the database, but it removes unnecessary data in order to 
 * allow less memory intensive queries. This will specifically be used 
 * for instances where we don't want to get the actual description of the 
 * question, for example, when we want a list of all the question titles 
 * for displaying.
 */
public class QuestionLightweightDTO {
	private int questionId;
	private int userId;
	private String title;
	
	public QuestionLightweightDTO(int questionId, int userId, String title) {
		this.questionId = questionId;
		this.userId = userId;
		this.title = title;
	}
	
	public int getQuestionId() { return questionId; }
	public int getUserId() { return userId; }
	public String getTitle() { return title; }
}
