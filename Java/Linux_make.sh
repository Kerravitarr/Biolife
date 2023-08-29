#!/bin/bash

function error {
	echo -e "!"
	echo -e "!"
	echo -e "!!!!!!WARNING!!!!!!"
	echo -e "!!!!!!FATAL ERROR!!!!!!"
	echo -e "!!!!!!If you can't understand it out, write to the author!!!!!!"
	read -n1 -r -p "Press any key to continue..."
	exit 1
}
function clean {
	rm -r ./jarFile/
	rm -r ./docsFiles/
	rm -r ./binFiles/
	rm -r ./testBinFiles/
}
function build {
	echo -e "======================================="
	echo -e "STEP 1. Compiling files to bytecode."
	echo -e "======================================="
	javac --release 16 -encoding UTF-8 -classpath  src/ -d binFiles src/start/BioLife.java
	if [[  $? != 0 ]]; then
		error
	fi
	mkdir -p "./binFiles/locales"
	cp -r "./src/locales" "./binFiles/"
	mkdir -p "./binFiles/resources"
	cp -r "./src/resources" "./binFiles/"
	clear

	echo -e "======================================="
	echo -e "STEP 1 - OK"
	echo -e "bytecode files in binFiles/"
	echo -e "======================================="
	echo -e "======================================="
	echo -e "STEP 2. Creation of program documentation."
	echo -e "======================================="
	javadoc --release 16 -encoding utf8 -d docsFiles -sourcepath src/main/Configurations.java src/*/*.java src/*/*/*.java
	if [[  $? != 0 ]]; then
		error
	fi
	clear

	echo -e "======================================="
	echo -e "STEP 1 - OK"
	echo -e "bytecode files in binFiles/"
	echo -e "======================================="
	echo -e "======================================="
	echo -e "STEP 2 - OK"
	echo -e "API documents in docsFiles/"
	echo -e "======================================="
	echo -e "======================================="
	echo -e "STEP 3. Collecting bytecode into an executable"
	echo -e "======================================="
	jar -cvef start/BioLife BioLife.jar -C binFiles .
	if [[  $? != 0 ]]; then
		error
	fi
	clear

	echo -e "======================================="
	echo -e "STEP 1 - OK"
	echo -e "bytecode files in binFiles/"
	echo -e "======================================="
	echo -e "======================================="
	echo -e "STEP 2 - OK"
	echo -e "API documents in docsFiles/"
	echo -e "======================================="
	echo -e "======================================="
	echo -e "STEP 3 - OK"
	echo -e "BioLife.jar in jarFile/"
	echo -e "======================================="

	chmod +x BioLife.jar
	mkdir -p ./jarFile/
	mv BioLife.jar ./jarFile/
}

function tests {
	echo -e "======================================="
	echo -e "STEP 1. Compiling test files to bytecode."
	echo -e "======================================="
	rm -r ./testBinFiles/
	javac --release 16 -encoding UTF-8 -classpath  tests/:src/:tests/testLib/junit-4.13.2.jar:tests/testLib/hamcrest-core-1.3.jar -d testBinFiles tests/start/TestList.java
	if [[  $? != 0 ]]; then
		error
	fi
	echo -e "======================================="
	echo -e "STEP 2. Run tests."
	echo -e "======================================="
	cp -r "./src/locales" "./testBinFiles/"
	java -cp testBinFiles/:tests/testLib/junit-4.13.2.jar:tests/testLib/hamcrest-core-1.3.jar org.junit.runner.JUnitCore start.TestList
	if [[  $? != 0 ]]; then
		error
	fi
}

PS3='Please enter your choice: '
options=("Build" "Clean" "Rebuild" "Test")
select opt in "${options[@]}"
do
    case $opt in
        "Build")
			build
			exit 0
            ;;
        "Clean")
			clean
			exit 0
            ;;
        "Rebuild")
			clean
			build
			exit 0
            ;;
        "Test")
			tests
			exit 0
            ;;
        *) echo "invalid option $REPLY";;
    esac
done


