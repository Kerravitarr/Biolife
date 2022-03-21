all: clean class jar

class:
	@echo "$@"
	javac -verbose -encoding utf8 -d bin -classpath libs\json-simple-1.1.1.jar src\start\BioLife.java

jar: src/*.class
	@echo "$@"
	@jar -cvmf META-INF/* BioLife.jar $$(find .src/ -type f -name "*.class" | xargs echo) 
	mkdir -p ./build/
	mv BioLife.jar ./build/

clean:
	@echo "$@"
	find . -type f -name *.class -exec rm -rf {} \;
	rm -rf ./build