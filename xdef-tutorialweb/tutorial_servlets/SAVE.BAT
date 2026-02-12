@ECHO off
CALL cleanSrc.bat
IF NOT %1. == . GOTO old
IF EXIST %1tutorial_servlets.7z DEL %1tutorial_servlets.7z > nul
GOTO 7z
:old
IF NOT EXIST %1tutorial_servlets.7z GOTO 7z
IF EXIST %1tutorial_servlets.old.7z DEL %1tutorial_servlets.old.7z > nul
REN %1tutorial_servlets.7z tutorial_servlets.old.7z
:7z
7z a -mx -r -xr@exclude7z.txt %1tutorial_servlets *.mf *.java *.xml *.bat *.txt src\*.* lib\*.* test\*.* nbproject\*.* > nul
