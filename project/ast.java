import java.io.*;
import java.util.*;

// **********************************************************************
// The ASTnode class defines the nodes of the abstract-syntax tree that
// represents a C-- program.
//
// Internal nodes of the tree contain pointers to children, organized
// either in a list (for nodes that may have a variable number of 
// children) or as a fixed set of fields.
//
// The nodes for literals and ids contain line and character number
// information; for string literals and identifiers, they also contain a
// string; for integer literals, they also contain an integer value.
//
// Here are all the different kinds of AST nodes and what kinds of children
// they have.  All of these kinds of AST nodes are subclasses of "ASTnode".
// Indentation indicates further subclassing:
//
//     Subclass            Kids
//     --------            ----
//     ProgramNode         DeclListNode
//     DeclListNode        linked list of DeclNode
//     DeclNode:
//       VarDeclNode       TypeNode, IdNode, int
//       FnDeclNode        TypeNode, IdNode, FormalsListNode, FnBodyNode
//       FormalDeclNode    TypeNode, IdNode
//       StructDeclNode    IdNode, DeclListNode
//
//     FormalsListNode     linked list of FormalDeclNode
//     FnBodyNode          DeclListNode, StmtListNode
//     StmtListNode        linked list of StmtNode
//     ExpListNode         linked list of ExpNode
//
//     TypeNode:
//       IntNode           -- none --
//       BoolNode          -- none --
//       VoidNode          -- none --
//       StructNode        IdNode
//
//     StmtNode:
//       AssignStmtNode      AssignNode
//       PostIncStmtNode     ExpNode
//       PostDecStmtNode     ExpNode
//       ReadStmtNode        ExpNode
//       WriteStmtNode       ExpNode
//       IfStmtNode          ExpNode, DeclListNode, StmtListNode
//       IfElseStmtNode      ExpNode, DeclListNode, StmtListNode,
//                                    DeclListNode, StmtListNode
//       WhileStmtNode       ExpNode, DeclListNode, StmtListNode
//       CallStmtNode        CallExpNode
//       ReturnStmtNode      ExpNode
//
//     ExpNode:
//       IntLitNode          -- none --
//       StrLitNode          -- none --
//       TrueNode            -- none --
//       FalseNode           -- none --
//       IdNode              -- none --
//       DotAccessNode       ExpNode, IdNode
//       AssignNode          ExpNode, ExpNode
//       CallExpNode         IdNode, ExpListNode
//       UnaryExpNode        ExpNode
//         UnaryMinusNode
//         NotNode
//       BinaryExpNode       ExpNode ExpNode
//         PlusNode     
//         MinusNode
//         TimesNode
//         DivideNode
//         AndNode
//         OrNode
//         EqualsNode
//         NotEqualsNode
//         LessNode
//         GreaterNode
//         LessEqNode
//         GreaterEqNode
//
// Here are the different kinds of AST nodes again, organized according to
// whether they are leaves, internal nodes with linked lists of kids, or
// internal nodes with a fixed number of kids:
//
// (1) Leaf nodes:
//        IntNode,   BoolNode,  VoidNode,  IntLitNode,  StrLitNode,
//        TrueNode,  FalseNode, IdNode
//
// (2) Internal nodes with (possibly empty) linked lists of children:
//        DeclListNode, FormalsListNode, StmtListNode, ExpListNode
//
// (3) Internal nodes with fixed numbers of kids:
//        ProgramNode,     VarDeclNode,     FnDeclNode,     FormalDeclNode,
//        StructDeclNode,  FnBodyNode,      StructNode,     AssignStmtNode,
//        PostIncStmtNode, PostDecStmtNode, ReadStmtNode,   WriteStmtNode   
//        IfStmtNode,      IfElseStmtNode,  WhileStmtNode,  CallStmtNode
//        ReturnStmtNode,  DotAccessNode,   AssignExpNode,  CallExpNode,
//        UnaryExpNode,    BinaryExpNode,   UnaryMinusNode, NotNode,
//        PlusNode,        MinusNode,       TimesNode,      DivideNode,
//        AndNode,         OrNode,          EqualsNode,     NotEqualsNode,
//        LessNode,        GreaterNode,     LessEqNode,     GreaterEqNode
//
// **********************************************************************

// **********************************************************************
// ASTnode class (base class for all other kinds of nodes)
// **********************************************************************

abstract class ASTnode { 
    // every subclass must provide an unparse operation
    abstract public void unparse(PrintWriter p, int indent);

    // this method can be used by the unparse methods to do indenting
    protected void doIndent(PrintWriter p, int indent) {
        for (int k=0; k<indent; k++) p.print(" ");
    }

	// Check for main //
	static boolean hasMain = false;
	static String currentFunction = "";
}

// **********************************************************************
// ProgramNode,  DeclListNode, FormalsListNode, FnBodyNode,
// StmtListNode, ExpListNode
// **********************************************************************

class ProgramNode extends ASTnode {
    public ProgramNode(DeclListNode L) {
        myDeclList = L;
    }

    /**
     * nameAnalysis
     * Creates an empty symbol table for the outermost scope, then processes
     * all of the globals, struct defintions, and functions in the program.
     */
    public void nameAnalysis() {
        SymTable symTab = new SymTable();
        myDeclList.nameAnalysis(symTab);
    }
    
    /**
     * typeCheck
     */
    public void typeCheck() {
        myDeclList.typeCheck();

		if(!hasMain)
			ErrMsg.fatal(0, 0, "No main function");
    }
    
    public void unparse(PrintWriter p, int indent) {
        myDeclList.unparse(p, indent);
    }

	/**
	 * codeGen
	 */
	public void codeGen() {
		// Kickoff code generation //
		myDeclList.codeGen();
	}

    // 1 kid
    private DeclListNode myDeclList;
}

class DeclListNode extends ASTnode {
    public DeclListNode(List<DeclNode> S) {
        myDecls = S;
    }

    /**
     * nameAnalysis
     * Given a symbol table symTab, process all of the decls in the list.
     */
    public void nameAnalysis(SymTable symTab) {
        //nameAnalysis(symTab, symTab);
		for (DeclNode node : myDecls) {
             node.nameAnalysis(symTab);
         }
    }
    
    /**
     * nameAnalysis inside a struct definition
     * Given a symbol table symTab and a global symbol table globalTab
     * process all of the decls in the list.
     */    
    public void nameAnalysis(SymTable structSymTab, SymTable globalTab) { 
        for (DeclNode node : myDecls) {
            if (node instanceof VarDeclNode) {
                ((VarDeclNode)node).nameAnalysis(structSymTab, globalTab);
            } else {
                // this should never happen
                 node.nameAnalysis(globalTab);
            }
        }
    }    
    
    /**
     * typeCheck
     */
    public void typeCheck() {
        for (DeclNode node : myDecls) {
            node.typeCheck();
        }
    }

	/**
	 * codeGen
	 */
	public void codeGen(){
		for(DeclNode node : myDecls) {
			node.codeGen();
		}
	}
    
    public void unparse(PrintWriter p, int indent) {
        Iterator it = myDecls.iterator();
        try {
            while (it.hasNext()) {
                ((DeclNode)it.next()).unparse(p, indent);
            }
        } catch (NoSuchElementException ex) {
            System.err.println("unexpected NoSuchElementException in DeclListNode.print");
            System.exit(-1);
        }
    }

	// How many declared variables do we have //
	public int numDecl() {
		return myDecls.size();
	}

    // list of kids (DeclNodes)
    private List<DeclNode> myDecls;
}

class FormalsListNode extends ASTnode {
    public FormalsListNode(List<FormalDeclNode> S) {
        myFormals = S;
    }

    /**
     * nameAnalysis
     * Given a symbol table symTab, do:
     * for each formal decl in the list
     *     process the formal decl
     *     if there was no error, add type of formal decl to list
     */
    public List<Type> nameAnalysis(SymTable symTab) {
        List<Type> typeList = new LinkedList<Type>();
        for (FormalDeclNode node : myFormals) {
            SymInfo info = node.nameAnalysis(symTab);
            if (info != null) {
                typeList.add(info.getType());
            }
        }
        return typeList;
    }    

	/**
	 * codeGen
	 */
	public void codeGen() {
		for(FormalDeclNode node : myFormals) {
			node.codeGen();
		}
	}
    
    /**
     * Return the number of formals in this list.
     */
    public int length() {
        return myFormals.size();
    }
    
    public void unparse(PrintWriter p, int indent) {
        Iterator<FormalDeclNode> it = myFormals.iterator();
        if (it.hasNext()) { // if there is at least one element
            it.next().unparse(p, indent);
            while (it.hasNext()) {  // print the rest of the list
                p.print(", ");
                it.next().unparse(p, indent);
            }
        } 
    }

    // list of kids (FormalDeclNodes)
    private List<FormalDeclNode> myFormals;
}

class FnBodyNode extends ASTnode {
    public FnBodyNode(DeclListNode declList, StmtListNode stmtList) {
        myDeclList = declList;
        myStmtList = stmtList;
    }

    /**
     * nameAnalysis
     * Given a symbol table symTab, do:
     * - process the declaration list
     * - process the statement list
     */
    public void nameAnalysis(SymTable symTab) {
        myDeclList.nameAnalysis(symTab);
        myStmtList.nameAnalysis(symTab);
    }    
 
    /**
     * typeCheck
     */
    public void typeCheck(Type retType) {
        myStmtList.typeCheck(retType);
    }

	/**
	 * codeGen
	 */
	public void codeGen() {
		myStmtList.codeGen();
	}
          
    public void unparse(PrintWriter p, int indent) {
        myDeclList.unparse(p, indent);
        myStmtList.unparse(p, indent);
    }

	public int numLocals() {
		return myDeclList.numDecl();
	}

    // 2 kids
    private DeclListNode myDeclList;
    private StmtListNode myStmtList;
}

