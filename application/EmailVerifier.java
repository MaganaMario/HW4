package application;

public class EmailVerifier {
	/**
	 * Title: Email Verifier
	 * 
	 * Description: Verifies that the email is a valid email address by checking the requirements 
	 * and characters associated with a typical email address. 
	 * Based off RFC 5322 as defined in Wikipedia.
	 * 
	 * @author Braeden West
	 * 
	 */
	//**********************************************************************************************
	
	private static final String EMAIL_SPECIAL_CHARS = "!#$%&'*+-/=?^_`{|}~";
	private static final String EMAIL_QUOTED_CHARS = "!#$%&'*+-/=?^_`{|}~ .(),:;<>[]\\";
	
	public static String emailErrorMessage = "";		// The error message text
	public static String emailInput = "";			    // The input being processed
	public static int emailIndexofError = -1;		    // The index where the error was located
	public static boolean validLocalPart = false;
	public static boolean validDomain = false;
	
	/*
	 *	Verifies email address and returns a string with the error message.
	 *	If the email is valid, the method will return "".
	 *
	 *	Checks the requirements for the local-part (before the @) and
	 *	the domain (after the @).
	 */
	public static String verifyEmail(String input) {
		emailErrorMessage = "";
		emailIndexofError = -1;     // Index of character that led to an error
		
		// Check length constraints
		if (input.length() <= 0) return "*** Error *** The email address is empty!";
		if (input.length() > 320) return "*** Error *** The email address cannot exceed 320 characters!";
		
		// Check for exactly one '@' symbol
		if (input.chars().filter(character -> character == '@').count() != 1) return "*** Error *** The email address must contain exactly one '@' symbol!";
		
		emailInput = input; 			// Copy of input
		validLocalPart = false;			// Reset the Boolean flag
		validDomain = false;			// Reset the Boolean flag
		
		// Split the email address into the local-part and domain
		int indexOfAt = input.indexOf('@');
		String localPart = input.substring(0, indexOfAt);
		String domain = input.substring(indexOfAt + 1);
		
		// Validate local-part
		String localPartErrorMessage = EmailVerifier.verifyLocalPart(localPart);
		if (localPartErrorMessage.equals("")) {
			validLocalPart = true;
		}
		
		// Validate domain
		String domainErrorMessage = EmailVerifier.verifyDomain(domain);
		if (domainErrorMessage.equals("")) {
			validDomain = true;
		}
		
		String errMessage = "";
		if (!validLocalPart)
			errMessage += "Local-part: " + localPartErrorMessage;
		
		if (!validDomain)
			errMessage += "Domain: " + domainErrorMessage;
		
		if (errMessage == "")
			return "";

		return errMessage + "conditions were not satisfied";
	}

	public static String verifyLocalPart(String input) {
		String errorMessage = "";
		String allowedSpecialCharacters = EmailVerifier.EMAIL_SPECIAL_CHARS;
		
		// Check length constraints
		if (input.length() <= 0) return "Missing; ";
		if (input.length() > 64) errorMessage += "Too long; ";
		
		// Performs Finite State Machine simulation to determine if local-part is valid
		int state = 0;
		int currentCharIndex = 0;
		
		// Check for double quotes
		if (input.charAt(0) == '"' && input.charAt(input.length() - 1) == '"' 
				&& input.chars().filter(character -> character == '"').count() == 2) {
			allowedSpecialCharacters = EmailVerifier.EMAIL_QUOTED_CHARS;
			state = 1;
			currentCharIndex = 1;
		}
		
		// Run Finite State Machine until reaching the end of the input
		while (currentCharIndex < input.length()) {
			char currentChar = input.charAt(currentCharIndex);
			
			switch (state) {
			case 0:
				// State 0 has one valid transition
				if ((currentChar >= 'A' && currentChar <= 'Z' ) ||				// Check for A-Z
						(currentChar >= 'a' && currentChar <= 'z' ) ||			// Check for a-z
						(currentChar >= '0' && currentChar <= '9' ) ||  		// Check for 0-9
						allowedSpecialCharacters.indexOf(currentChar) != -1) {	// Check for special characters
					// Next state
					state = 1;
				} else {
					errorMessage += "Invalid starting character '" + currentChar + "'; ";
				}
				
				break;
				
			case 1:
				// State 1 has two valid transitions
				if ((currentChar >= 'A' && currentChar <= 'Z' ) ||				// Check for A-Z
						(currentChar >= 'a' && currentChar <= 'z' ) ||			// Check for a-z
						(currentChar >= '0' && currentChar <= '9' ) ||  		// Check for 0-9
						allowedSpecialCharacters.indexOf(currentChar) != -1) {	// Check for special characters
					// Next state
					state = 1;
				} else if (currentChar == '.') {								// Check for period
					// Next state
					state = 2;
				} else {
					errorMessage += "Invalid character '" + currentChar + "'; ";
				}
				
				break;
				
			case 2:
				// State 2 has one valid transition
				if ((currentChar >= 'A' && currentChar <= 'Z' ) ||				// Check for A-Z
						(currentChar >= 'a' && currentChar <= 'z' ) ||			// Check for a-z
						(currentChar >= '0' && currentChar <= '9' ) ||  		// Check for 0-9
						allowedSpecialCharacters.indexOf(currentChar) != -1) {	// Check for special characters
					// Next state
					state = 1;
				} else {
					errorMessage += "Double period not allowed; ";
				}
				
				break;
			}
			
			currentCharIndex++;
		}
		
		// Check if still in state 2
		if (state == 2) {
			errorMessage += "Must not end with a period; ";
		}
		
		return errorMessage;
	}

