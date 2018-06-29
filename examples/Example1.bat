@ECHO off
CLS
SETLOCAL
IF NOT EXIST build\nul MD build > nul
IF NOT EXIST build\classes\nul MD build\classes > nul
ECHO .
javac -classpath "../lib/syntea_xdef3.1.jar" -d build\classes Example1.java
CLS
java -classpath "build\classes;../lib/syntea_xdef3.1.jar" Example1
PAUSE
IF EXIST build\nul RD /Q /S build
IF EXIST temp\nul RD /Q /S temp
ENDLOCAL
