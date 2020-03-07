@ECHO off
CLS
SETLOCAL
IF %1. == . GOTO paramMissing

ECHO ==== %1 ====
IF NOT EXIST temp\nul MD temp > nul
IF NOT EXIST temp\classes\nul MD temp\classes > nul
IF NOT EXIST temp\test\nul MD temp\test > nul

IF %1 == Example_XC1 GOTO component
IF %1 == Example_XC2 GOTO component

IF EXIST src\components\nul RD src\components /S /Q > nul
javac -encoding UTF8 -classpath ../xdef-32.5.6 (2020-02-14) (2019-12-17) (2019-12-17).jar;derby/derby.jar -d temp\classes src/GenDerby.java src/%1.java
java -classpath temp/classes;../xdef-32.5.6 (2020-02-14) (2019-12-17) (2019-12-17).jar;derby/derby.jar %1
GOTO end

:component
javac -encoding UTF8 -classpath temp/classes;../xdef-32.5.6 (2020-02-14) (2019-12-17) (2019-12-17).jar -d temp\classes src/GenComponents.java src\data\MyClass.java
java -classpath temp/classes;../xdef-32.5.6 (2020-02-14) (2019-12-17) (2019-12-17).jar GenComponents
ECHO  X-components are generated to temp/components

javac -encoding UTF8 -classpath temp/classes;../xdef-32.5.6 (2020-02-14) (2019-12-17) (2019-12-17).jar -d temp\classes temp/components/*.java src/%1.java
java -classpath temp/classes;../xdef-32.5.6 (2020-02-14) (2019-12-17) (2019-12-17).jar %1
GOTO end

:paramMissing
ECHO parameter with the name of example is missing

:end
IF EXIST derby.log DEL derby.log > nul
pause
ENDLOCAL