class StmtListNode extends ASTnode {
    public StmtListNode(List<StmtNode> S) {
        myStmts = S;
    }

    /**
     * nameAnalysis
     * Given a symbol table symTab, process each statement in the list.
     */
    public void nameAnalysis(SymTable symTab) {
        for (StmtNode node : myStmts) {
            node.nameAnalysis(symTab);
        }
    }    
    
    /**
     * typeCheck
     */
    public void typeCheck(Type retType) {
        for(StmtNode node : myStmts) {
            node.typeCheck(retType);
        }
    }

	/**
	 * codeGen
	 */
	public void codeGen() {
		for(StmtNode node : myStmts) {
			node.codeGen();
		}
	}
    
    public void unparse(PrintWriter p, int indent) {
        Iterator<StmtNode> it = myStmts.iterator();
        while (it.hasNext()) {
            it.next().unparse(p, indent);
        }
    }

    // list of kids (StmtNodes)
    private List<StmtNode> myStmts;
}

class ExpListNode extends ASTnode {
    public ExpListNode(List<ExpNode> S) {
        myExps = S;
    }
    
    public int size() {
        return myExps.size();
    }
    
    /**
     * nameAnalysis
     * Given a symbol table symTab, process each exp in the list.
     */
    public void nameAnalysis(SymTable symTab) {
        for (ExpNode node : myExps) {
            node.nameAnalysis(symTab);
        }
    }
    
    /**
     * typeCheck
     */
    public void typeCheck(List<Type> typeList) {
        int k = 0;
        try {
            for (ExpNode node : myExps) {
                Type actualType = node.typeCheck();     // actual type of arg
                
                if (!actualType.isErrorType()) {        // if this is not an error
                    Type formalType = typeList.get(k);  // get the formal type
                    if (!formalType.equals(actualType)) {
                        ErrMsg.fatal(node.lineNum(), node.charNum(),
                                     "Type of actual does not match type of formal");
                    }
                }
                k++;
            }
        } catch (NoSuchElementException e) {
            System.err.println("unexpected NoSuchElementException in ExpListNode.typeCheck");
            System.exit(-1);
        }
    }

	/**
	 * codeGen
	 */
	public void codeGen() {
		for(ExpNode node : myExps) {
			node.codeGen();
		}
	}
    
    public void unparse(PrintWriter p, int indent) {
        Iterator<ExpNode> it = myExps.iterator();
        if (it.hasNext()) { // if there is at least one element
            it.next().unparse(p, indent);
            while (it.hasNext()) {  // print the rest of the list
                p.print(", ");
                it.next().unparse(p, indent);
            }
        } 
    }
	
    // list of kids (ExpNodes)
    private List<ExpNode> myExps;
}

// **********************************************************************
// DeclNode and its subclasses
// **********************************************************************

abstract class DeclNode extends ASTnode {
    /**
     * Note: a formal decl needs to return a info
     */
    abstract public SymInfo nameAnalysis(SymTable symTab);

    // default version of typeCheck for non-function decls
    public void typeCheck() { }
	public void codeGen() { }
}

class VarDeclNode extends DeclNode {
    public VarDeclNode(TypeNode type, IdNode id, int size) {
        myType = type;
        myId = id;
        mySize = size;
    }

    /**
     * nameAnalysis (overloaded)
     * Given a symbol table symTab, do:
     * if this name is declared void, then error
     * else if the declaration is of a struct type, 
     *     lookup type name (globally)
     *     if type name doesn't exist, then error
     * if no errors so far,
     *     if name has already been declared in this scope, then error
     *     else add name to local symbol table     
     *
     * symTab is local symbol table (say, for struct field decls)
     * globalTab is global symbol table (for struct type names)
     * symTab and globalTab can be the same
     */
    public SymInfo nameAnalysis(SymTable symTab) {
        // return nameAnalysis(symTab, symTab);
        boolean badDecl = false;
        String name = myId.name();
        SymInfo info = null;
        IdNode structId = null;

        if (myType instanceof VoidNode) {  // check for void type
            ErrMsg.fatal(myId.lineNum(), myId.charNum(), 
                         "Non-function declared void");
            badDecl = true;        
        }
        else if (myType instanceof StructNode) {
            structId = ((StructNode) myType).idNode();
            info = symTab.lookupGlobal(structId.name());
            // if the name for the struct type is not found, 
            // or is not a struct type
            if (info == null || !(info instanceof StructDefInfo)) {
                ErrMsg.fatal(structId.lineNum(), structId.charNum(), 
                             "Invalid name of struct type");
                badDecl = true;
            }
            else {
                structId.link(info);
            }
        }
        SymInfo dup = symTab.lookupLocal(name);
        
        if (dup != null) {
            ErrMsg.fatal(myId.lineNum(), myId.charNum(), 
                         "Multiple declaration of identifier");
            badDecl = true;            
        }
        if (!badDecl) {  // insert into symbol table
            if (myType instanceof StructNode) {
                info = new StructInfo(structId);
            }
            else {
                info = new SymInfo(myType.type());
            }
            symTab.addDecl(name, info);
            myId.link(info);
        }
        
        return info;
    }
    
    
    public SymInfo nameAnalysis(SymTable structSymTab, SymTable globalTab) {
        boolean badDecl = false;
        String name = myId.name();
        SymInfo info = null;
        IdNode structId = null;

        if (myType instanceof VoidNode) {  // check for void type
            ErrMsg.fatal(myId.lineNum(), myId.charNum(), 
                         "Non-function declared void");
            badDecl = true;        
        }
        else if (myType instanceof StructNode) {
            structId = ((StructNode) myType).idNode();
            info = globalTab.lookupGlobal(structId.name());
            // if the name for the struct type is not found, 
            // or is not a struct type
            if (info == null || !(info instanceof StructDefInfo)) {
                ErrMsg.fatal(structId.lineNum(), structId.charNum(), 
                             "Invalid name of struct type");
                badDecl = true;
            }
            else {
                structId.link(info);
            }
        }
        SymInfo dup = structSymTab.lookupLocal(name);
        
        if (dup != null) {
            ErrMsg.fatal(myId.lineNum(), myId.charNum(), 
                         "Multiple declaration of struct field");
            badDecl = true;            
        }
        if (!badDecl) {  // insert into symbol table
            if (myType instanceof StructNode) {
                info = new StructInfo(structId);
            }
            else {
                info = new SymInfo(myType.type());
            }
            structSymTab.addDecl(name, info);
            myId.link(info);
        }
        
        return info;
    }    

	/**
	 * codeGen
	 */
	public void codeGen() {
		if(myId.isGlobal())
		{
			// Generate global variable header //
			Codegen.generate(".data");
			Codegen.generate(".align 2");
			Codegen.genLabel("_" + myId.name());
			Codegen.generate(".space 4");
		}
	}
    
    public void unparse(PrintWriter p, int indent) {
        doIndent(p, indent);
        myType.unparse(p, 0);
        p.print(" ");
        p.print(myId.name());
        p.println(";");
    }

    // 3 kids
    private TypeNode myType;
    private IdNode myId;
    private int mySize;  // use value NOT_STRUCT if this is not a struct type

    public static int NOT_STRUCT = -1;
}

class FnDeclNode extends DeclNode {
    public FnDeclNode(TypeNode type,
                      IdNode id,
                      FormalsListNode formalList,
                      FnBodyNode body) {
        myType = type;
        myId = id;
        myFormalsList = formalList;
        myBody = body;
    }

    /**
     * nameAnalysis
     * Given a symbol table symTab, do:
     * if this name has already been declared in this scope, then error
     * else add name to local symbol table
     * in any case, do the following:
     *     enter new scope
     *     process the formals
     *     if this function is not multiply declared,
     *         update symbol table entry with types of formals
     *     process the body of the function
     *     exit scope
     */
    public SymInfo nameAnalysis(SymTable symTab) {
        String name = myId.name();
        FnInfo info = null;

		// Flag that we saw main() //
		if(name.equals("main"))
			hasMain = true;
		// Set current function //
		currentFunction = myId.name();
        SymInfo dup=symTab.lookupLocal(name);
        if ( dup!= null) {
            ErrMsg.fatal(myId.lineNum(), myId.charNum(),
                         "Multiply declared identifier");
        }
        
        else { // add function name to local symbol table
            info = new FnInfo(myType.type(), myFormalsList.length());
            symTab.addDecl(name, info);
            myId.link(info);
        }
        
        symTab.addScope();  // add a new scope for locals and params
        
        // process the formals
        List<Type> typeList = myFormalsList.nameAnalysis(symTab);
        if (info != null) {
            info.addFormals(typeList);
        }
        
        myBody.nameAnalysis(symTab); // process the function body
        
        symTab.removeScope();  // exit scope

		//TODO how do handle the variant size of structs???
		// formals //
		this.formalsSize = typeList.size() * 4;
		// locals //
		this.localsSize = myBody.numLocals() * 4;
        
        return null;
    } 
       
    /**
     * typeCheck
     */
    public void typeCheck() {
        myBody.typeCheck(myType.type());
    }

	/**
	 * codeGen
	 */
	public void codeGen() {
		// Preamble //

		if(myId.name().equals("main")){
			Codegen.genLabel("main");
			Codegen.genLabel("__start");
			Codegen.generate("\t#Begin Function Prologue");
		}
		else
			Codegen.genLabel("_" + myId.name()); // normal functions
		String jump_label = Codegen.nextLabel();
		// Push return address
		Codegen.genPush(Codegen.RA);
		// Push control link
		Codegen.genPush(Codegen.FP);
		// Set FP //
		Codegen.generate("addu", Codegen.FP, Codegen.SP, (formalsSize * 4) + 8);
		Codegen.generate("\t#Begin Function Body");
		// codeGen() the body //
		myBody.codeGen();

		// Epilogue //
		Codegen.generate("\t#Begin Function Epilogue");
		Codegen.genLabel(jump_label);
		// Return addr //
		Codegen.generateIndexed("lw", Codegen.RA, Codegen.FP, -formalsSize);
		// Control Link //
		Codegen.generate("move", Codegen.T0, Codegen.FP);
		// Restore FP //
		Codegen.generateIndexed("lw", Codegen.FP, Codegen.FP, -formalsSize - 4);
		// Restore SP //
		Codegen.generate("move", Codegen.SP, Codegen.T0);
		
		// Return from function - also handle main //
		if(myId.name().equals("main")){
			Codegen.generate("li", Codegen.V0, 10);
			Codegen.generate("syscall");
		}
		else
			Codegen.generate("jr", Codegen.RA); // return


	}
        
