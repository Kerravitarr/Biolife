all: clean class jar

class:
	@echo "$@"
	javac -encoding utf8 -d bin -classpath libs\json-simple-1.1.1.jar src\start\*.java src\main\*.java src\start\*.java src\MapObjects\*.java src\panels\*.java src\Utils\*.java
	
	
	
	javac -encoding utf8 -sourcepath src -d bin src\start\BioLife.java

jar: src/*.class
	@echo "$@"
	@jar -cvmf META-INF/* BioLife.jar $$(find .src/ -type f -name "*.class" | xargs echo) 
	
	jar cvf BioLife.jar -C bin .
	
	jar -cef start\BioLife BioLife.jar -C bin .
	
	mkdir -p ./build/
	mv BioLife.jar ./build/

clean:
	@echo "$@"
	find . -type f -name *.class -exec rm -rf {} \;
	rm -rf ./build