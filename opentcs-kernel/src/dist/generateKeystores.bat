@echo off
rem
rem Start SSL keystore and truststore generation.
rem

rem Don't export variables to the parent shell.
setlocal

rem Set the path to where keytool is located. Keytool comes with the Java JRE.
set KEYTOOL_PATH="keytool"

rem Try to execute keytool
%KEYTOOL_PATH% 2>nul
if %ERRORLEVEL% neq 0 (
	if %KEYTOOL_PATH% equ "keytool" (
		echo Error: Could not find keytool in PATH.
	) else (
		echo Error: Could not find keytool in %KEYTOOL_PATH%.
	)
	exit /B %ERRORLEVEL%
)

rem Set base directory names.
set OPENTCS_HOME=.
set OPENTCS_CONFIGDIR=%OPENTCS_HOME%\config
set OUTPUTDIR=%OPENTCS_CONFIGDIR%

rem Set paths to generate files at.
set KEYSTORE_FILEPATH=%OUTPUTDIR%\keystore.p12
set TRUSTSTORE_FILEPATH=%OUTPUTDIR%\truststore.p12
set CERTIFICATE_FILEPATH=%OUTPUTDIR%\certificate.cer

rem Set the password used for generating the stores.
set PASSWORD=password

echo Deleting previously generated keystore and truststore...
del %KEYSTORE_FILEPATH% 2>nul
del %TRUSTSTORE_FILEPATH% 2>nul
del %CERTIFICATE_FILEPATH% 2>nul

rem Generates a keypair wrapped in a self-signed (X.509) certificate.
rem Some defaults of the -genkeypair command: -alias "mykey" -keyalg "DSA" -keysize 1024 -validity 90
echo Generating a new keystore in %KEYSTORE_FILEPATH%...
%KEYTOOL_PATH% -genkeypair -alias openTCS -keyalg RSA -dname "c=DE" -storepass %PASSWORD% -keypass %PASSWORD% -validity 365 -storetype PKCS12 -keystore %KEYSTORE_FILEPATH%

rem Exports the (wrapping) self-signed certificate from the generated keypair.
rem '-rfc' - Output the certificate in a printable encoding format (by default the -export command outputs a certificate in binary encoding)
rem '2>nul' - Suppress output of this command
%KEYTOOL_PATH% -exportcert -alias openTCS -file %CERTIFICATE_FILEPATH% -keystore %KEYSTORE_FILEPATH% -storepass %PASSWORD% -rfc 2>nul

rem Adds the exported certificate to a new keystore and its trusted certificates.
echo Generating a new truststore in %TRUSTSTORE_FILEPATH%...
%KEYTOOL_PATH% -importcert -alias openTCS -file %CERTIFICATE_FILEPATH% -storepass %PASSWORD% -storetype PKCS12 -keystore %TRUSTSTORE_FILEPATH% -noprompt 2>nul

rem Delete the exported certificate since it's not really needed.
del %CERTIFICATE_FILEPATH%

echo Copy the generated truststore to the openTCS PlantOverview's \config folder or a corresponding location of your application.

pause
