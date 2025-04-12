package application;

public class PrivateMessage {
	
	private int authorId;
	private int commenterId;
	private boolean isAuthor;
	private String content;
	private boolean isRead;
	private String parentType;
	private int parentId;
	
	public PrivateMessage(int authorId, int commenterId, boolean isAuthor, String content, boolean isRead, String parentType, int parentId) {
		this.authorId = authorId;
		this.commenterId = commenterId;
		this.isAuthor = isAuthor;
		this.content = content;
		this.isRead = isRead;
		this.parentType = parentType;
		this.parentId = parentId;
	}
	
	public int getAuthorId() { return authorId; }
	public int getCommenterId() { return commenterId; }
	public boolean getIsAuthor() { return isAuthor; }
	public String getContent() { return content; }
	public boolean getIsRead() { return isRead; }
	public String getParentType() { return parentType; }
	public int getParentId() { return parentId; }
	
	
	public void setContent(String content) {
    	this.content = content;
    }
	
	public void setIsRead() {
		this.isRead = true;
	}
	
	public static String validate(PrivateMessage pm) {
		if (pm == null) {
			return "*** Error *** Message cannot be null";
		}
		if (pm.parentType != "question" && pm.parentType != "answer") {
			return "*** Error *** Message cannot be attached to non-Q&A object";
		}
		if (pm.authorId == pm.commenterId) {
			return "*** Error *** Message cannot be sent by a user to themselves";
		}
		if (pm.content == null || pm.content.trim().isEmpty()) {
    		return "*** Error *** Message content cannot be empty";
    	}
    	
    	if (pm.content.length() > 65535) { // Maybe constrain this length a little more?
    		return "*** Error *** Message content cannot exceed maximum length (65535 characters)";
    	}	
    	return "";
    }
	
	
	
	
}
