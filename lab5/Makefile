###
# This Makefile can be used to make a parser for the C-- language
# (CmmParser.class) and to make a program (Main.class) that tests the parser and
# the unparse methods in ast.java.
#
# make clean removes all generated files.
#
###

JC = javac
FLAGS = -g -cp $(CP)
CP = ../deps:.

Main.class: Main.java CmmParser.class Yylex.class ASTnode.class SyntaxErrorException.class
	$(JC) $(FLAGS) Main.java

CmmParser.class: CmmParser.java ASTnode.class Yylex.class ErrMsg.class
	$(JC) $(FLAGS) CmmParser.java

CmmParser.java: cmm.cup
	java -cp $(CP) java_cup.Main -parser CmmParser < cmm.cup

Yylex.class: cmm.jlex.java sym.class ErrMsg.class
	$(JC) $(FLAGS) cmm.jlex.java

ASTnode.class: ast.java Type.java
	$(JC) $(FLAGS) ast.java

cmm.jlex.java: cmm.jlex sym.class
	java -cp $(CP) JLex.Main cmm.jlex

sym.class: sym.java
	$(JC) $(FLAGS) sym.java

sym.java: cmm.cup
	java -cp $(CP) java_cup.Main < cmm.cup

ErrMsg.class: ErrMsg.java
	$(JC) ErrMsg.java

SymInfo.class: SymInfo.java Type.java ast.java
	$(JC) -g SymInfo.java

SymTable.class: SymTable.java SymInfo.java DuplicateSymException.java EmptySymTableException.java
	$(JC) $(FLAGS) SymTable.java

Type.class: Type.java
	$(JC) -g Type.java

SyntaxErrorException.class: SyntaxErrorException.java
	$(JC) -g SyntaxErrorException.java

DuplicateSymException.class: DuplicateSymException.java
	$(JC) -g DuplicateSymException.java

EmptySymTableException.class: EmptySymTableException.java
	$(JC) -g EmptySymTableException.java

###
# test
#
test:
	java -cp $(CP) Main

###
# clean
###
clean:
	rm -f *~ *.class CmmParser.java cmm.jlex.java sym.java
