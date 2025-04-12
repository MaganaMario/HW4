package application;

import java.util.ArrayList;

/**
 * The User class represents a user entity in the system.
 * It contains the user's details such as userName, password, and role.
 */
public class User {
    private String userName;
    private String password;
    private String fullName;
    private String email;
    private ArrayList<String> role;
    private boolean hasUnreadMsgs;

    // Constructor to initialize a new User object with userName, password, and role.
    public User(String userName, String password, String fullName, String email, ArrayList<String> role, boolean hasUnreadMsgs) {
        this.userName = userName;
        this.password = password;
        this.fullName = fullName;
        this.email = email;
        this.role = role;
        this.hasUnreadMsgs = hasUnreadMsgs;
    }
    
    // Sets the role of the user.
    public void setRole(ArrayList<String> role) {
    	this.role=role;
    }

    public String getUserName() { return userName; }
    public String getPassword() { return password; }
    public String getFullName() { return fullName; }
    public String getEmail() { return email; }
    public ArrayList<String> getRole() { return role; }
    public boolean getHasUnreadMsgs() { return hasUnreadMsgs; }
    public String getRoleString() {
    	String roleString = "";
    	for (int i = 0; i < role.size(); i++) {
    		if (i != role.size() - 1)
    			roleString = roleString + role.get(i) + ", ";
    		else 
    			roleString = roleString + role.get(i);
    	}
    	return roleString;
    }
}
