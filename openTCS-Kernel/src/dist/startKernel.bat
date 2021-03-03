@echo off
rem
rem Start the openTCS kernel.
rem

rem Set window title
title Kernel (openTCS)

rem Don't export variables to the parent shell
setlocal

rem Set openTCS base and home directory
set OPENTCS_BASE=.
set OPENTCS_HOME=.

call %OPENTCS_HOME%\bin\initOpenTCSEnvironment.bat

rem Set the class path
set OPENTCS_CP=%OPENTCS_LIBDIR%\*;
set OPENTCS_CP=%OPENTCS_CP%;%OPENTCS_LIBDIR%\openTCS-extensions\*;

rem Start kernel
start /b %JAVA% -enableassertions ^
    -Dopentcs.base="%OPENTCS_BASE%" ^
    -Dopentcs.home="%OPENTCS_HOME%" ^
    -Djava.util.logging.config.file="%OPENTCS_CONFIGDIR%\logging.config" ^
    -Djava.security.policy="file:%OPENTCS_CONFIGDIR%\java.policy" ^
    -Dorg.opentcs.util.configuration.xml.file="%OPENTCS_CONFIGDIR%\openTCS-config.xml" ^
    -Dorg.opentcs.util.configuration.saveonexit=true ^
    -Dorg.opentcs.virtualvehicle.profiles.file="%OPENTCS_CONFIGDIR%\virtualvehicle-profiles.xml" ^
    -Xrunjdwp:transport=dt_socket,address=8000,server=y,suspend=n ^
    -XX:-OmitStackTraceInFastThrow ^
    -classpath "%OPENTCS_CP%" ^
    -splash:bin/splash-image.gif ^
    org.opentcs.kernel.RunKernel
