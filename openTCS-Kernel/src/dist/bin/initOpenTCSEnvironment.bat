@echo off
rem
rem Initialize environment variables for the actual startup scripts.
rem

rem Avoid executing the initialization of the environment more than once.
if defined OPENTCS_ENV_SET (
    goto end
)
set OPENTCS_ENV_SET=1

rem XXX Be a bit more clever to find out the name of the JVM runtime.
set JAVA=javaw

rem Set base directory names.
set OPENTCS_CONFIGDIR=%OPENTCS_HOME%\config
set OPENTCS_LIBDIR=%OPENTCS_BASE%\lib

:end

