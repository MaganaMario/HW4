package database;
import java.sql.*;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.io.File;

import application.Answer;
import application.Question;
import application.QuestionLightweightDTO;
import application.Review;
import application.ReviewLightweightDTO;
import application.AnswerLightweightDTO;
import application.PrivateMessage;
import application.PrivateMessageLightweightDTO;
import application.User;
import application.UserLightweightDTO;


/**
 * The DatabaseHelper class is responsible for managing the connection to the database,
 * performing operations such as user registration, login validation, and handling invitation codes.
 */
public class DatabaseHelper {

	// JDBC driver name and database URL 
	static final String JDBC_DRIVER = "org.h2.Driver";   
	static final String DB_URL = "jdbc:h2:~/FoundationDatabase";  

	//  Database credentials 
	static final String USER = "sa"; 
	static final String PASS = ""; 

	private Connection connection = null;
	private Statement statement = null; 
	//	PreparedStatement pstmt

	public void connectToDatabase() throws SQLException {
		try {
			Class.forName(JDBC_DRIVER); // Load the JDBC driver
			System.out.println("Connecting to database...");
			connection = DriverManager.getConnection(DB_URL, USER, PASS);
			statement = connection.createStatement(); 
			// You can use this command to clear the database and restart from fresh.
			// statement.execute("DROP ALL OBJECTS");

			createTables();  // Create the necessary tables if they don't exist
		} catch (ClassNotFoundException e) {
			System.err.println("JDBC Driver not found: " + e.getMessage());
		}
	}

	private void createTables() throws SQLException {
		
		// Create the user table
		String userTable = "CREATE TABLE IF NOT EXISTS cse360users ("
				+ "id INT AUTO_INCREMENT PRIMARY KEY, "
				+ "userName VARCHAR(255) UNIQUE, "
				+ "password VARCHAR(255), "
				+ "fullName VARCHAR(255), "
				+ "email VARCHAR(320), "
				+ "role VARCHAR(255), "
				+ "hasUnreadMsgs BOOLEAN DEFAULT FALSE)";
		statement.execute(userTable);
		
		// Create the invitation codes table
	    String invitationCodesTable = "CREATE TABLE IF NOT EXISTS InvitationCodes ("
	            + "code VARCHAR(10) PRIMARY KEY, "
	            + "isUsed BOOLEAN DEFAULT FALSE)";
	    statement.execute(invitationCodesTable);
	    
	    // Create the questions table
	    String questionTable = "CREATE TABLE IF NOT EXISTS questions ("
	    		+ "id INT AUTO_INCREMENT PRIMARY KEY, "
	    		+ "userId INT, "
	    		+ "title VARCHAR(255), "
	    		+ "description VARCHAR(65535), "
	    		+ "parentQuestionId INT DEFAULT -1, "
	    		+ "authorUnreadCount INT DEFAULT 0, "
	    		+ "resolved BOOLEAN DEFAULT FALSE, "
	    		+ "resolvedAnswerId INT DEFAULT NULL, "
	    		+ "FOREIGN KEY (userId) REFERENCES cse360users(id))";
	    statement.execute(questionTable);
	    
	    // Create the answers table
	    String answerTable = "CREATE TABLE IF NOT EXISTS answers ("
	    		+ "id INT AUTO_INCREMENT PRIMARY KEY, "
	    		+ "userId INT, "
	    		+ "questionId INT, "
	    		+ "content VARCHAR(65535), "
	    		+ "FOREIGN KEY (userId) REFERENCES cse360users(id), "
	    		+ "FOREIGN KEY (questionId) REFERENCES questions(id))";
	    statement.execute(answerTable);
	    
	    // Create the vote table
	    String voteTable = "CREATE TABLE IF NOT EXISTS votes ("
	    		+ "id INT AUTO_INCREMENT PRIMARY KEY, "
	    		+ "userId INT, "
	    		+ "answerId INT, "
	    		+ "voteType INT NOT NULL, " // 1 = upvote, 0 = no vote, -1 = downvote
	    		+ "FOREIGN KEY (userId) REFERENCES cse360users(id), "
	    		+ "FOREIGN KEY (answerId) REFERENCES answers(id))";
	    statement.execute(voteTable);
	    
	    // Create the reviews table
	    String reviewTable = "CREATE TABLE IF NOT EXISTS reviews ("
	    		+ "id INT AUTO_INCREMENT PRIMARY KEY, "
	    		+ "userId INT, "
	    		+ "questionId INT NULL, "
	    		+ "answerId INT NULL, "
	    		+ "content VARCHAR(65535), "
	    		+ "FOREIGN KEY (userId) REFERENCES cse360users(id) ON DELETE CASCADE, "
	    		+ "FOREIGN KEY (questionId) REFERENCES questions(id) ON DELETE CASCADE, "
	    		+ "FOREIGN KEY (answerId) REFERENCES answers(id) ON DELETE CASCADE)";
	    statement.execute(reviewTable);
	    
	    // Create the messages table
	    String messageTable = "CREATE TABLE IF NOT EXISTS messages ("
	    		+ "id INT AUTO_INCREMENT PRIMARY KEY, "
	    		+ "authorId INT NOT NULL, "
	    		+ "commenterId INT NOT NULL, "
	    		+ "isAuthor BOOLEAN, "
	    		+ "content VARCHAR(65535), "
	    		+ "isRead BOOLEAN DEFAULT FALSE, "
	    		+ "parentType VARCHAR(50), "
	    		+ "parentId INT NOT NULL, "
	    		+ "FOREIGN KEY (authorId) REFERENCES cse360users(id) ON DELETE CASCADE, "
	    		+ "FOREIGN KEY (commenterId) REFERENCES cse360users(id) ON DELETE CASCADE)";
	    statement.execute(messageTable);
	        
	    // Create role request table (delete entry if approved or denied)
	    String roleRequestTable = "CREATE TABLE IF NOT EXISTS roleRequests ("
	    		+ "id INT AUTO_INCREMENT PRIMARY KEY, "
	    		+ "userId INT NOT NULL, "
	    		+ "role VARCHAR(255) NOT NULL)";
	    statement.execute(roleRequestTable);
	    
	    // Create the trusted reviewers table
	    String trustedReviewersTable = "CREATE TABLE IF NOT EXISTS trustedReviewers ("
	    		+ "studentId INT NOT NULL, "
	    		+ "reviewerId INT NOT NULL, "
	    		+ "weight INT NOT NULL, "
	    		+ "PRIMARY KEY (studentId, reviewerId), "
	    		+ "FOREIGN KEY (studentId) REFERENCES cse360users(id) ON DELETE CASCADE, "
	    		+ "FOREIGN KEY (reviewerId) REFERENCES cse360users(id) ON DELETE CASCADE)";
	    statement.execute(trustedReviewersTable);
	}


