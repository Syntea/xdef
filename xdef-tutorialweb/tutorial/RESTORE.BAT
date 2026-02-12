@ECHO off
IF NOT EXIST %1tutorial.7z GOTO end
IF EXIST build\nul RD build /Q /S > nul
IF EXIST dist\nul RD dist /Q /S > nul
call cleanProject.bat

if not exist nbproject\nul goto noproject
if exist nbproject.old\nul rd /Q /S nbproject.old
ren nbproject nbproject.old
md nbproject
:noproject

if not exist src\nul goto nosrc
if exist src.old\nul rd /Q /S src.old
ren src src.old
md src
:nosrc

if not exist lib\nul goto nolib
if exist lib.old\nul rd /Q /S lib.old
ren lib lib.old
md lib
:nolib

if not exist web\nul goto noweb
if exist web.old\nul rd /Q /S web.old
ren web web.old
md web
:noweb

7z x -y %1tutorial.7z > nul
IF NOT %1. == . GOTO end
if exist %1tutorial.old.7z del %1tutorial.old.7z
ren %1tutorial.7z tutorial.old.7z
:end