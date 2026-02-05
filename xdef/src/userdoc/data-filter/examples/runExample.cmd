@ECHO off
CLS
SETLOCAL
IF %1. == . GOTO paramMissing

ECHO ==== %1 ====
IF NOT EXIST temp\nul MD temp > nul
IF NOT EXIST temp\classes\nul MD temp\classes > nul
IF NOT EXIST temp\test\nul MD temp\test > nul

set cp=-classpath temp/classes;../xdef-${version}.jar;lib/derby-${derby.version}.jar;lib/SaxonHE-${saxon-he.version}-xqj.jar;lib/SaxonHE-${saxon-he.version}.jar;lib/snakeyaml-${snakeyaml.version}.jar
set copts=%cp% -encoding UTF8 -d temp/classes

IF %1 == task6/Town1 GOTO component1
IF %1 == task6/Town2 GOTO component2

IF EXIST src\components\nul RD src\components /S /Q > nul
javac %copts% src/GenDerby.java src/%1*.java
java %cp% %1
GOTO end

:component1
IF EXIST src\task6\components\nul RD src\task6\components1 /S /Q > nul
javac %copts% src/task6/GenComponents1.java
java %cp% task6.GenComponents1
javac %copts% src/task6/components1/*.java
javac %copts% src/task6/Town1.java
java %cp% task6.Town1
GOTO end

:component2
IF EXIST src\task6\components\nul RD src\task6\components2 /S /Q > nul
javac %copts% src/task6/GenComponents2.java
java %cp% task6.GenComponents2

javac %copts% src/task6/components2/*.java
javac %copts% src/task6/Town2.java
java %cp% task6.Town2
GOTO end

:paramMissing
ECHO parameter with the name of example is missing

:end
IF EXIST derby.log DEL derby.log > nul
pause
ENDLOCAL
