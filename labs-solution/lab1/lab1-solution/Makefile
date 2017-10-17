###
# A simple Makefile for Lab #1
###

# define the java compiler to be used and the flags
JC = javac
FLAGS = -g -cp $(CP)
CP = .

# define the rules
Main.class: Main.java Word.class WordCount.class Info.class
	$(JC) $(FLAGS) Main.java

WordScanner: WordScanner.java Word.class Info.class
	$(JC) $(FLAGS) WordScanner.java

WordCount.class: WordCount.java Info.class
	$(JC) $(FLAGS) WordCount.java

Info.class: Info.java
	$(JC) $(FLAGS) Info.java

Word.class: Word.java Info.class
	$(JC) $(FLAGS) Word.java

# testing 
test:
	java -cp $(CP) Main

# clean up
clean:
	rm -f *~ *.class
