@ECHO off
CLS
SETLOCAL
IF NOT EXIST temp\nul MD temp > nul
IF NOT EXIST temp\classes\nul MD temp\classes > nul
IF NOT EXIST temp\test\nul MD temp\test > nul


IF EXIST src\components\nul RD src\components /S /Q > nul
javac -encoding UTF8 -classpath ../xdef.jar;lib/derby.jar;lib/saxon9-xqj.jar;lib/saxon9he.jar;lib/snakeyaml-1.9.jar -d temp\classes src/RunAll_prepare.java src/task6/GenComponents*.java
java -classpath temp/classes;../xdef.jar;lib/derby.jar;lib/saxon9-xqj.jar;lib/saxon9he.jar;lib/snakeyaml-1.9.jar RunAll_prepare > nul
javac -encoding UTF8 -classpath temp/classes;../xdef.jar;lib/derby.jar;lib/saxon9-xqj.jar;lib/saxon9he.jar;lib/snakeyaml-1.9.jar -d temp\classes src/GenDerby.java src/*.java src/data/*.java src/task1/*.java src/task2/*.java src/task3/*.java src/task4/*.java src/task5/*.java src/task6/*.java src/task6/components1/*.java src/task6/components2/*.java src/task7/*.java src/task8/*.java src/task9/*.java src/task10/*.java
java -classpath temp/classes;../xdef.jar;lib/derby.jar;lib/saxon9-xqj.jar;lib/saxon9he.jar;lib/snakeyaml-1.9.jar RunAll

IF EXIST derby.log DEL derby.log > nul
pause
ENDLOCAL
