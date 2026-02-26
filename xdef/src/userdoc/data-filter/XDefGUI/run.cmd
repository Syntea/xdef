@ECHO off
if %1. == . goto noparams
java.exe -cp ../xdef-${version}.jar org.xdef.util.GUIEditor %1 %2 %3 %4 %5 %6
goto finish
:noparams
java.exe -cp ../xdef-${version}.jar org.xdef.util.GUIEditor -h
:finish