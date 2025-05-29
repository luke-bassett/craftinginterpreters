pkg_path := com/craftinginterpreters/lox
sources := $(wildcard $(pkg_path)/*.java)
class_path := bin/$(pkg_path)
classes := $(sources:$(pkg_path)/%.java=$(class_path)/%.class)

all: $(classes)

# Compile all Java files
$(class_path)/%.class: $(pkg_path)/%.java
	javac -d bin -Xlint:unchecked $^

# accepts file to be processed or no file for REPL
# (filter-out removes 'run' from MAKECMDGOALS, passing remaining args to java)
run: $(classes)
	@ java -cp bin com.craftinginterpreters.lox.Lox $(filter-out $@,$(MAKECMDGOALS))

clean:
	@ rm -rf bin

.PHONY: all run clean