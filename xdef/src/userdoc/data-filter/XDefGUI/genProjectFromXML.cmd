@ECHO off

java.exe -cp ../xdef-${project.version}.jar org.xdef.util.GUIEditor -g %1 %2 -workDir temp