    public void unparse(PrintWriter p, int indent) {
        doIndent(p, indent);
        myType.unparse(p, 0);
        p.print(" ");
        p.print(myId.name());
        p.print("(");
        myFormalsList.unparse(p, 0);
        p.println(") {");
        myBody.unparse(p, indent+4);
        p.println("}\n");
    }

    // 4 kids
    private TypeNode myType;
    private IdNode myId;
    private FormalsListNode myFormalsList;
    private FnBodyNode myBody;
	// Keep track of nodes formals and locals size //
	private int formalsSize;
	private int localsSize;
}

class FormalDeclNode extends DeclNode {
    public FormalDeclNode(TypeNode type, IdNode id) {
        myType = type;
        myId = id;
    }

    /**
     * nameAnalysis
     * Given a symbol table symTab, do:
     * if this formal is declared void, then error
     * else if this formal is already in the local symble table,
     *     then issue multiply declared error message and return null
     * else add a new entry to the symbol table and return that Sym
     */
    public SymInfo nameAnalysis(SymTable symTab) {
        String name = myId.name();
        boolean badDecl = false;
        SymInfo sym = null;
        
        if (myType instanceof VoidNode) {
            ErrMsg.fatal(myId.lineNum(), myId.charNum(), 
                         "Non-function declared void");
            badDecl = true;        
        }
        
        if (symTab.lookupLocal(name) != null) {
            ErrMsg.fatal(myId.lineNum(), myId.charNum(), 
                         "Multiply declared identifier");
            badDecl = true;
        }
        
        if (!badDecl) {  // insert into symbol table
            try {
                sym = new SymInfo(myType.type());
                symTab.addDecl(name, sym);
                myId.link(sym);
            } catch (DuplicateSymException ex) {
                System.err.println("Unexpected DuplicateSymException " +
                                   " in VarDeclNode.nameAnalysis");
                System.exit(-1);
            } catch (EmptySymTableException ex) {
                System.err.println("Unexpected EmptySymTableException " +
                                   " in VarDeclNode.nameAnalysis");
                System.exit(-1);
            }
        }
        
        return sym;
    }    
    
    public void unparse(PrintWriter p, int indent) {
        myType.unparse(p, 0);
        p.print(" ");
        p.print(myId.name());
    }

    // 2 kids
    private TypeNode myType;
    private IdNode myId;
}

class StructDeclNode extends DeclNode {
    public StructDeclNode(IdNode id, DeclListNode declList) {
        myId = id;
        myDeclList = declList;
    }

    /**
     * nameAnalysis
     * Given a symbol table symTab, do:
     * if this name is already in the symbol table,
     *     then multiply declared error (don't add to symbol table)
     * create a new symbol table for this struct definition
     * process the decl list
     * if no errors
     *     add a new entry to symbol table for this struct
     */
    public SymInfo nameAnalysis(SymTable symTab) {
        String name = myId.name();
        boolean badDecl = false;

		// Set size of struct //
		this.bodySize = this.myDeclList.numDecl();
        
        if (symTab.lookupLocal(name) != null) {
            ErrMsg.fatal(myId.lineNum(), myId.charNum(), 
                         "Multiply declared identifier");
            badDecl = true;            
        }

        SymTable structSymTab = new SymTable();
        
        // process the fields of the struct
        myDeclList.nameAnalysis(structSymTab, symTab);
        
        if (!badDecl) {
            StructDefInfo sym = new StructDefInfo(structSymTab);
            symTab.addDecl(name, sym);
            myId.link(sym);
        }
        
        return null;
    }    
    
    public void unparse(PrintWriter p, int indent) {
        doIndent(p, indent);
        p.print("struct ");
        p.print(myId.name());
        p.println("{");
        myDeclList.unparse(p, indent+4);
        doIndent(p, indent);
        p.println("};\n");

    }

	// Return size of struct //
	public int size() {
		return this.bodySize;
	}

    // 2 kids
    private IdNode myId;
    private DeclListNode myDeclList;
	// Size of struct body //
	private int bodySize;
}

// **********************************************************************
// TypeNode and its Subclasses
// **********************************************************************

abstract class TypeNode extends ASTnode {
    /* all subclasses must provide a type method */
    abstract public Type type();
}

class IntNode extends TypeNode {
    public IntNode() {
    }

    /**
     * type
     */
    public Type type() {
        return new IntType();
    }
    
    public void unparse(PrintWriter p, int indent) {
        p.print("int");
    }
}

class BoolNode extends TypeNode {
    public BoolNode() {
    }

    /**
     * type
     */
    public Type type() {
        return new BoolType();
    }
    
    public void unparse(PrintWriter p, int indent) {
        p.print("bool");
    }
}

class VoidNode extends TypeNode {
    public VoidNode() {
    }
    
    /**
     * type
     */
    public Type type() {
        return new VoidType();
    }
    
    public void unparse(PrintWriter p, int indent) {
        p.print("void");
    }
}

class StructNode extends TypeNode {
    public StructNode(IdNode id) {
        myId = id;
    }

    public IdNode idNode() {
        return myId;
    }
    
    /**
     * type
     */
    public Type type() {
        return new StructType(myId);
    }
    
    public void unparse(PrintWriter p, int indent) {
        p.print("struct ");
        p.print(myId.name());
    }
    
    // 1 kid
    private IdNode myId;
}

// **********************************************************************
// StmtNode and its subclasses
// **********************************************************************

abstract class StmtNode extends ASTnode {
    abstract public void nameAnalysis(SymTable symTab);
    abstract public void typeCheck(Type retType);
	abstract public void codeGen();
}

class AssignStmtNode extends StmtNode {
    public AssignStmtNode(AssignNode assign) {
        myAssign = assign;
    }

    /**
     * nameAnalysis
     * Given a symbol table symTab, perform name analysis on this node's child
     */
    public void nameAnalysis(SymTable symTab) {
        myAssign.nameAnalysis(symTab);
    }
    
    /**
     * typeCheck
     */
    public void typeCheck(Type retType) {
        myAssign.typeCheck();
    }

	/**
	 * codeGen
	 */
	public void codeGen() {
		myAssign.codeGen();
		// Pop intermediate result onto stack //
		Codegen.genPop(Codegen.T0);
	}
        
    public void unparse(PrintWriter p, int indent) {
        doIndent(p, indent);
        myAssign.unparse(p, -1); // no parentheses
        p.println(";");
    }

    // 1 kid
    private AssignNode myAssign;
}

class PostIncStmtNode extends StmtNode {
    public PostIncStmtNode(ExpNode exp) {
        myExp = exp;
    }
    
    /**
     * nameAnalysis
     * Given a symbol table symTab, perform name analysis on this node's child
     */
    public void nameAnalysis(SymTable symTab) {
        myExp.nameAnalysis(symTab);
    }
    
    /**
     * typeCheck
     */
    public void typeCheck(Type retType) {
        Type type = myExp.typeCheck();
        
        if (!type.isErrorType() && !type.isIntType()) {
            ErrMsg.fatal(myExp.lineNum(), myExp.charNum(),
                         "Arithmetic operator applied to non-numeric operand");
        }
    }

	/**
	 * codeGen
	 */
	public void codeGen() {
		myExp.codeGen();
		((IdNode)myExp).genAddr();
		// addr into T0 //
		Codegen.genPop(Codegen.T0);
		// value into T1 //
		Codegen.genPop(Codegen.T1);
		// Do the increment //
		Codegen.generate("add", Codegen.T1, Codegen.T1, 1); // ++
		Codegen.generateIndexed("sw", Codegen.T1, Codegen.T0, 0);
	}
        
    public void unparse(PrintWriter p, int indent) {
        doIndent(p, indent);
        myExp.unparse(p, 0);
        p.println("++;");
    }

    // 1 kid
    private ExpNode myExp;
}

class PostDecStmtNode extends StmtNode {
    public PostDecStmtNode(ExpNode exp) {
        myExp = exp;
    }

    /**
     * nameAnalysis
     * Given a symbol table symTab, perform name analysis on this node's child
     */
    public void nameAnalysis(SymTable symTab) {
        myExp.nameAnalysis(symTab);
    }
    
    /**
     * typeCheck
     */
    public void typeCheck(Type retType) {
        Type type = myExp.typeCheck();
        
        if (!type.isErrorType() && !type.isIntType()) {
            ErrMsg.fatal(myExp.lineNum(), myExp.charNum(),
                         "Arithmetic operator applied to non-numeric operand");
        }
    }

	/**
	 * codeGen
	 */
	public void codeGen() {
		myExp.codeGen();
		((IdNode)myExp).genAddr();
		// addr into T0 //
		Codegen.genPop(Codegen.T0);
		// value into T1 //
		Codegen.genPop(Codegen.T1);
		// Do the increment //
		Codegen.generate("add", Codegen.T1, Codegen.T1, -1); // ++
		Codegen.generateIndexed("sw", Codegen.T1, Codegen.T0, 0);
	}
              
    public void unparse(PrintWriter p, int indent) {
        doIndent(p, indent);
        myExp.unparse(p, 0);
        p.println("--;");
    }
    
    // 1 kid
    private ExpNode myExp;
}

