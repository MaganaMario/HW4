package application;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class AnswerTestJunit {


	@Test
	public void validAnswerTest() {
		Answer answer = new Answer(0, 0, "This is valid answer content.");
		assertEquals("", Answer.validate(answer));
	}
	
	@Test
	public void nullAnswerTest() {
		assertEquals("*** Error *** Answer cannot be null", Answer.validate(null));
	}
	
	@Test
	public void emptyContentTest() {
		Answer answer = new Answer(0, 0, "");
		assertEquals("*** Error *** Answer content cannot be empty", Answer.validate(answer));
	}
	
	@Test
	public void tooLongContentTest() {
		String longContent = "a".repeat(65536); // Exceeds 65535 character limit
		Answer answer = new Answer(0, 0, longContent);
		assertEquals("*** Error *** Answer content/ cannot exceed maximum length (65535 characters)", Answer.validate(answer));
	}


}
