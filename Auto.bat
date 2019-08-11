@echo off
cls
:start
ECHO.
echo 1. - Unpack SZS
echo 2. -
echo 3. - Exit
echo This Requires Python 3.x!
set choice=
set /p choice=Type the number to choose the option. 
if not '%choice%'=='' set choice=%choice:~0,1%
if '%choice%'=='1' goto one
if '%choice%'=='2' goto two
if '%choice%'=='3' goto leave
ECHO "%choice%" is not valid, try again
ECHO.
goto start
------------------------------
:one
cls
set szspath=
set /p szspath=Drag and drop the file to extract here. 
py -3 szstool.py -en %szspath%
cls
goto start
------------------------------
:two
cls

------------------------------
:leave
exit