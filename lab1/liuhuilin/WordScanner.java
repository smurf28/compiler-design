package lab1;
import java.io.*;

/**
 * This class implements a word (string) scanner
 */
public class WordScanner {

	private FileReader input;
	private int currentLineNumber;
	private int currentcharPos;
	private int firstChar;
	/**
	 * Builds a WordScanner object based on the given input
	 */
	public WordScanner(FileReader input) throws IOException {
		this.input=input;
		currentLineNumber=1;
		currentcharPos=0;
	}
	
	/**
	 * Returns the next word from input
	 * Precond: there must be at least
	 * one word left in the input
	 * (i.e. hasNextWord() must evaluate to true)
	 */
	public Word nextWord() throws IOException {
			Info info =new Info(currentcharPos,currentLineNumber);
			String word = "";
	        int ch = this.firstChar;  
	        while(ch != -1){  
	        	char currentCharacter=(char)ch;        		
	        	if('a'<=currentCharacter&&currentCharacter<'z'||'A'<=currentCharacter&&currentCharacter<'Z'||currentCharacter=='\''){
		        	word += currentCharacter;
	        		ch = input.read();
	        		currentcharPos++;
	        	}else if(currentCharacter=='\n'){
	        		ch = input.read();
	        		currentLineNumber++;
	        		currentcharPos=1;
	        		break;
	        	}else{
	        		break;
	        	}
	        }  
	        return new Word(word,info);
}
	
	/**
	 * Returns true if there is at least
	 * one word left in the input, false otherwise
	 * @throws IOException 
	 */
	public boolean hasNextWord() throws IOException {
		int ch = input.read();  
		currentcharPos++;
        while(ch != -1){
        	char currentCharacter=(char)ch;  
        	if('a'<=currentCharacter&&currentCharacter<'z'||'A'<=currentCharacter&&currentCharacter<'Z'){
        		this.firstChar=ch;
        		return true; 
        	}else if(currentCharacter=='\n'){
        		ch = input.read();
        		currentLineNumber++;
        		currentcharPos=1;
        		continue;
        	}else{
        		ch = input.read();
        		currentcharPos++;
        		continue;
        	}
        }
        return false;
	}
	
}
