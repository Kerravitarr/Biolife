
@choice /C IC /D I /T 5 /M "I - build and install; C - clean"

@if %errorlevel%==1 Goto make
@if %errorlevel%==2 Goto clean

:make

rd /s /q .\jarFile\
rd /s /q .\docsFiles\
rd /s /q .\binFiles\

@echo =======================================
@echo STEP 1. Compiling files to bytecode.
@echo =======================================
javac --release 16 -encoding utf8 -classpath  src\ -d binFiles src\start\BioLife.java
@if errorlevel 1 Goto error
mkdir .\binFiles\locales
xcopy .\src\locales .\binFiles\locales /e
@CLS
@echo =======================================
@echo Step 1 - OK
@echo =======================================
@echo =======================================
@echo STEP 2. Creation of program documentation
@echo =======================================
javadoc -encoding utf8  -d docsFiles -sourcepath src\start\*.java src\main\*.java src\start\*.java src\MapObjects\*.java src\panels\*.java src\Utils\*.java src\MapObjects\dna\*.java
rem if errorlevel 1 Goto error - Ой, да кому нужны документы? Что-то да соберётся
@CLS
@echo =======================================
@echo Step 2 - OK
@echo =======================================
@echo =======================================
@echo STEP 3. Collecting bytecode into an executable
@echo =======================================
jar -cvef start.BioLife BioLife.jar -C binFiles .
@if errorlevel 1 Goto error
@CLS
@echo =======================================
@echo Step 1 - OK
@echo =======================================
@echo =======================================
@echo Step 2 - OK
@echo =======================================
@echo =======================================
@echo Step 3 - OK
@echo =======================================

mkdir .\jarFile\
move BioLife.jar .\jarFile\
goto end

:clean
rd /s /q .\jarFile\
rd /s /q .\docsFiles\
rd /s /q .\binFiles\
goto end

:error
@echo !
@echo !
@echo !!!!!!WARNING!!!!!!
@echo !!!!!!FATAL ERROR!!!!!!
@echo !!!!!!If you can't figure it out, write to the author!!!!!!
@pause

:end: