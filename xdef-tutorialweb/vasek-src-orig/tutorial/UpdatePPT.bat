@ECHO OFF
if .%1 == . goto chyba
if not exist web\%1\img1.html goto chyba
%JAVA_1_8%\bin\javac -encoding UTF8 UpdatePPT.java
%JAVA_14%\bin\java UpdatePPT web\%1
goto konec

:chyba
echo Incorrect parameter

:konec