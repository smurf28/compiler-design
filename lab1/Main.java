
import java.io.*;

/**
 * Main class for testing. You should not change this
 * class except the the name of the input file
 */
public class Main {

	/**
	 * The main method
	 */
	public static void main(String[] args) throws IOException, FileNotFoundException {
		// the FileReader
		FileReader fr = new FileReader("/your/path/to/the/input/file.txt");
		// builds the scanner
		WordScanner scanner = new WordScanner(fr);
		// builds a word count
		WordCount counter = new WordCount();
		while ( scanner.hasNextWord() ) {
			Word w = scanner.nextWord();
			counter.add(w.getWord().toLowerCase(), w.getInfo());
		}
		// displays the word count
		counter.display();S
	}
}
