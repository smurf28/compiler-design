###
# A Makefile for Lab #4
###

# define the java compiler to be used and the flags
JC = javac
FLAGS = -g -cp $(CP)
CP = .

# define the rules
Main.class: Main.java SymInfo.class SymTable.class
	$(JC) $(FLAGS) Main.java

SymTable.class: SymTable.java SymInfo.class DuplicateSymException.class EmptySymTableException.class
	$(JC) $(FLAGS) SymTable.java

SymInfo.class: SymInfo.java Type.class IdNode.class
	$(JC) $(FLAGS) SymInfo.java

DuplicateSymException.class: DuplicateSymException.java
	$(JC) $(FLAGS) DuplicateSymException.java

EmptySymTableException.class: EmptySymTableException.java
	$(JC) $(FLAGS) EmptySymTableException.java

Type.class: Type.java IdNode.class
	$(JC) $(FLAGS) Type.java

IdNode.class: ast.java
	$(JC) $(FLAGS) ast.java

# testing 
test:
	java -cp $(CP) Main

# clean up
clean:
	rm -f *~ *.class
