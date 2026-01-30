@ECHO off
CLS
SETLOCAL
IF NOT EXIST temp\nul MD temp > nul
IF NOT EXIST temp\classes\nul MD temp\classes > nul
IF NOT EXIST temp\test\nul MD temp\test > nul

set cp="-classpath temp/classes;../xdef-${version}.jar;lib/derby-${derby.version}.jar;lib/SaxonHE-${saxon-he.version}-xqj.jar;lib/SaxonHE-${saxon-he.version}.jar;lib/snakeyaml-${snakeyaml.version}.jar"
set copts="%cp% -encoding UTF8 -d temp/classes"

IF EXIST src\components\nul RD src\components /S /Q > nul
javac %copts% src/RunAll_prepare.java src/task6/GenComponents*.java
java %cp% RunAll_prepare > nul
javac %copts% src/GenDerby.java src/*.java src/data/*.java src/task1/*.java src/task2/*.java src/task3/*.java src/task4/*.java src/task5/*.java src/task6/*.java src/task6/components1/*.java src/task6/components2/*.java src/task7/*.java src/task8/*.java src/task9/*.java src/task10/*.java
java %cp% RunAll

IF EXIST derby.log DEL derby.log > nul
pause
ENDLOCAL
