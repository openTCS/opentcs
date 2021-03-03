@echo off
rem
rem Start the openTCS plant overview client.
rem

rem Set window title
title Plant Overview (openTCS)

rem Don't export variables to the parent shell
setlocal

rem Set openTCS base and home directory
set OPENTCS_BASE=.
set OPENTCS_HOME=.

call %OPENTCS_HOME%\bin\initOpenTCSEnvironment.bat

rem Set the class path
set OPENTCS_CP=%OPENTCS_LIBDIR%\*;
set OPENTCS_CP=%OPENTCS_CP%;%OPENTCS_LIBDIR%\openTCS-extensions\*;

rem Start plant overview
start /b %JAVA% -enableassertions ^
    -Dopentcs.home="%OPENTCS_HOME%" ^
    -Dopentcs.dockinglayout="%OPENTCS_CONFIGDIR%\plantoverview-docking-layout.xml" ^
    -Dopentcs.kernel.host=localhost ^
    -Dopentcs.kernel.port=1099 ^
    -Djava.util.logging.config.file="%OPENTCS_CONFIGDIR%\logging-plantoverview.config" ^
    -Dorg.opentcs.util.configuration.xml.file="%OPENTCS_CONFIGDIR%\openTCS-plantoverview-config.xml" ^
    -Dorg.opentcs.util.configuration.saveonexit=true ^
    -Dsun.java2d.d3d=false ^
    -Xrunjdwp:transport=dt_socket,address=8002,server=y,suspend=n ^
    -XX:-OmitStackTraceInFastThrow ^
    -classpath "%OPENTCS_CP%" ^
    -splash:bin/splash-image.gif ^
    org.opentcs.guing.RunPlantOverview