class ReadStmtNode extends StmtNode {
    public ReadStmtNode(ExpNode e) {
        myExp = e;
    }

    /**
     * nameAnalysis
     * Given a symbol table symTab, perform name analysis on this node's child
     */
    public void nameAnalysis(SymTable symTab) {
        myExp.nameAnalysis(symTab);
    }    
 
    /**
     * typeCheck
     */
    public void typeCheck(Type retType) {
        Type type = myExp.typeCheck();
        
        if (type.isFnType()) {
            ErrMsg.fatal(myExp.lineNum(), myExp.charNum(),
                         "Attempt to read a function");
        }
        
        if (type.isStructDefType()) {
            ErrMsg.fatal(myExp.lineNum(), myExp.charNum(),
                         "Attempt to read a struct name");
        }
        
        if (type.isStructType()) {
            ErrMsg.fatal(myExp.lineNum(), myExp.charNum(),
                         "Attempt to read a struct variable");
        }
    }

	/**
	 * codeGen
	 */
	public void codeGen() {
		if(myExp instanceof IdNode){
			((IdNode)myExp).genAddr();
		}
		// Syscall 5 //
		Codegen.generate("li", Codegen.V0, 5);
		Codegen.generate("syscall");

		// Get addr //
		Codegen.genPop(Codegen.T0);
		Codegen.generateIndexed("sw", Codegen.V0, Codegen.T0, 0);
	}
    
    public void unparse(PrintWriter p, int indent) {
        doIndent(p, indent);
        p.print("cin >> ");
        myExp.unparse(p, 0);
        p.println(";");
    }

    // 1 kid (actually can only be an IdNode or an ArrayExpNode)
    private ExpNode myExp;
}

class WriteStmtNode extends StmtNode {
    public WriteStmtNode(ExpNode exp) {
        myExp = exp;
    }

    /**
     * nameAnalysis
     * Given a symbol table symTab, perform name analysis on this node's child
     */
    public void nameAnalysis(SymTable symTab) {
        myExp.nameAnalysis(symTab);
    }

    /**
     * typeCheck
     */
    public void typeCheck(Type retType) {
        Type type = myExp.typeCheck();
        
        if (type.isFnType()) {
            ErrMsg.fatal(myExp.lineNum(), myExp.charNum(),
                         "Attempt to write a function");
        }
        
        if (type.isStructDefType()) {
            ErrMsg.fatal(myExp.lineNum(), myExp.charNum(),
                         "Attempt to write a struct name");
        }
        
        if (type.isStructType()) {
            ErrMsg.fatal(myExp.lineNum(), myExp.charNum(),
                         "Attempt to write a struct variable");
        }
        
        if (type.isVoidType()) {
            ErrMsg.fatal(myExp.lineNum(), myExp.charNum(),
                         "Attempt to write void");
        }
    }

	/**
	 * codeGen
	 */
	public void codeGen() {
		int syscall = 1;

		if(myExp instanceof StringLitNode)
			syscall = 4;

		myExp.codeGen();
		Codegen.genPop(Codegen.A0);
		String comment = "System call for printing string";
		Codegen.generateWithComment("li", comment, Codegen.V0, syscall+"");
		Codegen.generate("syscall");
	}
        
    public void unparse(PrintWriter p, int indent) {
        doIndent(p, indent);
        p.print("cout << ");
        myExp.unparse(p, 0);
        p.println(";");
    }

    // 1 kid
    private ExpNode myExp;
}

class IfStmtNode extends StmtNode {
    public IfStmtNode(ExpNode exp, DeclListNode dlist, StmtListNode slist) {
        myDeclList = dlist;
        myExp = exp;
        myStmtList = slist;
    }
    
    /**
     * nameAnalysis
     * Given a symbol table symTab, do:
     * - process the condition
     * - enter a new scope
     * - process the decls and stmts
     * - exit the scope
     */
    public void nameAnalysis(SymTable symTab) {
        myExp.nameAnalysis(symTab);
        symTab.addScope();
        myDeclList.nameAnalysis(symTab);
        myStmtList.nameAnalysis(symTab);
        symTab.removeScope();
    }
    
     /**
     * typeCheck
     */
    public void typeCheck(Type retType) {
        Type type = myExp.typeCheck();
        
        if (!type.isErrorType() && !type.isBoolType()) {
            ErrMsg.fatal(myExp.lineNum(), myExp.charNum(),
                         "Non-bool expression used as an if condition");        
        }
        
        myStmtList.typeCheck(retType);
    }

	/**
	 * codeGen
	 */
	public void codeGen() {
		String jump_label = Codegen.nextLabel();

		// eval //
		myExp.codeGen();

		// get the return value //
		Codegen.genPop(Codegen.T0);
		Codegen.generate("beqz", Codegen.T0, "0", jump_label);

		myDeclList.codeGen();
		myStmtList.codeGen();

		// false label //
		Codegen.genLabel(jump_label);
	}
       
    public void unparse(PrintWriter p, int indent) {
        doIndent(p, indent);
        p.print("if (");
        myExp.unparse(p, 0);
        p.println(") {");
        myDeclList.unparse(p, indent+4);
        myStmtList.unparse(p, indent+4);
        doIndent(p, indent);
        p.println("}");
    }

    // e kids
    private ExpNode myExp;
    private DeclListNode myDeclList;
    private StmtListNode myStmtList;
}

class IfElseStmtNode extends StmtNode {
    public IfElseStmtNode(ExpNode exp, DeclListNode dlist1,
                          StmtListNode slist1, DeclListNode dlist2,
                          StmtListNode slist2) {
        myExp = exp;
        myThenDeclList = dlist1;
        myThenStmtList = slist1;
        myElseDeclList = dlist2;
        myElseStmtList = slist2;
    }
    
    /**
     * nameAnalysis
     * Given a symbol table symTab, do:
     * - process the condition
     * - enter a new scope
     * - process the decls and stmts of then
     * - exit the scope
     * - enter a new scope
     * - process the decls and stmts of else
     * - exit the scope
     */
    public void nameAnalysis(SymTable symTab) {
        myExp.nameAnalysis(symTab);
        symTab.addScope();
        myThenDeclList.nameAnalysis(symTab);
        myThenStmtList.nameAnalysis(symTab);
        symTab.removeScope();
        symTab.addScope();
        myElseDeclList.nameAnalysis(symTab);
        myElseStmtList.nameAnalysis(symTab);
        symTab.removeScope();
    }
    
    /**
     * typeCheck
     */
    public void typeCheck(Type retType) {
        Type type = myExp.typeCheck();
        
        if (!type.isErrorType() && !type.isBoolType()) {
            ErrMsg.fatal(myExp.lineNum(), myExp.charNum(),
                         "Non-bool expression used as an if condition");        
        }
        
        myThenStmtList.typeCheck(retType);
        myElseStmtList.typeCheck(retType);
    }

	/**
	 * codeGen
	 */
	public void codeGen() {
		String false_label = Codegen.nextLabel();
		String end_label = Codegen.nextLabel();

		myExp.codeGen();

		// Get evaluated //
		Codegen.genPop(Codegen.T0);
		// go to else if its false //
		Codegen.generate("beqz", Codegen.T0, "0", false_label);
		myThenDeclList.codeGen();
		myThenStmtList.codeGen();
		Codegen.generate("b", end_label);
		// else case //
		Codegen.genLabel(false_label);
		myElseDeclList.codeGen();
		myElseStmtList.codeGen();
		// end label //
		Codegen.genLabel(end_label);

	}
        
    public void unparse(PrintWriter p, int indent) {
        doIndent(p, indent);
        p.print("if (");
        myExp.unparse(p, 0);
        p.println(") {");
        myThenDeclList.unparse(p, indent+4);
        myThenStmtList.unparse(p, indent+4);
        doIndent(p, indent);
        p.println("}");
        doIndent(p, indent);
        p.println("else {");
        myElseDeclList.unparse(p, indent+4);
        myElseStmtList.unparse(p, indent+4);
        doIndent(p, indent);
        p.println("}");        
    }

    // 5 kids
    private ExpNode myExp;
    private DeclListNode myThenDeclList;
    private StmtListNode myThenStmtList;
    private StmtListNode myElseStmtList;
    private DeclListNode myElseDeclList;
}

class WhileStmtNode extends StmtNode {
    public WhileStmtNode(ExpNode exp, DeclListNode dlist, StmtListNode slist) {
        myExp = exp;
        myDeclList = dlist;
        myStmtList = slist;
    }
    
    /**
     * nameAnalysis
     * Given a symbol table symTab, do:
     * - process the condition
     * - enter a new scope
     * - process the decls and stmts
     * - exit the scope
     */
    public void nameAnalysis(SymTable symTab) {
        myExp.nameAnalysis(symTab);
        symTab.addScope();
        myDeclList.nameAnalysis(symTab);
        myStmtList.nameAnalysis(symTab);
        symTab.removeScope();
    }
    
    /**
     * typeCheck
     */
    public void typeCheck(Type retType) {
        Type type = myExp.typeCheck();
        
        if (!type.isErrorType() && !type.isBoolType()) {
            ErrMsg.fatal(myExp.lineNum(), myExp.charNum(),
                         "Non-bool expression used as a while condition");        
        }
        
        myStmtList.typeCheck(retType);
    }

	/**
	 * codeGen
	 */
	public void codeGen() {
		// Loop label //
		String loop = Codegen.nextLabel();
		String end = Codegen.nextLabel();
		// top of while //
		Codegen.genLabel(loop);
		myExp.codeGen();
		Codegen.genPop(Codegen.T0);
		Codegen.generate("beq", Codegen.T0, "0", end); // break while
		myDeclList.codeGen(); // hmm we dont want to repeat this doe?
		myStmtList.codeGen();
		Codegen.generate("b", loop);
		// Finish //
		Codegen.genLabel(end);
	}
        
