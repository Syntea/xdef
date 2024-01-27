@ECHO off
CLS
SETLOCAL
IF %1. == . GOTO paramMissing

ECHO ==== %1 ====
IF NOT EXIST temp\nul MD temp > nul
IF NOT EXIST temp\classes\nul MD temp\classes > nul
IF NOT EXIST temp\test\nul MD temp\test > nul

IF %1 == task6/Town1 GOTO component1
IF %1 == task6/Town2 GOTO component2

IF EXIST src\components\nul RD src\components /S /Q > nul
javac -encoding UTF8 -classpath ../xdef.jar;lib/derby.jar;lib/saxon9-xqj.jar;lib/saxon9he.jar;lib/snakeyaml-1.9.jar -d temp\classes src/GenDerby.java src/%1*.java
java -classpath temp/classes;../xdef.jar;lib/derby.jar;lib/saxon9-xqj.jar;lib/saxon9he.jar;lib/snakeyaml-1.9.jar %1
GOTO end

:component1
IF EXIST src\task6\components\nul RD src\task6\components1 /S /Q > nul
javac -encoding UTF8 -classpath temp/classes;../xdef.jar -d temp\classes src/task6/GenComponents1.java
java -classpath temp/classes;../xdef.jar task6.GenComponents1
javac -encoding UTF8 -classpath temp/classes;../xdef.jar -d temp\classes src/task6/components1/*.java
javac -encoding UTF8 -classpath temp/classes;../xdef.jar -d temp\classes src/task6/Town1.java
java -classpath temp/classes;../xdef.jar task6.Town1
GOTO end

:component2
IF EXIST src\task6\components\nul RD src\task6\components2 /S /Q > nul
javac -encoding UTF8 -classpath temp/classes;../xdef.jar -d temp\classes src/task6/GenComponents2.java
java -classpath temp/classes;../xdef.jar task6.GenComponents2

javac -encoding UTF8 -classpath temp/classes;../xdef.jar -d temp\classes src/task6/components2/*.java
javac -encoding UTF8 -classpath temp/classes;../xdef.jar -d temp\classes src/task6/Town2.java
java -classpath temp/classes;../xdef.jar task6.Town2
GOTO end

:paramMissing
ECHO parameter with the name of example is missing

:end
IF EXIST derby.log DEL derby.log > nul
pause
ENDLOCAL
