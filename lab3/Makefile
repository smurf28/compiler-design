###
# This Makefile can be used to make a parser for the C-- language
# (CmmParser.class) and to make a program (Main.class) that tests the parser and
# the unparse methods in ast.java.
#
# make clean removes all generated files.
#
###


# define the java compiler to be used and the flags
JC = javac
FLAGS = -g -cp $(CP)
CP = ../deps:.

# rules
Main.class: Main.java CmmParser.class Yylex.class ASTnode.class SyntaxErrorException.class
	$(JC) $(FLAGS) Main.java

CmmParser.class: CmmParser.java ASTnode.class Yylex.class ErrMsg.class SyntaxErrorException.class
	$(JC) $(FLAGS) CmmParser.java

CmmParser.java: cmm.cup
	java -cp $(CP) java_cup.Main -parser CmmParser < cmm.cup

Yylex.class: cmm.jlex.java sym.class ErrMsg.class SyntaxErrorException.class
	$(JC) $(FLAGS) cmm.jlex.java

ASTnode.class: ast.java
	$(JC) $(FLAGS) ast.java

cmm.jlex.java: cmm.jlex sym.class
	java -cp $(CP) JLex.Main cmm.jlex

sym.class: sym.java
	$(JC) $(FLAGS) sym.java

sym.java: cmm.cup
	java -cp $(CP) java_cup.Main -parser CmmParser < cmm.cup

ErrMsg.class: ErrMsg.java
	$(JC) $(FLAGS) ErrMsg.java

SyntaxErrorException.class: SyntaxErrorException.java
	$(JC) $(FLAGS)  SyntaxErrorException.java

# test
test:
	java -cp $(CP) Main test.cmm test.out

# clean
clean:
	rm -f *~ *.class CmmParser.java cmm.jlex.java sym.java