    public void unparse(PrintWriter p, int indent) {
        doIndent(p, indent);
        p.print("while (");
        myExp.unparse(p, 0);
        p.println(") {");
        myDeclList.unparse(p, indent+4);
        myStmtList.unparse(p, indent+4);
        doIndent(p, indent);
        p.println("}");
    }

    // 3 kids
    private ExpNode myExp;
    private DeclListNode myDeclList;
    private StmtListNode myStmtList;
}

class CallStmtNode extends StmtNode {
    public CallStmtNode(CallExpNode call) {
        myCall = call;
    }
    
    /**
     * nameAnalysis
     * Given a symbol table symTab, perform name analysis on this node's child
     */
    public void nameAnalysis(SymTable symTab) {
        myCall.nameAnalysis(symTab);
    }
    
    /**
     * typeCheck
     */
    public void typeCheck(Type retType) {
        myCall.typeCheck();
    }

	/**
	 * codeGen
	 */
	public void codeGen() {
		myCall.codeGen();
		// pop ret value //
		Codegen.genPop(Codegen.V0);
	}
    
    public void unparse(PrintWriter p, int indent) {
        doIndent(p, indent);
        myCall.unparse(p, indent);
        p.println(";");
    }

    // 1 kid
    private CallExpNode myCall;
}

class ReturnStmtNode extends StmtNode {
    public ReturnStmtNode(ExpNode exp) {
        this(exp,0,0);
    }

    public ReturnStmtNode(ExpNode exp, int charnum, int linenum) {
        myExp = exp;
        myCharnum = charnum;
        myLinenum = linenum;
    }
    
    /**
     * nameAnalysis
     * Given a symbol table symTab, perform name analysis on this node's child,
     * if it has one
     */
    public void nameAnalysis(SymTable symTab) {
        if (myExp != null) {
            myExp.nameAnalysis(symTab);
        }
    }

    /**
     * typeCheck
     */
    public void typeCheck(Type retType) {
        if (myExp != null) {  // return value given
            Type type = myExp.typeCheck();
            
            if (retType.isVoidType()) {
                ErrMsg.fatal(myExp.lineNum(), myExp.charNum(),
                             "Return with a value in a void function");                
            }
            
            else if (!retType.isErrorType() && !type.isErrorType() && !retType.equals(type)){
                ErrMsg.fatal(myExp.lineNum(), myExp.charNum(),
                             "Bad return value");
            }
        }
        
        else {  // no return value given -- ok if this is a void function
            if (!retType.isVoidType()) {
                ErrMsg.fatal(myLinenum, myCharnum, "Missing return value");                
            }
        }
        
    }

	/**
	 * codeGen
	 */
	public void codeGen() {
		if(myExp != null){
			myExp.codeGen();
			Codegen.genPop(Codegen.V0);
		}

		Codegen.generate("b", currentFunction);
	}
    
    public void unparse(PrintWriter p, int indent) {
        doIndent(p, indent);
        p.print("return");
        if (myExp != null) {
            p.print(" ");
            myExp.unparse(p, 0);
        }
        p.println(";");
    }

    // 1 kid
    private ExpNode myExp; // possibly null
	private int myCharnum;
    private int myLinenum;
}

// **********************************************************************
// ExpNode and its subclasses
// **********************************************************************

abstract class ExpNode extends ASTnode {
    /**
     * Default version for nodes with no names
     */
    public void nameAnalysis(SymTable symTab) { }
    
    abstract public Type typeCheck();
	abstract public void codeGen();
    abstract public int lineNum();
    abstract public int charNum();
}

class IntLitNode extends ExpNode {
    public IntLitNode(int lineNum, int charNum, int intVal) {
        myLineNum = lineNum;
        myCharNum = charNum;
        myIntVal = intVal;
    }
    public void unparse(PrintWriter p, int indent) {
        p.print(myIntVal);
    }
    
    /**
     * Return the line number for this literal.
     */
    public int lineNum() {
        return myLineNum;
    }
    
    /**
     * Return the char number for this literal.
     */
    public int charNum() {
        return myCharNum;
    }
        
    /**
     * typeCheck
     */
    public Type typeCheck() {
        return new IntType();
    }

	/**
	 * codeGen
	 */
	public void codeGen() {
		// load value and push onto stack //
		Codegen.generate("li", Codegen.T0, myIntVal);
		Codegen.genPush(Codegen.T0);
	}
    
   // public void unparse(PrintWriter p, int indent) {
       // p.print(myIntVal);
    //}

    private int myLineNum;
    private int myCharNum;
    private int myIntVal;
}

class StringLitNode extends ExpNode {
    public StringLitNode(int lineNum, int charNum, String strVal) {
        myLineNum = lineNum;
        myCharNum = charNum;
        myStrVal = strVal;
    }
    
    /**
     * Return the line number for this literal.
     */
    public int lineNum() {
        return myLineNum;
    }
    
    /**
     * Return the char number for this literal.
     */
    public int charNum() {
        return myCharNum;
    }
    
    /**
     * typeCheck
     */
    public Type typeCheck() {
        return new StringType();
    }

	/**
	 * codeGen
	 */
	public void codeGen() {
		// Handle duplicates somehow? TODO if I have time //
		String l = Codegen.nextLabel();
		Codegen.generate(".data");
		Codegen.generateLabeled(l, ".asciiz ", "", myStrVal);
		// PUsh address //
		Codegen.generate(".text");
		Codegen.generate("la", Codegen.T0, l);
		Codegen.genPush(Codegen.T0);
	}
        
    public void unparse(PrintWriter p, int indent) {
        p.print(myStrVal);
    }

    private int myLineNum;
    private int myCharNum;
    private String myStrVal;
}

class TrueNode extends ExpNode {
    public TrueNode(int lineNum, int charNum) {
        myLineNum = lineNum;
        myCharNum = charNum;
    }

    /**
     * Return the line number for this literal.
     */
    public int lineNum() {
        return myLineNum;
    }
    
    /**
     * Return the char number for this literal.
     */
    public int charNum() {
        return myCharNum;
    }
    
    /**
     * typeCheck
     */
    public Type typeCheck() {
        return new BoolType();
    }

	/**
	 * codeGen
	 */
	public void codeGen() {
		Codegen.generate("li", Codegen.T0, Codegen.TRUE);
		Codegen.genPush(Codegen.T0);
	}
        
    public void unparse(PrintWriter p, int indent) {
        p.print("true");
    }

    private int myLineNum;
    private int myCharNum;
}

class FalseNode extends ExpNode {
    public FalseNode(int lineNum, int charNum) {
        myLineNum = lineNum;
        myCharNum = charNum;
    }

    /**
     * Return the line number for this literal.
     */
    public int lineNum() {
        return myLineNum;
    }
    
    /**
     * Return the char number for this literal.
     */
    public int charNum() {
        return myCharNum;
    }

    /**
     * typeCheck
     */
    public Type typeCheck() {
        return new BoolType();
    }

	/**
	 * codeGen
	 */
	public void codeGen() {
		Codegen.generate("li", Codegen.T0, Codegen.FALSE);
		Codegen.genPush(Codegen.T0);
	}
        
    public void unparse(PrintWriter p, int indent) {
        p.print("false");
    }

    private int myLineNum;
    private int myCharNum;
}

class IdNode extends ExpNode {
    public IdNode(int lineNum, int charNum, String strVal) {
        myLineNum = lineNum;
        myCharNum = charNum;
        myStrVal = strVal;
    }

    /**
     * Link the given symbol to this ID.
     */
    public void link(SymInfo info) {
        myInfo = info;
    }
    
    /**
     * Return the name of this ID.
     */
    public String name() {
        return myStrVal;
    }
    
    /**
     * Return the symbol associated with this ID.
     */
    public SymInfo info() {
        return myInfo;
    }

	/**
	 * Return if symbol is global
	 */
	public boolean isGlobal() {
		return myInfo.isGlobal();
	}
    
    /**
     * Return the line number for this ID.
     */
    public int lineNum() {
        return myLineNum;
    }
    
    /**
     * Return the char number for this ID.
     */
    public int charNum() {
        return myCharNum;
    }    
    
    /**
     * nameAnalysis
     * Given a symbol table symTab, do:
     * - check for use of undeclared name
     * - if ok, link to symbol table entry
     */
     public void nameAnalysis(SymTable symTab) {
        SymInfo info = symTab.lookupGlobal(myStrVal);
        
        if (info == null) {
            ErrMsg.fatal(myLineNum, myCharNum, "Undeclared identifier");
        } else {
            link(info);
        }
    }
 
    /**
     * typeCheck
     */
    public Type typeCheck() {
        if (myInfo != null) {
            return myInfo.getType();
        } 
        else {
            System.err.println("ID with null info field in IdNode.typeCheck");
            System.exit(-1);
        }
        return null;
    }

	/**
	 * codeGen
	 */
	public void codeGen() {
		if(myInfo.isGlobal())
			Codegen.generate("lw", Codegen.T0, "_" + myStrVal);
		else
			Codegen.generateIndexed("lw", Codegen.T0, Codegen.FP, -myInfo.getOffset());
		
	}

	// linking back //
	public void genJumpAndLink() {
		Codegen.generate("jal", "_" + myStrVal);
	}

	// generate address //
	public void genAddr() {
		if(myInfo.isGlobal()) {
			Codegen.generate("la", Codegen.T0, "_" + myStrVal);
		} else {
			Codegen.generateIndexed("la", Codegen.T0, Codegen.FP, -myInfo.getOffset());
		}
	}
           
    public void unparse(PrintWriter p, int indent) {
        p.print(myStrVal);
        if (myInfo != null) {
            p.print("(" + myInfo + ")");
        }
    }

    private int myLineNum;
    private int myCharNum;
    private String myStrVal;
    private SymInfo myInfo;
}

