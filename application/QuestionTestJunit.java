package application;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class QuestionTestJunit {

	@Test
	public void validQuestionTest() {
		Question question = new Question(0, "Valid Title", "This is a valid description.", -1);
		assertEquals("", Question.validate(question));
	}
	
	@Test
	public void nullQuestionTest() {
		assertEquals("*** Error *** Question cannot be null", Question.validate(null));
	}
	
	@Test
	public void emptyTitleTest() {
		Question question = new Question(0, "", "Valid description.", -1);
		assertEquals("*** Error *** Question title cannot be empty", Question.validate(question));
	}
	
	@Test
	public void tooLongTitleTest() {
		 String longTitle = "a".repeat(256); // Exceeds 255 character limit
		 Question question = new Question(0, longTitle, "Valid description.", -1);
		 assertEquals("*** Error *** Question title cannot exceed maximum length (255 characters)", Question.validate(question));
	}
	
	@Test
	public void emptyDescriptionTest() {
		Question question = new Question(0, "Valid Title", "", -1);
		assertEquals("*** Error *** Question description cannot be empty", Question.validate(question));
	}
	
	@Test
	public void tooLongDescriptionTest() {
		String longDescription = "a".repeat(65536); // Exceeds 65535 character limit
	    Question question = new Question(0, "Valid Title", longDescription, -1);
		assertEquals("*** Error *** Question description cannot exceed maximum length (65535 characters)", Question.validate(question));
	}

}
