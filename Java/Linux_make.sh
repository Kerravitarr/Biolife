#!/bin/bash

function error {
	echo -e "!"
	echo -e "!"
	echo -e "!!!!!!WARNING!!!!!!"
	echo -e "!!!!!!FATAL ERROR!!!!!!"
	echo -e "!!!!!!If you can't figure it out, write to the author!!!!!!"
	read -n1 -r -p "Press any key to continue..."
	exit 1
}
function clean {
	rm -r ./jarFile/
	rm -r ./docsFiles/
	rm -r ./binFiles/
}

PS3='Please enter your choice: '
options=("Build" "Clean" "Rebuild")
select opt in "${options[@]}"
do
    case $opt in
        "Build")
            break
            ;;
        "Clean")
			clean
			exit 0
            ;;
        "Rebuild")
			clean
            break
            ;;
        *) echo "invalid option $REPLY";;
    esac
done

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
javadoc --release 16 -encoding utf8 -d docsFiles -sourcepath src/start/*.java src/main/*.java src/start/*.java src/MapObjects/*.java src/panels/*.java src/Utils/*.java src/MapObjects/dna/*.java 
#if [[  $? != 0 ]]; then
#	error #Ой, да кому нужны документы? Что-то да соберётся
#fi

echo -e "======================================="
echo -e "STEP 1 - OK"
echo -e "bytecode files in binFiles/"
echo -e "======================================="
echo -e "======================================="
echo -e "STEP 2 - OK"
echo -e "API documents in binFiles/"
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
echo -e "API documents in binFiles/"
echo -e "======================================="
echo -e "======================================="
echo -e "STEP 3 - OK"
echo -e "BioLife.jar in binFiles/"
echo -e "======================================="

chmod +x BioLife.jar
mkdir -p ./jarFile/
mv BioLife.jar ./jarFile/

exit 0
