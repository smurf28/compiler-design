import java.util.*;

/**
 * The SymInfo class defines a Symbol Table entry. 
 * Each SymInfo contains a type (a Type object).
 */
public class SymInfo {
    
    private Type type;

    /**
     * Create a SymInfo object of type 'type'
     */
    public SymInfo(Type type) {
        this.type = type;
    }

    /**
     * Return the type of the SymInfo
     */
    public Type getType() {
        return type;
    }

    /**
     * Return a String representation of the SymInfo
     * (for debuggung purpose)
     */
    public String toString() {
        return type.toString();
    }
}
