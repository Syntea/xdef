@ECHO off
CLS
SETLOCAL
IF NOT EXIST temp\nul MD temp > nul
IF NOT EXIST temp\classes\nul MD temp\classes > nul
IF NOT EXIST temp\test\nul MD temp\test > nul

set cp=-classpath temp/classes;../xdef-42.2.40-SNAPSHOT.jar;lib/derby-10.12.1.1.jar;lib/Saxon-HE-9.7.0-18-xqj.jar;lib/Saxon-HE-9.7.0-18.jar;lib/snakeyaml-1.29.jar
set copts=%cp% -encoding UTF8 -d temp/classes

IF EXIST src\components\nul RD src\components /S /Q > nul
javac %copts% src/RunAll_prepare.java src/task6/GenComponents*.java
java %cp% RunAll_prepare > nul
javac %copts% src/GenDerby.java src/*.java src/data/*.java src/task1/*.java src/task2/*.java src/task3/*.java src/task4/*.java src/task5/*.java src/task6/*.java src/task6/components1/*.java src/task6/components2/*.java src/task7/*.java src/task8/*.java src/task9/*.java src/task10/*.java
java %cp% RunAll

IF EXIST derby.log DEL derby.log > nul
pause
ENDLOCAL