class DotAccessExpNode extends ExpNode {
    public DotAccessExpNode(ExpNode lhs, IdNode id) {
        myLhs = lhs;    
        myId = id;
        myInfo = null;
    }

    /**
     * Return the info associated with this dot-access node.
     */
    public SymInfo info() {
        return myInfo;
    }    
    
    /**
     * Return the line number for this dot-access node. 
     * The line number is the one corresponding to the RHS of the dot-access.
     */
    public int lineNum() {
        return myId.lineNum();
    }
    
    /**
     * Return the char number for this dot-access node.
     * The char number is the one corresponding to the RHS of the dot-access.
     */
    public int charNum() {
        return myId.charNum();
    }
    
    /**
     * nameAnalysis
     * Given a symbol table symTab, do:
     * - process the LHS of the dot-access
     * - process the RHS of the dot-access
     * - if the RHS is of a struct type, set the sym for this node so that
     *   a dot-access "higher up" in the AST can get access to the symbol
     *   table for the appropriate struct definition
     */
    public void nameAnalysis(SymTable symTab) {
        badAccess = false;
        SymTable structSymTab = null; // to lookup RHS of dot-access
        SymInfo info = null;
        
        myLhs.nameAnalysis(symTab);  // do name analysis on LHS
        
        // if myLhs is really an ID, then info will be a link to the ID's symbol
        if (myLhs instanceof IdNode) {
            IdNode id = (IdNode)myLhs;
            info = id.info();
            
            // check ID has been declared to be of a struct type
            
            if (info == null) { // ID was undeclared
                badAccess = true;
            }
            else if (info instanceof StructInfo) { 
                // get symbol table for struct type
                SymInfo tempSym = ((StructInfo)info).getStructType().info();
                structSymTab = ((StructDefInfo)tempSym).getSymTable();
            } 
            else {  // LHS is not a struct type
                ErrMsg.fatal(id.lineNum(), id.charNum(), 
                             "Dot-access of non-struct type");
                badAccess = true;
            }
        }
        
        // if myLhs is really a dot-access (i.e., myLhs was of the form
        // LHSloc.RHSid), then info will either be
        // null - indicating RHSid is not of a struct type, or
        // a link to the Sym for the struct type RHSid was declared to be
        else if (myLhs instanceof DotAccessExpNode) {
            DotAccessExpNode lhs = (DotAccessExpNode)myLhs;
            
            if (lhs.badAccess) {  // if errors in processing myLhs
                badAccess = true; // don't continue proccessing this dot-access
            }
            else { //  no errors in processing myLhs
                info = lhs.info();

                if (info == null) {  // no struct in which to look up RHS
                    ErrMsg.fatal(lhs.lineNum(), lhs.charNum(), 
                                 "Dot-access of non-struct type");
                    badAccess = true;
                }
                else {  // get the struct's symbol table in which to lookup RHS
                    if (info instanceof StructDefInfo) {
                        structSymTab = ((StructDefInfo)info).getSymTable();
                    }
                    else {
                        System.err.println("Unexpected Sym type in DotAccessExpNode");
                        System.exit(-1);
                    }
                }
            }

        }
        
        else { // don't know what kind of thing myLhs is
            System.err.println("Unexpected node type in LHS of dot-access");
            System.exit(-1);
        }
        
        // do name analysis on RHS of dot-access in the struct's symbol table
        if (!badAccess) {
            info = structSymTab.lookupGlobal(myId.name()); // lookup
                
            if (info == null) { // not found - RHS is not a valid field name
                ErrMsg.fatal(myId.lineNum(), myId.charNum(), 
                             "Invalid struct field name");
                badAccess = true;
            }
            
            else {
                myId.link(info);  // link the symbol
                // if RHS is itself as struct type, link the symbol for its struct 
                // type to this dot-access node (to allow chained dot-access)
                if (info instanceof StructInfo) {
                    myInfo = ((StructInfo)info).getStructType().info();
                }
            }
        }
    }    
 
    /**
     * typeCheck
     */
    public Type typeCheck() {
        return myId.typeCheck();
    }
    public void codeGen() {}
    public void unparse(PrintWriter p, int indent) {
        myLhs.unparse(p, 0);
        p.print(".");
        myId.unparse(p, 0);
        if (myInfo != null) {
            p.print("(" + myInfo + ")");
        }
    }

    // 2 kids
    private ExpNode myLhs;    
    private IdNode myId;
    private SymInfo myInfo;     // link to SymInfo for struct type
    private boolean badAccess;  // to prevent multiple, cascading errors
}
class AssignNode extends ExpNode {
    public AssignNode(ExpNode lhs, ExpNode exp) {
        myLhs = lhs;
        myRhs = exp;
    }
    
    /**
     * Return the line number for this assignment node. 
     * The line number is the one corresponding to the left operand.
     */
    public int lineNum() {
        return myLhs.lineNum();
    }
    
    /**
     * Return the char number for this assignment node.
     * The char number is the one corresponding to the left operand.
     */
    public int charNum() {
        return myLhs.charNum();
    }
    
    /**
     * nameAnalysis
     * Given a symbol table symTab, perform name analysis on this node's 
     * two children
     */
    public void nameAnalysis(SymTable symTab) {
        myLhs.nameAnalysis(symTab);
        myRhs.nameAnalysis(symTab);
    }
 
    /**
     * typeCheck
     */
    public Type typeCheck() {
        Type typeLhs = myLhs.typeCheck();
        Type typeExp = myRhs.typeCheck();
        Type retType = typeLhs;
        
        if (typeLhs.isFnType() && typeExp.isFnType()) {
            ErrMsg.fatal(lineNum(), charNum(), "Function assignment");
            retType = new ErrorType();
        }
        
        if (typeLhs.isStructDefType() && typeExp.isStructDefType()) {
            ErrMsg.fatal(lineNum(), charNum(), "Struct name assignment");
            retType = new ErrorType();
        }
        
        if (typeLhs.isStructType() && typeExp.isStructType()) {
            ErrMsg.fatal(lineNum(), charNum(), "Struct variable assignment");
            retType = new ErrorType();
        }        
        
        if (!typeLhs.equals(typeExp) && !typeLhs.isErrorType() && !typeExp.isErrorType()) {
            ErrMsg.fatal(lineNum(), charNum(), "Type mismatch");
            retType = new ErrorType();
        }
        
        if (typeLhs.isErrorType() || typeExp.isErrorType()) {
            retType = new ErrorType();
        }
        
        return retType;
    }

	/**
	 * codeGen
	 */
	public void codeGen() {
		// RHS
		myRhs.codeGen();
		// LHS
		((IdNode)myLhs).genAddr();
		// get address and the load value //
		Codegen.genPop(Codegen.T0);
		Codegen.genPop(Codegen.T1);
		Codegen.generateIndexed("sw", Codegen.T1, Codegen.T0, 0);

		// return operand //
		Codegen.genPush(Codegen.T1);
	}
    
    public void unparse(PrintWriter p, int indent) {
        if (indent != -1)  p.print("(");
        myLhs.unparse(p, 0);
        p.print(" = ");
        myRhs.unparse(p, 0);
        if (indent != -1)  p.print(")");
    }

    // 2 kids
    private ExpNode myLhs;
    private ExpNode myRhs;
}

class CallExpNode extends ExpNode {
    public CallExpNode(IdNode name, ExpListNode elist) {
        myId = name;
        myExpList = elist;
    }

    public CallExpNode(IdNode name) {
        myId = name;
        myExpList = new ExpListNode(new LinkedList<ExpNode>());
    }

    /**
     * Return the line number for this call node. 
     * The line number is the one corresponding to the function name.
     */
    public int lineNum() {
        return myId.lineNum();
    }
    
    /**
     * Return the char number for this call node.
     * The char number is the one corresponding to the function name.
     */
    public int charNum() {
        return myId.charNum();
    }
    
    /**
     * nameAnalysis
     * Given a symbol table symTab, perform name analysis on this node's 
     * two children
     */
    public void nameAnalysis(SymTable symTab) {
        myId.nameAnalysis(symTab);
        myExpList.nameAnalysis(symTab);
    }  
      
    /**
     * typeCheck
     */
   public Type typeCheck() {
        if (!myId.typeCheck().isFnType()) {  
            ErrMsg.fatal(myId.lineNum(), myId.charNum(), 
                         "Attempt to call a non-function");
            return new ErrorType();
        }
        
        FnInfo fnInfo = (FnInfo)(myId.info());
        
        if (fnInfo == null) {
            System.err.println("null sym for Id in CallExpNode.typeCheck");
            System.exit(-1);
        }
        
        if (myExpList.size() != fnInfo.getNumParams()) {
            ErrMsg.fatal(myId.lineNum(), myId.charNum(), 
                         "Function call with wrong number of args");
            return fnInfo.getReturnType();
        }
        
        myExpList.typeCheck(fnInfo.getParamTypes());
        return fnInfo.getReturnType();
    }

	/**
	 * codeGen
	 */
	public void codeGen() {
		if(myExpList != null)
			myExpList.codeGen();

		myId.genJumpAndLink(); // Codegen.generate("jal", "__" + myId.name());
		Codegen.genPush(Codegen.V0);
	}
        
    // ** unparse **
    public void unparse(PrintWriter p, int indent) {
        myId.unparse(p, 0);
        p.print("(");
        if (myExpList != null) {
            myExpList.unparse(p, 0);
        }
        p.print(")");
    }

    // 2 kids
    private IdNode myId;
    private ExpListNode myExpList;  // possibly null
}

abstract class UnaryExpNode extends ExpNode {
    public UnaryExpNode(ExpNode exp) {
        myExp = exp;
    }
    
    /**
     * Return the line number for this unary expression node. 
     * The line number is the one corresponding to the  operand.
     */
    public int lineNum() {
        return myExp.lineNum();
    }
    
