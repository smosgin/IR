compile:
	mkdir bin
	javac -d bin -cp bin src/tokenizer/*.java
run:
	java -cp bin tokenizer.Tokenizer input output
all: 
	compile, run
clean: 
	rm -r bin