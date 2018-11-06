@ECHO off
CLS
SETLOCAL
java -classpath "../lib/syntea_xdef3.1.jar;" cz.syntea.xdef.utils.GenDTD %1 %2 %3 %4 %5 %6 %7 %8 %9
ENDLOCAL
