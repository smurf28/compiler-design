###
# This Makefile can be used to make a scanner for the C-- language
# (Yylex.class) and to make a program that tests the scanner (Main.class).
#
# The default makes both the scanner and the test program.
#
# make clean removes all generated files.
#
# Note: Main.java will not compile unless Yylex.class exists.
#
###

# define the java compiler to be used and the flags
JC = javac
FLAGS = -g -cp $(CP)
CP = ../deps:.

Main.class: Main.java Yylex.class sym.class
	$(JC) $(FLAGS) Main.java

Yylex.class: cmm.jlex.java ErrMsg.class sym.class
	$(JC) $(FLAGS) cmm.jlex.java

cmm.jlex.java: cmm.jlex sym.class
	java -cp $(CP) JLex.Main cmm.jlex

sym.class: sym.java
	$(JC) $(FLAGS) sym.java

ErrMsg.class: ErrMsg.java
	$(JC) $(FLAGS) ErrMsg.java

###
# testing - add more here to run your tester and compare its results
# to expected results
###
test:
	java -cp $(CP) Main 

###
# clean up
###

clean:
	rm -f *~ *.class cmm.jlex.java
