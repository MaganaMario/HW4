package application;

/**
 * The PrivateMessageLightweightDTO (Data Transfer Object) class represents a 
 * message in the database, but it removes unnecessary data in order to 
 * allow less memory intensive queries. 
 */
public class PrivateMessageLightweightDTO {
	private int messageId; // Stores the messageId instead of the senderId and recipientId like in Message.java because method is called using the senderId and recipientId as the parameters
	private String content;
	private boolean byAuthor;
	
	public PrivateMessageLightweightDTO(int messageId, String content, boolean byAuthor) {
		this.messageId = messageId;
		this.content = content;
		this.byAuthor = byAuthor;
	}
	
	public int getMessageId() { return messageId; }
	public String getContent() { return content; }
	public boolean isByAuthor() { return byAuthor; }
}
