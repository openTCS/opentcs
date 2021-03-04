@echo off
rem
rem Start the openTCS plant overview client.
rem

rem Set window title
title Plant Overview (openTCS)

rem Don't export variables to the parent shell
setlocal

rem Set base directory names.
set OPENTCS_BASE=.
set OPENTCS_HOME=.
set OPENTCS_CONFIGDIR=%OPENTCS_HOME%\config
set OPENTCS_LIBDIR=%OPENTCS_BASE%\lib

rem Set the class path
set OPENTCS_CP=%OPENTCS_LIBDIR%\*;
set OPENTCS_CP=%OPENTCS_CP%;%OPENTCS_LIBDIR%\openTCS-extensions\*;

rem XXX Be a bit more clever to find out the name of the JVM runtime.
set JAVA=javaw

rem Start plant overview
start /b %JAVA% -enableassertions ^
    -Dopentcs.home="%OPENTCS_HOME%" ^
    -Djava.util.logging.config.file="%OPENTCS_CONFIGDIR%\logging.config" ^
    -Djava.security.policy="file:%OPENTCS_CONFIGDIR%\java.policy" ^
    -Dsun.java2d.d3d=false ^
    -XX:-OmitStackTraceInFastThrow ^
    -classpath "%OPENTCS_CP%" ^
    -splash:bin/splash-image.gif ^
    org.opentcs.guing.RunPlantOverview
