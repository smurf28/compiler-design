package lab1;
import java.util.*;

/**
 * A class for the WordCount data structure.
 * A WordCount object is a map which pairs a word (string)
 * with a list of information (Info)
 */
public class WordCount {

	//HashMap<String, List<Info>> map; 
	TreeMap<String, List<Info>> map;
	/**
	 * Builds an empty WordCount
	 */
	public WordCount() {
		//map=new HashMap<String, List<Info>>();
		map=new TreeMap<String, List<Info>>();
	}
	
	/**
	 * Adds the given 'info' in the list of
	 * Infos of the given word 'word'
	 */
	public void add(String word, Info info) {
		if(map.containsKey(word)){
			map.get(word).add(info);
		}
		else{
			List<Info> infos = new LinkedList<Info>();
			infos.add(info);
			map.put(word, infos);
		}
	}
	
	/**
	 * Returns an iterator over the informations of
	 * the given word 'word'. If 'word' has no information
	 * returns null
	 */
	public Iterator<Info> getListIterator(String word) {
		Iterator it = map.keySet().iterator();  
		while(it.hasNext()){
			String w = (String)it.next(); 
			if(w==word)
				return it;
		}
		return null;
	}
	
	/**
	 * Displays the WordCount on System.out
	 */
	public void display() {
        Iterator it = map.keySet().iterator();  
        while(it.hasNext()) {  
        	String w = (String)it.next(); 
        	List<Info> infos=map.get(w);
            System.out.print(w+" ("+infos.size()+")"+":");
        	for(Info info :infos)
        		System.out.print(" "+info.toString()); 
        	System.out.println();
        }  
	}
}
