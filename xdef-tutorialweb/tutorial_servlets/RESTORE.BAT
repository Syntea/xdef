@ECHO off
IF NOT EXIST %1tutorial_servlets.7z GOTO end
IF EXIST build\nul RD build /Q /S > nul
IF EXIST dist\nul RD dist /Q /S > nul
CALL cleanProject.bat
IF NOT EXIST nbproject\nul GOTO noproject
IF EXIST nbproject.old\nul RD /Q /S nbproject.old > nul
REN nbproject nbproject.old > nul
MD nbproject > nul
:noproject
IF NOT EXIST src\nul GOTO nosrc
IF EXIST src.old\nul RD /Q /S src.old
REN src src.old > nul
MD src > nul
:nosrc
IF NOT EXIST lib\nul GOTO nolib
IF EXIST lib.old\nul RD /Q /S lib.old > nul
REN lib lib.old > nul
md lib > nul
:nolib
IF NOT EXIST test\nul GOTO notest
IF EXIST test.old\nul RD /Q /S test.old > nul
REN test test.old > nul
md test > nul
:notutorial

7z x -y %1tutorial_servlets.7z > nul
IF NOT %1. == . GOTO end
IF EXIST %1tutorial_servlets.old.7z DEL %1tutorial_servlets.old.7z > nul
REN %1tutorial_servlets.7z tutorial_servlets.old.7z > nul
:end