    /**
     * Return the char number for this unary expression node.
     * The char number is the one corresponding to the  operand.
     */
    public int charNum() {
        return myExp.charNum();
    }
    
    /**
     * nameAnalysis
     * Given a symbol table symTab, perform name analysis on this node's child
     */
    public void nameAnalysis(SymTable symTab) {
        myExp.nameAnalysis(symTab);
    }
    
    // one child
    protected ExpNode myExp;
}

abstract class BinaryExpNode extends ExpNode {
    public BinaryExpNode(ExpNode exp1, ExpNode exp2) {
        myExp1 = exp1;
        myExp2 = exp2;
    }
    
    /**
     * Return the line number for this binary expression node. 
     * The line number is the one corresponding to the left operand.
     */
    public int lineNum() {
        return myExp1.lineNum();
    }
    
    /**
     * Return the char number for this binary expression node.
     * The char number is the one corresponding to the left operand.
     */
    public int charNum() {
        return myExp1.charNum();
    }
    
    /**
     * nameAnalysis
     * Given a symbol table symTab, perform name analysis on this node's 
     * two children
     */
    public void nameAnalysis(SymTable symTab) {
        myExp1.nameAnalysis(symTab);
        myExp2.nameAnalysis(symTab);
    }
    
    // two kids
    protected ExpNode myExp1;
    protected ExpNode myExp2;
}

// **********************************************************************
// Subclasses of UnaryExpNode
// **********************************************************************

class UnaryMinusNode extends UnaryExpNode {
    public UnaryMinusNode(ExpNode exp) {
        super(exp);
    }

    /**
     * typeCheck
     */
    public Type typeCheck() {
        Type type = myExp.typeCheck();
        Type retType = new IntType();
        
        if (!type.isErrorType() && !type.isIntType()) {
            ErrMsg.fatal(lineNum(), charNum(),
                         "Arithmetic operator applied to non-numeric operand");
            retType = new ErrorType();
        }
        
        if (type.isErrorType()) {
            retType = new ErrorType();
        }
        
        return retType;
    }

	/**
	 * codeGen
	 */
	public void codeGen() {
		myExp.codeGen();
		// Retrieve off the stack //
		Codegen.genPop(Codegen.T0);
		Codegen.generate("li", Codegen.T1, "0"); //load 0 to flip into negative
		Codegen.generate("sub", Codegen.T1, Codegen.T1, Codegen.T0);
		// place on the stack //
		Codegen.genPush(Codegen.T1);
	}

    public void unparse(PrintWriter p, int indent) {
        p.print("(-");
        myExp.unparse(p, 0);
        p.print(")");
    }
}

class NotNode extends UnaryExpNode {
    public NotNode(ExpNode exp) {
        super(exp);
    }

    /**
     * typeCheck
     */
    public Type typeCheck() {
        Type type = myExp.typeCheck();
        Type retType = new BoolType();
        
        if (!type.isErrorType() && !type.isBoolType()) {
            ErrMsg.fatal(lineNum(), charNum(),
                         "Logical operator applied to non-bool operand");
            retType = new ErrorType();
        }
        
        if (type.isErrorType()) {
            retType = new ErrorType();
        }
        
        return retType;
    }

	/**
	 * codeGen
	 */
	public void codeGen() {
		myExp.codeGen();
		// Get operand //
		Codegen.genPop(Codegen.T0);
		Codegen.generate("li", Codegen.T1, 1);
		// xor with 1 -> NOT // I never knew why mips doesn't have a NOT operator...
		Codegen.generate("xor", Codegen.T0, Codegen.T0, Codegen.T1);

		// push result //
		Codegen.genPush(Codegen.T0);
	}

    public void unparse(PrintWriter p, int indent) {
        p.print("(!");
        myExp.unparse(p, 0);
        p.print(")");
    }
}


abstract class ArithmeticExpNode extends BinaryExpNode {
    public ArithmeticExpNode(ExpNode exp1, ExpNode exp2) {
        super(exp1, exp2);
    }
    
    /**
     * typeCheck
     */
    public Type typeCheck() {
        Type type1 = myExp1.typeCheck();
        Type type2 = myExp2.typeCheck();
        Type retType = new IntType();
        
        if (!type1.isErrorType() && !type1.isIntType()) {
            ErrMsg.fatal(myExp1.lineNum(), myExp1.charNum(),
                         "Arithmetic operator applied to non-numeric operand");
            retType = new ErrorType();
        }
        
        if (!type2.isErrorType() && !type2.isIntType()) {
            ErrMsg.fatal(myExp2.lineNum(), myExp2.charNum(),
                         "Arithmetic operator applied to non-numeric operand");
            retType = new ErrorType();
        }
        
        if (type1.isErrorType() || type2.isErrorType()) {
            retType = new ErrorType();
        }
        
        return retType;
    }
}
// **********************************************************************
// Subclasses of BinaryExpNode
// **********************************************************************

abstract class LogicalExpNode extends BinaryExpNode {
    public LogicalExpNode(ExpNode exp1, ExpNode exp2) {
        super(exp1, exp2);
    }
    
    /**
     * typeCheck
     */
    public Type typeCheck() {
        Type type1 = myExp1.typeCheck();
        Type type2 = myExp2.typeCheck();
        Type retType = new BoolType();
        
        if (!type1.isErrorType() && !type1.isBoolType()) {
            ErrMsg.fatal(myExp1.lineNum(), myExp1.charNum(),
                         "Logical operator applied to non-bool operand");
            retType = new ErrorType();
        }
        
        if (!type2.isErrorType() && !type2.isBoolType()) {
            ErrMsg.fatal(myExp2.lineNum(), myExp2.charNum(),
                         "Logical operator applied to non-bool operand");
            retType = new ErrorType();
        }
        
        if (type1.isErrorType() || type2.isErrorType()) {
            retType = new ErrorType();
        }
        
        return retType;
    }
}

abstract class EqualityExpNode extends BinaryExpNode {
    public EqualityExpNode(ExpNode exp1, ExpNode exp2) {
        super(exp1, exp2);
    }
    
    /**
     * typeCheck
     */
    public Type typeCheck() {
        Type type1 = myExp1.typeCheck();
        Type type2 = myExp2.typeCheck();
        Type retType = new BoolType();
        
        if (type1.isVoidType() && type2.isVoidType()) {
            ErrMsg.fatal(lineNum(), charNum(),
                         "Equality operator applied to void functions");
            retType = new ErrorType();
        }
        
        if (type1.isFnType() && type2.isFnType()) {
            ErrMsg.fatal(lineNum(), charNum(),
                         "Equality operator applied to functions");
            retType = new ErrorType();
        }
        
        if (type1.isStructDefType() && type2.isStructDefType()) {
            ErrMsg.fatal(lineNum(), charNum(),
                         "Equality operator applied to struct names");
            retType = new ErrorType();
        }
        
        if (type1.isStructType() && type2.isStructType()) {
            ErrMsg.fatal(lineNum(), charNum(),
                         "Equality operator applied to struct variables");
            retType = new ErrorType();
        }        
        
        if (!type1.equals(type2) && !type1.isErrorType() && !type2.isErrorType()) {
            ErrMsg.fatal(lineNum(), charNum(),
                         "Type mismatch");
            retType = new ErrorType();
        }
        
        if (type1.isErrorType() || type2.isErrorType()) {
            retType = new ErrorType();
        }
        
        return retType;
    }
}

abstract class RelationalExpNode extends BinaryExpNode {
    public RelationalExpNode(ExpNode exp1, ExpNode exp2) {
        super(exp1, exp2);
    }
    
    /**
     * typeCheck
     */
    public Type typeCheck() {
        Type type1 = myExp1.typeCheck();
        Type type2 = myExp2.typeCheck();
        Type retType = new BoolType();
        
        if (!type1.isErrorType() && !type1.isIntType()) {
            ErrMsg.fatal(myExp1.lineNum(), myExp1.charNum(),
                         "Relational operator applied to non-numeric operand");
            retType = new ErrorType();
        }
        
        if (!type2.isErrorType() && !type2.isIntType()) {
            ErrMsg.fatal(myExp2.lineNum(), myExp2.charNum(),
                         "Relational operator applied to non-numeric operand");
            retType = new ErrorType();
        }
        
        if (type1.isErrorType() || type2.isErrorType()) {
            retType = new ErrorType();
        }
        
        return retType;
    }
}

class PlusNode extends ArithmeticExpNode {
    public PlusNode(ExpNode exp1, ExpNode exp2) {
        super(exp1, exp2);
    }
    
    public void unparse(PrintWriter p, int indent) {
        p.print("(");
        myExp1.unparse(p, 0);
        p.print(" + ");
        myExp2.unparse(p, 0);
        p.print(")");
    }

	/**
	 * codeGen
	 */
	public void codeGen() {
		// Evaluate operands //
		myExp2.codeGen();
		myExp1.codeGen();
		// Pop values //
		Codegen.genPop(Codegen.T0);
		Codegen.genPop(Codegen.T1);

		// Add and push result //
		Codegen.generate("add", Codegen.T0, Codegen.T0, Codegen.T1);
		Codegen.genPush(Codegen.T0);
	}
}

class MinusNode extends ArithmeticExpNode {
    public MinusNode(ExpNode exp1, ExpNode exp2) {
        super(exp1, exp2);
    }
    
    public void unparse(PrintWriter p, int indent) {
        p.print("(");
        myExp1.unparse(p, 0);
        p.print(" - ");
        myExp2.unparse(p, 0);
        p.print(")");
    }

	/**
	 * codeGen
	 */
	public void codeGen() {
		// Evaluate operands //
		myExp2.codeGen();
		myExp1.codeGen();
		// Pop values //
		Codegen.genPop(Codegen.T0);
		Codegen.genPop(Codegen.T1);

		// Subtract and push result //
		Codegen.generate("sub", Codegen.T0, Codegen.T0, Codegen.T1);
		Codegen.genPush(Codegen.T0);
	}
}

