package application;

/**
 * The AnswerLightweightDTO (Data Transfer Object) class represents a 
 * answer in the database, but it removes unnecessary data in order to 
 * allow less memory intensive queries. 
 */
public class AnswerLightweightDTO {
	private int answerId; // Stores the answerId instead of the questionId like in answer.java because method is called using the questionId as the parameter
	private int userId;
	private String content;
	
	public AnswerLightweightDTO(int answerId, int userId, String content) {
		this.answerId = answerId;
		this.userId = userId;
		this.content = content;
	}
	
	public int getAnswerId() { return answerId; }
	public int getUserId() { return userId; }
	public String getContent() { return content; }
}
