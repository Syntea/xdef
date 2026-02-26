@ECHO off
if %1. == . goto noparams
java.exe -cp ../xdef-${version}.jar;%2 org.xdef.util.GUIEditor -p projects/%1/project.xml
goto end
:noparams
@ECHO ***********************
@ECHO * Missing parameters! *
@ECHO ***********************
@ECHO First parameter is name of directory in projects (it must contain the file project.xml)
@ECHO The second parameter is optional. It is a list jar files added to classpath (separatoe is ";")
@ECHO .
pause
:end