class TimesNode extends ArithmeticExpNode {
    public TimesNode(ExpNode exp1, ExpNode exp2) {
        super(exp1, exp2);
    }

    
    public void unparse(PrintWriter p, int indent) {
        p.print("(");
        myExp1.unparse(p, 0);
        p.print(" * ");
        myExp2.unparse(p, 0);
        p.print(")");
    }

	/**
	 * codeGen
	 */
	public void codeGen() {
		// Evaluate operands //
		myExp2.codeGen();
		myExp1.codeGen();
		// Pop values //
		Codegen.genPop(Codegen.T0);
		Codegen.genPop(Codegen.T1);

		// Multiply and push result //
		Codegen.generate("mul", Codegen.T0, Codegen.T0, Codegen.T1);
		Codegen.genPush(Codegen.T0);
	}
}

class DivideNode extends ArithmeticExpNode {
    public DivideNode(ExpNode exp1, ExpNode exp2) {
        super(exp1, exp2);
    }
    
    public void unparse(PrintWriter p, int indent) {
        p.print("(");
        myExp1.unparse(p, 0);
        p.print(" / ");
        myExp2.unparse(p, 0);
        p.print(")");
    }

	/**
	 * codeGen
	 */
	public void codeGen() {
		// Evaluate operands //
		myExp2.codeGen();
		myExp1.codeGen();
		// Pop values //
		Codegen.genPop(Codegen.T0);
		Codegen.genPop(Codegen.T1);

		//Divide and push result //
		Codegen.generate("div", Codegen.T0, Codegen.T0, Codegen.T1);
		Codegen.genPush(Codegen.T0);
	}
}

class AndNode extends LogicalExpNode {
    public AndNode(ExpNode exp1, ExpNode exp2) {
        super(exp1, exp2);
    }
    
    public void unparse(PrintWriter p, int indent) {
        p.print("(");
        myExp1.unparse(p, 0);
        p.print(" && ");
        myExp2.unparse(p, 0);
        p.print(")");
    }

	/**
	 * codeGen
	 */
	public void codeGen() {
		String shortCircuit = Codegen.nextLabel();
		String end = Codegen.nextLabel();
		// Evaluate operands //
		myExp2.codeGen();
		myExp1.codeGen();
		// Pop values //
		Codegen.genPop(Codegen.T0);
		Codegen.genPop(Codegen.T1);
		
		// Load 0 to T2 and see if LHS is false //
		Codegen.generate("li", Codegen.T2, 0);
		Codegen.generate("beq", Codegen.T2, Codegen.T0, shortCircuit);

		// check left operand - short if false //

		// AND and push result //
		Codegen.generate("and", Codegen.T0, Codegen.T0, Codegen.T1);
		Codegen.genPush(Codegen.T0);
		Codegen.generate("b", end);

		// short //
		Codegen.genLabel(shortCircuit);
		Codegen.genPush(Codegen.T0);
		// end //
		Codegen.genLabel(end);
	}
}

class OrNode extends LogicalExpNode {
    public OrNode(ExpNode exp1, ExpNode exp2) {
        super(exp1, exp2);
    }
    
    public void unparse(PrintWriter p, int indent) {
        p.print("(");
        myExp1.unparse(p, 0);
        p.print(" || ");
        myExp2.unparse(p, 0);
        p.print(")");
    }

	/**
	 * codeGen
	 */
	public void codeGen() {
		String shortCircuit = Codegen.nextLabel();
		String end = Codegen.nextLabel();
		// Evaluate operands //
		myExp2.codeGen();
		myExp1.codeGen();
		// Pop values //
		Codegen.genPop(Codegen.T0);
		Codegen.genPop(Codegen.T1);
		
		// Load  to T2 and see if LHS is true //
		Codegen.generate("li", Codegen.T2, 0);
		Codegen.generate("bne", Codegen.T2, Codegen.T0, shortCircuit);

		// check left operand - short if false //

		// AND and push result //
		Codegen.generate("or", Codegen.T0, Codegen.T0, Codegen.T1);
		Codegen.genPush(Codegen.T0);
		Codegen.generate("b", end);

		// short //
		Codegen.genLabel(shortCircuit);
		Codegen.genPush(Codegen.T0);
		// end //
		Codegen.genLabel(end);
	}
}

class EqualsNode extends EqualityExpNode {
    public EqualsNode(ExpNode exp1, ExpNode exp2) {
        super(exp1, exp2);
    }
    
    public void unparse(PrintWriter p, int indent) {
        p.print("(");
        myExp1.unparse(p, 0);
        p.print(" == ");
        myExp2.unparse(p, 0);
        p.print(")");
    }

	/**
	 * codeGen
	 */
	public void codeGen() {
		myExp1.codeGen();
		myExp2.codeGen();
		// Get operands //
		Codegen.genPop(Codegen.T1);
		Codegen.genPop(Codegen.T0);
		// check if - XOR followed by NOT gives all 1's for equal //
		Codegen.generate("xor", Codegen.T0, Codegen.T0, Codegen.T1);
		Codegen.generate("not", Codegen.T0, Codegen.T0);
		Codegen.genPush(Codegen.T0);
	}
}

class NotEqualsNode extends EqualityExpNode {
    public NotEqualsNode(ExpNode exp1, ExpNode exp2) {
        super(exp1, exp2);
    }
    
    public void unparse(PrintWriter p, int indent) {
        p.print("(");
        myExp1.unparse(p, 0);
        p.print(" != ");
        myExp2.unparse(p, 0);
        p.print(")");
    }

	/**
	 * codeGen
	 */
	public void codeGen() {
		myExp1.codeGen();
		myExp2.codeGen();
		// Get operands //
		Codegen.genPop(Codegen.T1);
		Codegen.genPop(Codegen.T0);
		// check if - XOR gives 1's for not equal //
		Codegen.generate("xor", Codegen.T0, Codegen.T0, Codegen.T1);
		Codegen.genPush(Codegen.T0);
	}
}

class LessNode extends RelationalExpNode {
    public LessNode(ExpNode exp1, ExpNode exp2) {
        super(exp1, exp2);
    }
    
    public void unparse(PrintWriter p, int indent) {
        p.print("(");
        myExp1.unparse(p, 0);
        p.print(" < ");
        myExp2.unparse(p, 0);
        p.print(")");
    }

	/**
	 * codeGen
	 */
	public void codeGen() {
		// False label //
		String skip = Codegen.nextLabel();
		myExp2.codeGen();
		myExp1.codeGen();
		// Get operands //
		Codegen.genPop(Codegen.T0);
		Codegen.genPop(Codegen.T1);
		// Load 0 //
		Codegen.generate("li", Codegen.T2, 0);

		// BGE //
		Codegen.generate("bge", Codegen.T0, Codegen.T1, skip);
		Codegen.generate("addi", Codegen.T2, 1); // 0 -> 1
		Codegen.genLabel(skip);

		// push result //
		Codegen.genPush(Codegen.T2);
	}
}

class GreaterNode extends RelationalExpNode {
    public GreaterNode(ExpNode exp1, ExpNode exp2) {
        super(exp1, exp2);
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("(");
        myExp1.unparse(p, 0);
        p.print(" > ");
        myExp2.unparse(p, 0);
        p.print(")");
    }

	/**
	 * codeGen
	 */
	public void codeGen() {
		// False label //
		String skip = Codegen.nextLabel();
		myExp2.codeGen();
		myExp1.codeGen();
		// Get operands //
		Codegen.genPop(Codegen.T0);
		Codegen.genPop(Codegen.T1);
		// Load 0 //
		Codegen.generate("li", Codegen.T2, 0);

		// BLE //
		Codegen.generate("ble", Codegen.T0, Codegen.T1, skip);
		Codegen.generate("addi", Codegen.T2, 1); // 0 -> 1
		Codegen.genLabel(skip);

		// push result //
		Codegen.genPush(Codegen.T2);
	}
}

class LessEqNode extends RelationalExpNode {
    public LessEqNode(ExpNode exp1, ExpNode exp2) {
        super(exp1, exp2);
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("(");
        myExp1.unparse(p, 0);
        p.print(" <= ");
        myExp2.unparse(p, 0);
        p.print(")");
    }

	/**
	 * codeGen
	 */
	public void codeGen() {
		// False label //
		String skip = Codegen.nextLabel();
		myExp2.codeGen();
		myExp1.codeGen();
		// Get operands //
		Codegen.genPop(Codegen.T0);
		Codegen.genPop(Codegen.T1);
		// Load 0 //
		Codegen.generate("li", Codegen.T2, 0);

		// BGE //
		Codegen.generate("bgt", Codegen.T0, Codegen.T1, skip);
		Codegen.generate("addi", Codegen.T2, 1); // 0 -> 1
		Codegen.genLabel(skip);

		// push result //
		Codegen.genPush(Codegen.T2);
	}
}

class GreaterEqNode extends RelationalExpNode {
    public GreaterEqNode(ExpNode exp1, ExpNode exp2) {
        super(exp1, exp2);
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("(");
        myExp1.unparse(p, 0);
        p.print(" >= ");
        myExp2.unparse(p, 0);
        p.print(")");
    }

	/**
	 * codeGen
	 */
	public void codeGen() {
		// False label //
		String skip = Codegen.nextLabel();
		myExp2.codeGen();
		myExp1.codeGen();
		// Get operands //
		Codegen.genPop(Codegen.T0);
		Codegen.genPop(Codegen.T1);
		// Load 0 //
		Codegen.generate("li", Codegen.T2, 0);

		// BGE //
		Codegen.generate("blt", Codegen.T0, Codegen.T1, skip);
		Codegen.generate("addi", Codegen.T2, 1); // 0 -> 1
		Codegen.genLabel(skip);

		// push result //
		Codegen.genPush(Codegen.T2);
	}
}
