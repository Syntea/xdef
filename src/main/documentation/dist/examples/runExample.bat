@ECHO off
CLS
SETLOCAL
IF %1. == . GOTO paramMissing

ECHO ==== %1 ====
IF NOT EXIST test\nul MD test > nul
IF NOT EXIST test\classes\nul MD test\classes > nul
IF NOT EXIST test\test\nul MD test\test > nul

IF %1 == Example_XC1 GOTO component
IF %1 == Example_XC2 GOTO component

IF EXIST src\components\nul RD src\components /S /Q > nul
javac -encoding UTF8 -classpath ../xdef-core-3.2.1.2.jar;derby/derby.jar -d test\classes src/GenDerby.java src/%1.java
java -classpath test/classes;../xdef-core-3.2.1.2.jar;derby/derby.jar %1
GOTO end

:component
javac -encoding UTF8 -classpath test/classes;../xdef-core-3.2.1.2.jar -d test\classes src/GenComponents.java src\data\MyClass.java
java -classpath test/classes;../xdef-core-3.2.1.2.jar GenComponents
ECHO  X-components are generated in src/components

javac -encoding UTF8 -classpath test/classes;../xdef-core-3.2.1.2.jar -d test\classes src/components/*.java src/%1.java
java -classpath test/classes;../xdef-core-3.2.1.2.jar %1
GOTO end

:paramMissing
ECHO parameter with the name of example is missing

:end
pause
ENDLOCAL