	// Check if the database is empty
	public boolean isDatabaseEmpty() throws SQLException {
		String query = "SELECT COUNT(*) AS count FROM cse360users";
		ResultSet resultSet = statement.executeQuery(query);
		if (resultSet.next()) {
			return resultSet.getInt("count") == 0;
		}
		return true;
	}

	// Registers a new user in the database. Returns userId
	public int register(User user) throws SQLException {
		String insertUser = "INSERT INTO cse360users (userName, password, fullName, email, role, hasUnreadMsgs) VALUES (?, ? , ?, ?, ?, ?)";
		try (PreparedStatement pstmt = connection.prepareStatement(insertUser, Statement.RETURN_GENERATED_KEYS)) {
			pstmt.setString(1, user.getUserName());
			pstmt.setString(2, user.getPassword());
			pstmt.setString(3, user.getFullName());
			pstmt.setString(4, user.getEmail());
			pstmt.setString(5, user.getRoleString());
			pstmt.setBoolean(6, user.getHasUnreadMsgs());
			pstmt.executeUpdate();
			
			// Get generated id
			try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
				if (generatedKeys.next()) {
					return generatedKeys.getInt(1);
				} else {
					throw new SQLException("No User ID obtainted.");
				}
			}
		}
	}

	// Validates a user's login credentials. Return userId or -1 if user not found
	public int login(User user) throws SQLException {
		String query = "SELECT * FROM cse360users WHERE userName = ? AND password = ?";
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setString(1, user.getUserName());
			pstmt.setString(2, user.getPassword());
			try (ResultSet rs = pstmt.executeQuery()) {
				if (rs.next()) {
					return rs.getInt("id");
				} else {
					return -1;
				}
			}
		}
	}
	
	// Checks if a user already exists in the database based on their userName.
	public boolean doesUserExist(String userName) {
	    String query = "SELECT COUNT(*) FROM cse360users WHERE userName = ?";
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        
	        pstmt.setString(1, userName);
	        ResultSet rs = pstmt.executeQuery();
	        
	        if (rs.next()) {
	            // If the count is greater than 0, the user exists
	            return rs.getInt(1) > 0;
	        }
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	    return false; // If an error occurs, assume user doesn't exist
	}
	
	// Gathers existing user's data and creates a list
	public List<User> getAllUsersList() {
        ArrayList<User> userList = new ArrayList<>();
        String query = "SELECT userName, password, fullName, email, role FROM cse360users";

        try (PreparedStatement pstmt = connection.prepareStatement(query);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                String username = rs.getString("userName");
                String password = rs.getString("password");
                String fullName = rs.getString("fullName");
                String email = rs.getString("email");
                String role = rs.getString("role");
                boolean hasUnreadMsgs = rs.getBoolean("hasUnreadMsgs");
                ArrayList<String> roleList = new ArrayList<String>();
                roleList.add(role);
                userList.add(new User(username, password, fullName, email, roleList, hasUnreadMsgs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return userList;
    }
	
	// Retrieves the roles of a user from the database using their UserName.
	public ArrayList<String> getUserRole(String userName) {
	    String query = "SELECT role FROM cse360users WHERE userName = ?";
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setString(1, userName);
	        ResultSet rs = pstmt.executeQuery();
	        
	        if (rs.next()) {
	            String rolesString = rs.getString("role"); // Get the roles if user exists
	            return new ArrayList<String>(List.of(rolesString.split(", "))); // Split by commas 
	        }
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	    return null; // If no user exists or an error occurs
	}
	
	// Retrieves the roles of a user from the database using their id.
	public ArrayList<String> getUserRole(int id) {
	    String query = "SELECT role FROM cse360users WHERE id = ?";
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setInt(1, id);
	        ResultSet rs = pstmt.executeQuery();
	        
	        if (rs.next()) {
	            String rolesString = rs.getString("role"); // Get the roles if user exists
	            return new ArrayList<String>(List.of(rolesString.split(", "))); // Split by commas 
	        }
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	    return null; // If no user exists or an error occurs
	}
	
	// Retrieves the lightweight version of all questions in the database and creates a list
	public List<QuestionLightweightDTO> getAllQuestions(String filter) {
		ArrayList<QuestionLightweightDTO> questionList = new ArrayList<>();
		String query = "SELECT id, userId, title FROM questions " + filter;
		
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			ResultSet rs = pstmt.executeQuery();
			
			while (rs.next()) {
				int id = rs.getInt("id");
				int userId = rs.getInt("userId");
				String title = rs.getString("title");
				questionList.add(new QuestionLightweightDTO(id, userId, title));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return questionList;
	}
	
	// Retrieves all answers in the database for a specific question and create a list
	public List<AnswerLightweightDTO> getAllAnswers(int questionId) {
		ArrayList<AnswerLightweightDTO> answerList = new ArrayList<>();
		String query = "SELECT id, userId, content FROM answers WHERE questionId = ?";
		
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setInt(1, questionId);
			ResultSet rs = pstmt.executeQuery();
			
			while (rs.next()) {
				int id = rs.getInt("id");
				int userId = rs.getInt("userId");
				String content = rs.getString("content");
				answerList.add(new AnswerLightweightDTO(id, userId, content));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return answerList;
	}
	
	public AnswerLightweightDTO getAnswerDTO(int answerId) {
		String query = "SELECT id, userId, content FROM answers WHERE id = ?";
		
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setInt(1, answerId);
			ResultSet rs = pstmt.executeQuery();
			
			if (rs.next()) {
				int id = rs.getInt("id");
				int userId = rs.getInt("userId");
				String content = rs.getString("content");
				return new AnswerLightweightDTO(id, userId, content);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	// Generates a new invitation code and inserts it into the database.
	public String generateInvitationCode() {
	    String code = UUID.randomUUID().toString().substring(0, 4); // Generate a random 4-character code
	    String query = "INSERT INTO InvitationCodes (code) VALUES (?)";

	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setString(1, code);
	        pstmt.executeUpdate();
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }

	    return code;
	}
	
	// Validates an invitation code to check if it is unused.
	public boolean validateInvitationCode(String code) {
	    String query = "SELECT * FROM InvitationCodes WHERE code = ? AND isUsed = FALSE";
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setString(1, code);
	        ResultSet rs = pstmt.executeQuery();
	        if (rs.next()) {
	            // Mark the code as used
	            markInvitationCodeAsUsed(code);
	            return true;
	        }
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	    return false;
	}
	
	// Marks the invitation code as used in the database.
	private void markInvitationCodeAsUsed(String code) {
	    String query = "UPDATE InvitationCodes SET isUsed = TRUE WHERE code = ?";
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setString(1, code);
	        pstmt.executeUpdate();
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	}
	
	// Search for questions through keywords
	private static final List<String> COMMON_WORDS = Arrays.asList("the", "and", "is", "of", "to", "in", "that", "it", "for");

	public static List<QuestionLightweightDTO> searchQuestions(String query, String filter, String sort) {
	    List<QuestionLightweightDTO> results = new ArrayList<>();
	    String[] keywords = query.toLowerCase().split("\\s+");
	    List<String> filteredKeywords = new ArrayList<>();
	    
	    for (String keyword : keywords) {
	        if (!COMMON_WORDS.contains(keyword)) {
	            filteredKeywords.add(keyword);
	        }
	    }
	    
	    if (filteredKeywords.isEmpty()) {
	        return results; // Return empty if only common words were entered
	    }
	    
	    StringBuilder sql = new StringBuilder("SELECT DISTINCT id, userId, title FROM questions WHERE (");
	    
	    for (int i = 0; i < filteredKeywords.size(); i++) {
	        sql.append("LOWER(title) LIKE ?");
	        if (i < filteredKeywords.size() - 1) {
	            sql.append(" OR ");
	        }
	    }
	    sql.append(") ");
	    
	    // Add filter and sort
	    if (filter != "") {
	    	sql.append("AND " + filter + " ");
	    }
	    sql.append(sort);
	    
	    try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
	         PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
	        
	        for (int i = 0; i < filteredKeywords.size(); i++) {
	            stmt.setString(i + 1, "%" + filteredKeywords.get(i) + "%"); 
	        }
	        
	        ResultSet rs = stmt.executeQuery();
	        while (rs.next()) {
	            int id = rs.getInt("id");
	            int userId = rs.getInt("userId");
	            String title = rs.getString("title");
	            results.add(new QuestionLightweightDTO(id, userId, title));
	        }
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	    return results;
	}
	
	// Adds a question to the database
	public void addQuestion(Question question) {
		addQuestion(question.getUserId(), question.getTitle(), question.getDescription(), question.getParentQuestionId());
	}
	
	// Adds a answer to the database
	public void addAnswer(Answer answer) {
		addAnswer(answer.getUserId(), answer.getQuestionId(), answer.getContent());
	}
	
	// Get a user from the database by its ID
	public UserLightweightDTO getUser(int id) {
		UserLightweightDTO user = null; // Returns null user if not found
		String query = "SELECT * FROM cse360users WHERE id = ?";
		
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setInt(1, id);
			ResultSet rs = pstmt.executeQuery();
			
			if (rs.next()) {
				String userName = rs.getString("userName");
				String fullName = rs.getString("fullName");
				String email = rs.getString("email");
				String role = rs.getString("role");
				boolean hasUnreadMsgs = rs.getBoolean("hasUnreadMsgs");
				ArrayList<String> roles = new ArrayList<>(Arrays.asList(role.split(", ")));
				
				user = new UserLightweightDTO(userName, fullName, email, roles, hasUnreadMsgs);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return user;
	}
	
	// Get a question from the database by its ID
	public Question getQuestion(int id) {
		Question question = null; // Returns null question if not found
		String query = "SELECT * FROM questions WHERE id = ?";
		
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setInt(1, id);
			ResultSet rs = pstmt.executeQuery();
			
			if (rs.next()) {
				int userId = rs.getInt("userId");
				String title = rs.getString("title");
				String description = rs.getString("description");
				int parentQuestionId = rs.getInt("parentQuestionId");
				
				question = new Question(userId, title, description, parentQuestionId);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return question;
	}
	
	// Get an answer from the database by its ID
	public Answer getAnswer(int id) {
		Answer answer = null; // Returns null answer if not found
		String query = "SELECT * FROM answers WHERE id = ?";
		
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setInt(1, id);
			ResultSet rs = pstmt.executeQuery();
			
			if (rs.next()) {
				int userId = rs.getInt("userId");
				int questionId = rs.getInt("questionId");
				String content = rs.getString("content");
				
				answer = new Answer(userId, questionId, content);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return answer;
	}
	
	// Adds a question to the database
	public void addQuestion(int userId, String title, String description, int parentQuestionId) {
		String insertQuestion = "INSERT INTO questions (userId, title, description, parentQuestionId) VALUES (?, ? , ?, ?)";
		try (PreparedStatement pstmt = connection.prepareStatement(insertQuestion)) {
			pstmt.setInt(1, userId);
			pstmt.setString(2, title);
			pstmt.setString(3, description);
			pstmt.setInt(4, parentQuestionId);
			pstmt.executeUpdate();
		} catch (SQLException e) {
	        e.printStackTrace();
	    }
	}
	
	// Adds a answer to the database
	public void addAnswer(int userId, int questionId, String content) {
		String insertAnswer = "INSERT INTO answers (userId, questionId, content) VALUES (?, ? , ?)";
		try (PreparedStatement pstmt = connection.prepareStatement(insertAnswer)) {
			pstmt.setInt(1, userId);
			pstmt.setInt(2, questionId);
			pstmt.setString(3, content);
			pstmt.executeUpdate();
			
			// Increment unread count if userId is not the author
			if (userId != getQuestion(questionId).getUserId()) {
				incrementQuestionUnreadCount(questionId);
			}
		} catch (SQLException e) {
	        e.printStackTrace();
	    }
	}
	
	// Get title of question
	public String getQuestionTitle(int questionId) {
		String query = "SELECT title FROM questions WHERE id = ?";
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setInt(1, questionId);
			ResultSet rs = pstmt.executeQuery();
			
			if (rs.next()) { // Check is question exists
				return rs.getString("title");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return ""; // Return empty string if not found
	}
	
	// Get description of question
	public String getQuestionDescription(int questionId) {
		String query = "SELECT description FROM questions WHERE id = ?";
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setInt(1, questionId);
			ResultSet rs = pstmt.executeQuery();
			
			if (rs.next()) { // Check is question exists
				return rs.getString("description");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return ""; // Return empty string if not found
	}
	
	public int getQuestionParentId(int questionId) {
		String query = "SELECT parentQuestionId FROM questions WHERE id = ?";
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setInt(1, questionId);
			ResultSet rs = pstmt.executeQuery();
			
			if (rs.next()) { // Check is question exists
				return rs.getInt("parentQuestionId");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return -1; // Return -1 if no parent question
	}
	
	// Get userName linked to question
	public String getQuestionUsername(int questionId) {
		String query = "SELECT u.userName FROM questions q JOIN cse360users u ON q.userId = u.id WHERE q.id = ?";
		
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setInt(1, questionId);
			ResultSet rs = pstmt.executeQuery();
			
			if (rs.next()) { // Check if there is a user for that question
				return rs.getString("userName");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return ""; // Return empty string if not found
	}
	
	// Get author unread potential answer count
	public int getQuestionUnreadCount(int questionId) {
		String query = "SELECT authorUnreadCount FROM questions WHERE id = ?";
		
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setInt(1, questionId);
			ResultSet rs = pstmt.executeQuery();
			
			if (rs.next()) {
				return rs.getInt("authorUnreadCount");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return 0;
	}
	
	// Increment author unread potential answer count
	public void incrementQuestionUnreadCount(int questionId) {
		String query = "UPDATE questions SET authorUnreadCount = authorUnreadCount + 1 WHERE id = ?";
		
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setInt(1, questionId);
			pstmt.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	// Clear unread potential answer count
	public void resetQuestionUnreadCount(int questionId) {
		String query = "UPDATE questions SET authorUnreadCount = 0 WHERE id = ?";
		
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setInt(1, questionId);
			pstmt.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	// Get answer content
	public String getAnswerContent(int id) {
		String query = "SELECT content FROM answers WHERE id = ?";
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setInt(1, id);
			ResultSet rs = pstmt.executeQuery();
			
			if (rs.next()) { // Check is question exists
				return rs.getString("content");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return ""; // Return empty string if not found
	}
	
	// Get userName linked to answer
	public String getAnswerUsername(int id) {
		String query = "SELECT u.userName FROM answers a JOIN cse360users u ON a.userId = u.id WHERE a.id = ?";
		
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setInt(1, id);
			ResultSet rs = pstmt.executeQuery();
			
			if (rs.next()) { // Check if there is a user for that question
				return rs.getString("userName");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return ""; // Return empty string if not found
	}
	
	// Update question title
	public void updateQuestionTitle(int id, String newTitle) {
		String query = "UPDATE questions SET title = ? WHERE id = ?";
		
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setString(1, newTitle);
			pstmt.setInt(2, id);
			int updatedQuestions = pstmt.executeUpdate(); // Number of questions updated (should be either 0 or 1, but will check for any number changed)
			
			if (updatedQuestions < 1) {
				System.out.println("*** Error *** Question not found.");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	// Update question description
	public void updateQuestionDescription(int id, String newDescription) {
		String query = "UPDATE questions SET description = ? WHERE id = ?";
		
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setString(1, newDescription);
			pstmt.setInt(2, id);
			int updatedQuestions = pstmt.executeUpdate(); // Number of questions updated (should be either 0 or 1, but will check for any number changed)
			
			if (updatedQuestions < 1) {
				System.out.println("*** Error *** Question not found.");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	// Update answer content
	public void updateAnswerContent(int id, String newContent) {
		String query = "UPDATE answers SET content = ? WHERE id = ?";
		
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setString(1, newContent);
			pstmt.setInt(2, id);
			int updatedQuestions = pstmt.executeUpdate(); // Number of answers updated (should be either 0 or 1, but will check for any number changed)
			
			if (updatedQuestions < 1) {
				System.out.println("*** Error *** Answer not found.");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	// Delete question from database
	public void deleteQuestion(int questionId) {
		// Delete all associated answers
		deleteAnswersForQuestion(questionId);
		
		String query = "DELETE FROM questions WHERE id = ?";
		
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setInt(1, questionId);
			int questionsDeleted = pstmt.executeUpdate(); // Number of questions deleted (should be either 0 or 1, but will check for any number changed)
			
			if (questionsDeleted < 1) {
				System.out.println("*** Error *** Question ID not found.");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	// Delete votes for answer
	public void deleteAnswersForQuestion(int questionId) {
		String query = "SELECT id FROM answers WHERE questionId = ?";
		
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setInt(1, questionId);
			ResultSet rs = pstmt.executeQuery();
			
			while (rs.next()) {
				int answerId = rs.getInt("id");
				deleteAnswer(answerId);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	// Delete answer from database
	public void deleteAnswer(int answerId) {
		// Delete associated votes
		deleteVotesForAnswer(answerId);
		
		// Delete associated resolves
		deleteAssociatedResolvesForAnswer(answerId);
		
		String query = "DELETE FROM answers WHERE id = ?";
		
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setInt(1, answerId);
			int answersDeleted = pstmt.executeUpdate(); // Number of answers deleted (should be either 0 or 1, but will check for any number changed)
			
			if (answersDeleted < 1) {
				System.out.println("*** Error *** Answer ID not found.");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	// Count votes
	public int getAnswerVoteCount(int answerId) {
		String query = "SELECT SUM(voteType) FROM votes WHERE answerId = ?";
		
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setInt(1, answerId);
			
			try (ResultSet rs = pstmt.executeQuery()) {
				if (rs.next()) {
					return rs.getInt(1);
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return 0; // No votes found
	}
	
	// Get user's vote for answer
	public int getUserVoteForAnswer(int userId, int answerId) {
		String query = "SELECT voteType FROM votes WHERE userId = ? AND answerId = ?";
		
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setInt(1, userId);
			pstmt.setInt(2, answerId);
			ResultSet rs = pstmt.executeQuery();
			
			if (rs.next()) {
				return rs.getInt("voteType");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return 0;
	}
	
	// Update user's vote for answer
	public void updateUserVoteForAnswer(int userId, int answerId, int vote) {
		String query = "MERGE INTO votes (userId, answerId, voteType) KEY (userId, answerId) VALUES (?, ?, ?)";
		
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setInt(1, userId);
			pstmt.setInt(2, answerId);
			pstmt.setInt(3, vote);
			pstmt.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	// Delete votes for answer
	public void deleteVotesForAnswer(int answerId) {
		String query = "DELETE FROM votes WHERE answerId = ?";
		
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setInt(1, answerId);
			pstmt.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	// Add review to question or answer
	public void addReview(Review review) {
		addReview(review.getUserId(), review.getQuestionId(), review.getAnswerId(), review.getContent());
	}
	
	public void addReview(int userId, Integer questionId, Integer answerId, String content) {
		String insertReview = "INSERT INTO reviews (userId, questionId, answerId, content) VALUES (?, ? , ?, ?)";
		try (PreparedStatement pstmt = connection.prepareStatement(insertReview)) {
			pstmt.setInt(1, userId);
			if (questionId != null) {
				pstmt.setInt(2, questionId);
			} else {
				pstmt.setNull(2, Types.INTEGER);
			}
			if (answerId != null) {
				pstmt.setInt(3, answerId);
			} else {
				pstmt.setNull(3, Types.INTEGER);
			}
			pstmt.setString(4, content);
			pstmt.executeUpdate();
		} catch (SQLException e) {
	        e.printStackTrace();
	    }
	}
	
	// Get reviews with qaId (question or answer id, depending on which one is needed) and forQuestion boolean (true for question, false for answer)
	public List<ReviewLightweightDTO> getAllReviews(int qaId, boolean forQuestion) {
		ArrayList<ReviewLightweightDTO> reviewList = new ArrayList<>();
		String query = "SELECT id, userId, content FROM reviews WHERE " + (forQuestion ? "questionId" : "answerId") + " = ?";
		
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setInt(1, qaId);
			ResultSet rs = pstmt.executeQuery();
			
			while (rs.next()) {
				int id = rs.getInt("id");
				int userId = rs.getInt("userId");
				String content = rs.getString("content");
				reviewList.add(new ReviewLightweightDTO(id, userId, content));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return reviewList;
	}
	
	// Get trusted reviews with qaId (question or answer id, depending on which one is needed) and forQuestion boolean (true for question, false for answer)
	public List<ReviewLightweightDTO> getAllTrustedReviews(int qaId, boolean forQuestion, int studentId) {
		ArrayList<ReviewLightweightDTO> reviewList = new ArrayList<>();
		String query = "SELECT r.id, r.userId, r.content "
				+ "FROM reviews r "
				+ "JOIN trustedReviewers tr ON r.userId = tr.reviewerId "
				+ "WHERE tr.studentId = ? AND " + (forQuestion ? "r.questionId" : "r.answerId") + " = ? "
				+ "ORDER BY tr.weight DESC";
		
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setInt(1, studentId);
			pstmt.setInt(2, qaId);
			ResultSet rs = pstmt.executeQuery();
			
			while (rs.next()) {
				int id = rs.getInt("id");
				int userId = rs.getInt("userId");
				String content = rs.getString("content");
				reviewList.add(new ReviewLightweightDTO(id, userId, content));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return reviewList;
	}
	
	// Retrieves the lightweight version of all reviews in the database from a user and creates a list
	public List<ReviewLightweightDTO> getAllReviews(int userId, String filter) {
		ArrayList<ReviewLightweightDTO> reviewList = new ArrayList<>();
		String query = "SELECT id, userId, content FROM reviews WHERE userId = ? " + filter;
		
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setInt(1, userId);
			ResultSet rs = pstmt.executeQuery();
			
			while (rs.next()) {
				int id = rs.getInt("id");
				String content = rs.getString("content");
				reviewList.add(new ReviewLightweightDTO(id, userId, content));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return reviewList;
	}
	
	// Get reviews from search
	public static List<ReviewLightweightDTO> searchReviews(int userId, String query, String filter, String sort) {
	    List<ReviewLightweightDTO> results = new ArrayList<>();
	    String[] keywords = query.toLowerCase().split("\\s+");
	    List<String> filteredKeywords = new ArrayList<>();
	    
	    for (String keyword : keywords) {
	        if (!COMMON_WORDS.contains(keyword)) {
	            filteredKeywords.add(keyword);
	        }
	    }
	    
	    if (filteredKeywords.isEmpty()) {
	        return results; // Return empty if only common words were entered
	    }
	    
	    StringBuilder sql = new StringBuilder("SELECT DISTINCT id, userId, content FROM reviews WHERE userId = ? AND (");
	    
	    for (int i = 0; i < filteredKeywords.size(); i++) {
	        sql.append("LOWER(title) LIKE ?");
	        if (i < filteredKeywords.size() - 1) {
	            sql.append(" OR ");
	        }
	    }
	    sql.append(") ");
	    
	    // Add filter and sort
	    if (filter != "") {
	    	sql.append("AND " + filter + " ");
	    }
	    sql.append(sort);
	    
	    try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
	         PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {
	    	
	    	pstmt.setInt(1, userId);
	        
	        for (int i = 0; i < filteredKeywords.size(); i++) {
	            pstmt.setString(i + 2, "%" + filteredKeywords.get(i) + "%"); 
	        }
	        
	        ResultSet rs = pstmt.executeQuery();
	        while (rs.next()) {
	            int id = rs.getInt("id");
	            String content = rs.getString("content");
	            results.add(new ReviewLightweightDTO(id, userId, content));
	        }
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	    return results;
	}
	
	// Get review with reviewId
	public Review getReview(int id) {
		Review review = null; // Returns null review if not found
		String query = "SELECT * FROM reviews WHERE id = ?";
		
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setInt(1, id);
			ResultSet rs = pstmt.executeQuery();
			
			if (rs.next()) {
				int userId = rs.getInt("userId");
				Integer questionId = rs.getObject("questionId", Integer.class);
				Integer answerId = rs.getObject("answerId", Integer.class);
				String content = rs.getString("content");
				
				review = new Review(userId, questionId, answerId, content);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return review;
	}
	
	public String getReviewUsername(int id) {
		String query = "SELECT u.userName FROM reviews r JOIN cse360users u ON r.userId = u.id WHERE a.id = ?";
		
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setInt(1, id);
			ResultSet rs = pstmt.executeQuery();
			
			if (rs.next()) { // Check if there is a user for that question
				return rs.getString("userName");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return ""; // Return empty string if not found
	}
	
	public String getReviewContent(int id) {
		String query = "SELECT content FROM reviews WHERE id = ?";
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setInt(1, id);
			ResultSet rs = pstmt.executeQuery();
			
			if (rs.next()) { // Check is question exists
				return rs.getString("content");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return ""; // Return empty string if not found
	}
	
	// Check if a review if for a question
	public boolean reviewIsForQuestion(int id) {
		String query = "SELECT questionId FROM reviews WHERE id = ?";
		
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setInt(1, id);
			ResultSet rs = pstmt.executeQuery();
			
			if (rs.next()) {
				Integer questionId = rs.getObject("questionId", Integer.class);
				
				return questionId != null;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}
	
	// Update review
	public void updateReviewContent(int id, String newContent) {
		String query = "UPDATE reviews SET content = ? WHERE id = ?";
		
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setString(1, newContent);
			pstmt.setInt(2, id);
			int updatedReviews = pstmt.executeUpdate(); // Number of reviews updated (should be either 0 or 1, but will check for any number changed)
			
			if (updatedReviews < 1) {
				System.out.println("*** Error *** Review not found.");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	// Delete review
	public void deleteReview(int id) {
		String query = "DELETE FROM reviews WHERE id = ?";
		
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setInt(1, id);
			int reviewsDeleted = pstmt.executeUpdate(); // Number of reviews deleted (should be either 0 or 1, but will check for any number changed)
			
			if (reviewsDeleted < 1) {
				System.out.println("*** Error *** Review ID not found.");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	// Add role request
	public void addRoleRequest(int userId, String role) {
		String query = "INSERT INTO roleRequests (userId, role) VALUES (?, ?)";
		
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setInt(1, userId);
			pstmt.setString(2, role);
			pstmt.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public boolean hasRequestedRole(int userId, String role) {
	    String query = "SELECT COUNT(*) FROM roleRequests WHERE userId = ? AND role = ?";
	    
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setInt(1, userId);
	        pstmt.setString(2, role);
	        
	        try (ResultSet rs = pstmt.executeQuery()) {
	            if (rs.next() && rs.getInt(1) > 0) {
	                return true;
	            }
	        }
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	    
	    return false;
	}
	
	public List<Map<String, Object>> getRoleRequests(List<String> roles, String sort) {
	    if (roles == null || roles.isEmpty()) {
	        return new ArrayList<>(); // Return empty list if no roles are provided
	    }

	    String query = "SELECT id, userId, role FROM roleRequests WHERE role IN (" + 
	                   roles.stream().map(r -> "?").collect(Collectors.joining(", ")) + ")";

	    if (sort != null && !sort.trim().isEmpty()) {
	        query += sort;
	    }

	    List<Map<String, Object>> roleRequests = new ArrayList<>();

	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        // Set role values dynamically
	        for (int i = 0; i < roles.size(); i++) {
	            pstmt.setString(i + 1, roles.get(i));
	        }

	        ResultSet rs = pstmt.executeQuery();

	        while (rs.next()) {
	            Map<String, Object> request = new HashMap<>();
	            request.put("id", rs.getInt("id"));
	            request.put("userId", rs.getInt("userId"));
	            request.put("role", rs.getString("role"));
	            roleRequests.add(request);
	        }
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }

	    return roleRequests;
	}
	
	public void addPrivateMessage(PrivateMessage pm) {
		addPrivateMessage(pm.getAuthorId(), pm.getCommenterId(), pm.getIsAuthor(), pm.getContent(), pm.getIsRead(), pm.getParentType(), pm.getParentId());
	}
	
	public void addPrivateMessage(int authorId, int commenterId, boolean isAuthor, String content, boolean isRead, String parentType, int parentId) {
		String insertQuestion = "INSERT INTO messages (authorId, commenterId, content, isRead, parentType, parentId, isAuthor) VALUES (?, ?, ?, ?, ?, ?, ?)";
		try (PreparedStatement pstmt = connection.prepareStatement(insertQuestion)) {
			pstmt.setInt(1, authorId);
			pstmt.setInt(2, commenterId);
			pstmt.setString(3, content);
			pstmt.setBoolean(4, isRead);
			pstmt.setString(5, parentType);
			pstmt.setInt(6,  parentId);
			pstmt.setBoolean(7, isAuthor);
			pstmt.executeUpdate();
		} catch (SQLException e) {
	        e.printStackTrace();
	    }
	}
	
	public PrivateMessage getPrivateMessage(int id) {
		PrivateMessage pm = null; // Returns null answer if not found
		String query = "SELECT * FROM messages WHERE id = ?";
		
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setInt(1, id);
			ResultSet rs = pstmt.executeQuery();
			
			if (rs.next()) {
				int authorId = rs.getInt("authorId");
				int commenterId = rs.getInt("commenterId");
				boolean isAuthor = rs.getBoolean("isAuthor");
;				String content = rs.getString("content");
				boolean isRead = rs.getBoolean("isRead");
				String parentType = rs.getString("parentType");
				int parentId = rs.getInt("parentId");
				
				pm = new PrivateMessage(authorId, commenterId, isAuthor, content, isRead, parentType, parentId);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return pm;
	}
	
	// This is a stopgap method. Because we are not currently dealing with the problem of messages from multiple commenters, 
	// if multiple commenters send messages, this method will find the commenterID of the first message sent 
	// in order to launch the PrivateMessagePage.
	
	public int getFirstMessageCommenterId(String parentType, int parentId) {
		int firstMsgsCommenterId = -1;
		String query = "SELECT commenterId FROM answers WHERE parentType = ?, parentId = ?";
		
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setString(1, parentType);
			pstmt.setInt(2, parentId);
			ResultSet rs = pstmt.executeQuery();
			
			
			firstMsgsCommenterId = rs.getInt("commenterId");
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return firstMsgsCommenterId;
		
	}
	
	// Get a list of all the users that have sent a message to author for specific question/answer/review
	public List<Integer> getAllMessages(int authorId, String parentType, int parentId) {
	    List<Integer> userList = new ArrayList<>();
	    String query = "SELECT DISTINCT commenterId FROM messages WHERE authorId = ? AND parentType = ? AND parentId = ? ";
	    
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setInt(1, authorId);
	        pstmt.setString(2, parentType);
	        pstmt.setInt(3, parentId);
	        
	        ResultSet rs = pstmt.executeQuery();
	        while (rs.next()) {
	            userList.add(rs.getInt("commenterId"));
	        }
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	    
	    return userList;
	}
	
	public List<PrivateMessageLightweightDTO> getAllPrivateMessages(int authorId, int commenterId, String parentType, int parentId) {
		ArrayList<PrivateMessageLightweightDTO> privateMessageList = new ArrayList<>();
		String query = "SELECT id, content, isAuthor FROM messages WHERE authorId = ? AND commenterId = ? AND parentType = ? AND parentId = ?";
		
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setInt(1, authorId);
			pstmt.setInt(2, commenterId);
			pstmt.setString(3,  parentType);
			pstmt.setInt(4,  parentId);
			ResultSet rs = pstmt.executeQuery();
			
			while (rs.next()) {
				int id = rs.getInt("id");
				String content = rs.getString("content");
				boolean isAuthor = rs.getBoolean("isAuthor");
				privateMessageList.add(new PrivateMessageLightweightDTO(id, content, isAuthor));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return privateMessageList;
	}
	
	public String getPrivateMessageUsername(String parentType, int parentId) {
	    String query = "SELECT u.userName FROM messages m "
	                   + "JOIN cse360users u ON "
	                   + "  (m.isAuthor = TRUE AND m.authorId = u.id) OR "
	                   + "  (m.isAuthor = FALSE AND m.commenterId = u.id) "
	                   + "WHERE m.parentType = ? AND m.parentId = ?";

	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setString(1, parentType);
	        pstmt.setInt(2, parentId);
	        ResultSet rs = pstmt.executeQuery();

	        if (rs.next()) {
	            return rs.getString("userName");
	        }
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }

	    return "";
	}


	
	public void addUserRole(int userId, String newRole) {
		// Get users current roles
		ArrayList<String> currentRoles = getUserRole(userId);
		
		if (currentRoles == null) {
			System.out.println("*** Error *** User not found.");
			return;
		}
		
		if (currentRoles.contains(newRole)) {
	        return; // Already has role
	    }
		
		// Add role and get role string
		currentRoles.add(newRole);
		String updatedRoles = String.join(", ", currentRoles);
		
	    String query = "UPDATE cse360users SET role = ? WHERE id = ?";
	    
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setString(1, updatedRoles);
	        pstmt.setInt(2, userId);
	        
	        int rowsAffected = pstmt.executeUpdate();
	        if (rowsAffected < 1) {
	        	System.out.println("*** Error *** Role not added.");
	        }
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	}

	
	public void deleteRoleRequest(int id) {
	    String query = "DELETE FROM roleRequests WHERE id = ?";

	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setInt(1, id);
	        int deleted = pstmt.executeUpdate();
	        
	        if (deleted < 1) {
				System.out.println("*** Error *** Role Request ID not found.");
			}
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	}

	
	// Check if question is resolved
	public boolean isResolved(int questionId) {
		String query = "SELECT resolved FROM questions WHERE id = ?";
		
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setInt(1, questionId);
			ResultSet rs = pstmt.executeQuery();
			
			if (rs.next()) {
				return rs.getBoolean("resolved");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return false;
	}
	
	// Get resolved answerId for question, return -1 if no answer
	public int getResolvedAnswerId(int questionId) {
		String query = "SELECT resolvedAnswerId FROM questions WHERE id = ?";
		
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setInt(1, questionId);
			ResultSet rs = pstmt.executeQuery();
			
			if (rs.next()) {
				return rs.getInt("resolvedAnswerId");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return -1;
	}
	
	public void setResolvedAnswerId(int questionId, int answerId) {
		String query = "UPDATE questions SET resolvedAnswerId = ?, resolved = ? WHERE id = ?";
		
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setInt(1, answerId);
			pstmt.setBoolean(2, true);
			pstmt.setInt(3, questionId);
			int updatedQuestions = pstmt.executeUpdate(); // Number of questions updated (should be either 0 or 1, but will check for any number changed)
			
			if (updatedQuestions < 1) {
				System.out.println("*** Error *** Question not found.");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public void removeResolvedAnswerId(int questionId) {
		String query = "UPDATE questions SET resolvedAnswerId = ?, resolved = ? WHERE id = ?";
		
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setNull(1, java.sql.Types.NULL);
			pstmt.setBoolean(2, false);
			pstmt.setInt(3, questionId);
			int updatedQuestions = pstmt.executeUpdate(); // Number of questions updated (should be either 0 or 1, but will check for any number changed)
			
			if (updatedQuestions < 1) {
				System.out.println("*** Error *** Question not found.");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public void deleteAssociatedResolvesForAnswer(int answerId) {
		String query = "UPDATE questions SET resolvedAnswerId = ?, resolved = ? WHERE resolvedAnswerId = ?";
		
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setNull(1, java.sql.Types.NULL);
			pstmt.setBoolean(2, false);
			pstmt.setInt(3, answerId);
			pstmt.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	// Add trusted reviewer for student
	public void addTrustedReviewer(int studentId, int reviewerId, int weight) {
		String query = "INSERT INTO trustedReviewers (studentId, reviewerId, weight) VALUES (?, ?, ?)";
		
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setInt(1, studentId);
			pstmt.setInt(2, reviewerId);
			pstmt.setInt(3, weight);
			pstmt.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	// Checks if student trusts reviewer
	public boolean studentTrustsReviewer(int studentId, int reviewerId) {
	    String query = "SELECT COUNT(*) FROM trustedReviewers WHERE studentId = ? AND reviewerId = ?";
	    
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setInt(1, studentId);
	        pstmt.setInt(2, reviewerId);
	        
	        ResultSet rs = pstmt.executeQuery();
	        
	        if (rs.next()) {
	            int count = rs.getInt(1);
	            return count > 0;
	        }
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	    
	    return false;
	}
	
	// Delete trusted reviewer for student
	public void deleteTrustedReviewer(int studentId, int reviewerId) {
	    String query = "DELETE FROM trustedReviewers WHERE studentId = ? AND reviewerId = ?";
	    
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setInt(1, studentId);
	        pstmt.setInt(2, reviewerId);
	        pstmt.executeUpdate();
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	}


	
	// Closes the database connection and statement.
	public void closeConnection() {
		try{ 
			if(statement!=null) statement.close(); 
		} catch(SQLException se2) { 
			se2.printStackTrace();
		} 
		try { 
			if(connection!=null) connection.close(); 
		} catch(SQLException se){ 
			se.printStackTrace(); 
		} 
	}
	
	// Delete the files associated with the database
	public void deleteDatabase() {
		// Close connection
		closeConnection();
		
		// Get file paths
		String databasePath = System.getProperty("user.home") + "/FoundationDatabase.mv.db";
		String tracePath = System.getProperty("user.home") + "/FoundationDatabase.trace.db";
		
		// Get files from path
		File databaseFile = new File(databasePath);
		File traceFile = new File(tracePath);
		
		// Attempt to delete the database file
		if (databaseFile.exists() && databaseFile.delete()) {
			System.out.println("Database file deleted successfully!");
		} else {
			System.out.println("Database file not found or unable to be deleted!");
		}
		
		// Attempt to delete the trace file
		if (traceFile.exists() && traceFile.delete()) {
			System.out.println("Trace file deleted successfully!");
		} else {
			System.out.println("Trace file not found or unable to be deleted!");
		}
	}
}
