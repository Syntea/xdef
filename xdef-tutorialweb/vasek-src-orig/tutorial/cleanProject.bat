@ECHO OFF
if exist temp\nul rd /Q /S temp
if exist build\nul rd /Q /S build
if exist dist\nul rd /Q /S dist
if exist userdocs\*.wbk del userdocs\*.wbk > nul
if exist *.log del *.log > nul
call cleanSrc.bat
