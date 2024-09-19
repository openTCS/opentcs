@echo off
rem
rem Shut down the openTCS kernel.
rem

rem Don't export variables to the parent shell
setlocal

rem Set base directory names.
set OPENTCS_BASE=.
set OPENTCS_LIBDIR=%OPENTCS_BASE%\lib

rem Set the class path
set OPENTCS_CP=%OPENTCS_LIBDIR%\*;
set OPENTCS_CP=%OPENTCS_CP%;%OPENTCS_LIBDIR%\openTCS-extensions\*;

java -classpath "%OPENTCS_CP%" ^
    org.opentcs.kernel.ShutdownKernel localhost 55100
