@ECHO off
CLS
SETLOCAL
%JAVA_HOME%\bin\java -classpath "../lib/syntea_xdef3.1.jar" cz.syntea.xdef.utils.XdefToXsd %1 %2 %3 %4 %5 %6 %7 %8 %9
java -classpath "../lib/syntea_xdef3.1.jar" cz.syntea.xdef.utils.XdefToXsd %1 %2 %3 %4 %5 %6 %7 %8 %9
ENDLOCAL