	public static String verifyDomain(String input) {
		String errorMessage = "";
		
		// Convert to all lower case (domains are not case-sensitive)
		input = input.toLowerCase();
		
		// Check length constraints
		if (input.length() <= 0) return "Missing; ";
		if (input.length() > 255) errorMessage += "Too long; ";
		
		// Check for at least one '.'
		if (input.indexOf('.') == -1) errorMessage += "Missing '.'; ";
		
		// Performs Finite State Machine simulation to determine if local-part is valid
		int state = 0;
		int currentCharIndex = 0;
		
		// Run Finite State Machine until reaching the end of the input
		while (currentCharIndex < input.length()) { 
			char currentChar = input.charAt(currentCharIndex);
			
			switch (state) {
			case 0:
				// State 0 has one valid transition
				if ((currentChar >= 'A' && currentChar <= 'Z' ) ||				// Check for A-Z
						(currentChar >= 'a' && currentChar <= 'z' ) ||			// Check for a-z
						(currentChar >= '0' && currentChar <= '9' )) {  		// Check for 0-9
					// Next state
					state = 1;
				} else {
					errorMessage += "Invalid starting character '" + currentChar + "'; ";
				}
				
				break;
				
			case 1:
				// State 1 has three valid transitions
				if ((currentChar >= 'A' && currentChar <= 'Z' ) ||				// Check for A-Z
						(currentChar >= 'a' && currentChar <= 'z' ) ||			// Check for a-z
						(currentChar >= '0' && currentChar <= '9' )) {  		// Check for 0-9
					// Next state
					state = 1;
				} else if (currentChar == '-') {								// Check for hyphen
					// Next state
					state = 2;
				} else if (currentChar == '.') {								// Check for period
					// Next state
					state = 3;
				} else {
					errorMessage += "Invalid character '" + currentChar + "'; ";
				}
					
				break;
				
			case 2:
				// State 2 has one valid transition
				if ((currentChar >= 'A' && currentChar <= 'Z' ) ||				// Check for A-Z
						(currentChar >= 'a' && currentChar <= 'z' ) ||			// Check for a-z
						(currentChar >= '0' && currentChar <= '9' )) {  		// Check for 0-9
					// Next state
					state = 1;
				} else if (currentChar == '-') {								// Check for hyphen
					errorMessage += "Double hyphen not allowed; ";
				} else if (currentChar == '.') {								// Check for period
					errorMessage += "Must not end with hyphen; ";
				} else {
					errorMessage += "Invalid character '" + currentChar + "'; ";
				}
				
				break;
				
			case 3:
				// State 3 has one valid transition
				if ((currentChar >= 'A' && currentChar <= 'Z' ) ||				// Check for A-Z
						(currentChar >= 'a' && currentChar <= 'z' ) ||			// Check for a-z
						(currentChar >= '0' && currentChar <= '9' )) {  		// Check for 0-9
					// Next state
					state = 1;
				} else if (currentChar == '-') {								// Check for hyphen
					errorMessage += "Must not start with hyphen; ";
				} else if (currentChar == '.') {								// Check for period
					errorMessage += "Double period not allowed; ";
				} else {
					errorMessage += "Invalid character '" + currentChar + "'; ";
				}
				
				break;
			}
			
			currentCharIndex++;
		}
		
		// Check if still in state 2 or 3
		if (state == 2) {
			errorMessage += "Must not end with hyphen; ";
		} else if (state == 3) {
			errorMessage += "Must not end with period; ";
		}
		
		return errorMessage;
	}
}