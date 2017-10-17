package lab1;
/**
 * A class for Word
 */
public class Word {
	
	private Info info;
	private String word;
	
	/**
	 * Builds a Word object with the actual
	 * word (string) 'word' and the information 'info'
	 */
	public Word(String word, Info info) {
		this.info=info;
		this.word=word;
	}
	
	/**
	 * Returns the actual word (string)
	 * of this Word
	 */
	public String getWord() {
		return word;
	}
	
	/**
	 * Returns the information (Info)
	 * of this Word
	 */
	public Info getInfo() {
		return info;
	}
	
	/**
	 * Returns a String representation
	 * of this Word
	 * (for testing/debugging only)
	 */
	public String toString() {
		return word;
	}
}
