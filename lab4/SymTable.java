import java.util.*;

/**
 * A class for Symbol Table
 */
public class SymTable {

    /**
     * Create a Symbol Table with one empty scope
     */
    private List<HashMap<String,SymInfo>> symMapList;

    public SymTable() {
	symMapList = new LinkedList<HashMap<String,SymInfo>>();
        symMapList.add(0,new HashMap<String,SymInfo>());
    }

    /**
     * Add a declaration (i.e. a pair [name,sym]) in the inner scope
     */
    public void addDecl(String name, SymInfo sym) throws DuplicateSymException, EmptySymTableException {
	if ((name==null) || (sym==null)) {
         throw new NullPointerException();
      }
      if (symMapList.isEmpty()) {
         throw new EmptySymTableException();
      }
      if (symMapList.get(0).containsKey(name)) {
         throw new DuplicateSymException();
      }
     
      symMapList.get(0).put(name,sym);
     
      return;
    }

    /**
     * Add a new inner scope
     */
    public void addScope() {
	 symMapList.add(0,new HashMap<String,SymInfo>());
    }

    /**
     * Lookup for 'name' in the inner scope
     */
    public SymInfo lookupLocal(String name) throws EmptySymTableException {
	if (symMapList.isEmpty()) {
         throw new EmptySymTableException();
      }
      if (symMapList.get(0).containsKey(name)) {
         return symMapList.get(0).get(name);
      }
      else {
         return null;
      }
    }

    /**
     * Lookup for 'name' sequentially in all scopes from inner to outer
     */
    public SymInfo lookupGlobal(String name)  throws EmptySymTableException {
	 if (symMapList.isEmpty()) {
         throw new EmptySymTableException();
      }
     
      for (int i = 0; i < symMapList.size(); i++) {
         if (symMapList.get(i).containsKey(name)) {
            return symMapList.get(i).get(name);
         }
      }
     
      return null;
    }

    /**
     * Remove the inner scope
     */
    public void removeScope() throws EmptySymTableException {
	 if (symMapList.isEmpty()) {
         throw new EmptySymTableException();
      }
      symMapList.remove(0);
    }

    /**
     * Print the Symbol Table on System.out
     */
    public void print() {
	System.out.print("\nSym Table\n");
      for (int i = 0; i < symMapList.size(); i++) {
         System.out.print(symMapList.get(i).toString());
         System.out.print("\n");
      }
      System.out.print("\n");
    }
}
