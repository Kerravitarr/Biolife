@echo off
choice /C IC /D I /T 5 /M "I - build and install; C - clean"

if %errorlevel%==1 Goto make
if %errorlevel%==2 Goto clean

:make
javac -encoding utf8 -d bin src\start\*.java src\main\*.java src\start\*.java src\MapObjects\*.java src\panels\*.java src\Utils\*.java

javadoc -encoding utf8  -d docs -sourcepath src\start\*.java src\main\*.java src\start\*.java src\MapObjects\*.java src\panels\*.java src\Utils\*.java

jar -cef start.BioLife BioLife.jar -C bin .

mkdir .\build\
move BioLife.jar .\build\

goto end

:clean
rd /s /q .\build\
rd /s /q .\docs\
rd /s /q .\bin\

:end: