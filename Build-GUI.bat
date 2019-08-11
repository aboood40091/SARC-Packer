@echo off
cls
:start
ECHO.
echo 1. - To Build
echo 2. - To Build First Time
echo This Requires Python 3.x!
set choice=
set /p choice=Type the number to choose the option.
if not '%choice%'=='' set choice=%choice:~0,1%
if '%choice%'=='1' goto one
if '%choice%'=='2' goto two
ECHO "%choice%" is not valid, try again
ECHO.
goto start
------------------------------
:one
cls
pyinstaller --icon=szstool.ico --onefile szstoolgui.py
echo Done!
pause
exit
------------------------------
:two
cls
pip install pyinstaller
pyinstaller --icon=szstool.ico --onefile szstoolgui.py
echo Done!
pause
exit 