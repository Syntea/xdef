@ECHO OFF
if exist temp\nul rd /Q /S temp
if exist build\nul rd /Q /S build
if exist dist\nul rd /Q /S dist
call cleanSrc.bat
if exist userdocs\*.wbk del userdocs\*.wbk > nul