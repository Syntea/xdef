@ECHO off
CALL cleanSrc.bat
IF NOT %1. == . GOTO old
IF EXIST %1tutorial.7z DEL %1tutorial.7z > nul
GOTO 7z
:old
IF NOT EXIST %1tutorial.7z GOTO 7z
IF EXIST %1tutorial.old.7z DEL %1tutorial.old.7z > nul
REN %1tutorial.7z tutorial.old.7z
:7z
7z a -mx -r -xr@exclude7z.txt %1tutorial *.mf *.xml *.java *.bat *.txt  src\*.* lib\*.* web\*.* nbproject\*.* > nul
