@ECHO OFF
FOR /R %1 %%i IN (*.java~) DO IF EXIST %%i DEL %%i > nul
FOR /R %1 %%i IN (*.xml~) DO IF EXIST %%i DEL %%i > nul
FOR /R %1 %%i IN (*.mf~) DO IF EXIST %%i DEL %%i > nul
FOR /R %1 %%i IN (*.html~) DO IF EXIST %%i DEL %%i > nul
FOR /R %1 %%i IN (.nbattrs) DO IF EXIST %%i DEL %%i > nul
