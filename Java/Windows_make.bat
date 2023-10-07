
@choice /C ICRT /D I /T 5 /M "I - build; C - clean; R - rebuild; T - Test"

@if %errorlevel%==1 Goto m_build
@if %errorlevel%==2 Goto m_clean
@if %errorlevel%==3 Goto m_rebuild
@if %errorlevel%==4 Goto m_test

:m_build
    call :test_existence
    call :build
exit 0
:m_clean
    call :clean
exit 0
:m_rebuild
    call :test_existence
    call :clean
    call :build
exit 0
:m_test
    call :test_existence
    call :test
exit 0

:build


    rd /s /q .\jarFile\
    rd /s /q .\docsFiles\
    rd /s /q .\binFiles\

    @echo =======================================
    @echo STEP 1. Compiling files to bytecode.
    @echo =======================================
    javac --release 16 -encoding utf8 -classpath  src\ -d binFiles src\start\BioLife.java
    @if errorlevel 1 call :error
    mkdir .\binFiles\locales
    xcopy .\src\locales .\binFiles\locales /e
    mkdir .\binFiles\resources
    xcopy .\src\resources .\binFiles\resources /e
    @CLS
    @echo =======================================
    @echo Step 1 - OK
    @echo bytecode files in binFiles/
    @echo =======================================
    javadoc >nul 2>&1
    @if errorlevel 9009 goto :not_has_javadoc
        @echo =======================================
        @echo STEP 2. Creation of program documentation
        @echo =======================================
        dir /s /b src\*.java > file.lst
        javadoc -encoding utf8 -d docsFiles @file.lst
        if errorlevel 1 goto build_else 
                del "file.lst"
                goto build_eif 
        :build_else
                del "file.lst"
                call :error
        :build_eif
    :not_has_javadoc
    @CLS
    @echo =======================================
    @echo Step 1 - OK
    @echo bytecode files in binFiles/
    @echo =======================================
    @echo =======================================
    @echo Step 2 - OK
    @echo API documents in docsFiles/
    @echo =======================================
    @echo =======================================
    @echo STEP 3. Collecting bytecode into an executable
    @echo =======================================
    jar -cvef start.BioLife BioLife.jar -C binFiles .
    @if errorlevel 1 call :error
    @CLS
    @echo =======================================
    @echo Step 1 - OK
    @echo bytecode files in binFiles/
    @echo =======================================
    @echo =======================================
    @echo Step 2 - OK
    @echo API documents in docsFiles/
    @echo =======================================
    @echo =======================================
    @echo Step 3 - OK
    @echo BioLife.jar in jarFile/
    @echo =======================================

    mkdir .\jarFile\
    move BioLife.jar .\jarFile\
	
exit /b

:test
    rd /s /q .\testBinFiles\
    javac --release 16 -encoding UTF-8 -classpath tests\;src\;tests\testLib\junit-4.13.2.jar;tests\testLib\hamcrest-core-1.3.jar -d testBinFiles tests\start\TestList.java
    @if errorlevel 1 call :error
    mkdir .\testBinFiles\locales
    xcopy .\src\locales .\testBinFiles\locales /e
    java -cp testBinFiles\;tests\testLib\junit-4.13.2.jar;tests\testLib\hamcrest-core-1.3.jar org.junit.runner.JUnitCore start.TestList
    @if errorlevel 1 call :error
    @pause
exit /b


:clean
    rd /s /q .\jarFile\
    rd /s /q .\docsFiles\
    rd /s /q .\binFiles\
    rd /s /q .\testBinFiles\
exit /b

:test_existence
    javac >nul 2>&1
    @if not errorlevel 9009 goto :has_javac
        javac
	@echo !
        @echo !!!!!! You need to install JDK !!!!!!
        @echo !!!!!! And set the path to the JDK in the 'Path' system variable !!!!!!
        call :error
    :has_javac
    jar >nul 2>&1
    @if not errorlevel 9009 goto :has_jar
        jar
	@echo !
        @echo !!!!!! You need to install JDK !!!!!!
        @echo !!!!!! And set the path to the JDK in the 'Path' system variable !!!!!!
        call :error
    :has_jar
exit /b


:error
	@echo !
	@echo !
	@echo !!!!!!WARNING!!!!!!
	@echo !!!!!!FATAL ERROR!!!!!!
	@echo !!!!!!If you can't figure it out, write to the author!!!!!!
	@pause
exit 1

