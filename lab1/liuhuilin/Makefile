###
# A simple Makefile for Lab #1
###

# define the java compiler to be used and the flags
JC = javac
FLAGS = -g -cp $(CP)
CP = ../lab1

# define the rules


Main.class:Main.java WordScanner.class WordCount.class 
	$(JC) $(FLAGS) Main.java

WordScanner.class:WordScanner.java Word.class
	$(JC) $(FLAGS) WordScanner.java

WordCount.class:WordCount.java Info.class
	$(JC) $(FLAGS) WordCount.java


Info.class:Info.java
	$(JC) $(FLAGS) Info.java

Word.class:Word.java Info.class
	$(JC) $(FLAGS) Word.java

# clean up
clean:
	-rm *.class
