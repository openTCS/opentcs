#!/bin/sh
#
# Start SSL keystore and truststore generation.
#

# Set the path to where keytool is located. Keytool comes with the Java JRE.
export KEYTOOL_PATH="keytool"

# Try to execute keytool
${KEYTOOL_PATH} 2>/dev/null
if [ $? -ne 0 ]; then
	if [ "${KEYTOOL_PATH}" = "keytool" ]; then
		echo Error: Could not find keytool in PATH.
	else
		echo Error: Could not find keytool in ${KEYTOOL_PATH}.
	fi
	exit $?
fi

# Set base directory names.
export OPENTCS_HOME=.
export OPENTCS_CONFIGDIR="${OPENTCS_HOME}/config"
export OUTPUTDIR="${OPENTCS_CONFIGDIR}"

# Set paths to generate files at.
export KEYSTORE_FILEPATH="${OUTPUTDIR}/keystore.p12"
export TRUSTSTORE_FILEPATH="${OUTPUTDIR}/truststore.p12"
export CERTIFICATE_FILEPATH="${OUTPUTDIR}/certificate.cer"

# Set the password used for generating the stores.
export PASSWORD=password

echo Deleting previously generated keystore and truststore...
rm ${KEYSTORE_FILEPATH} 2>/dev/null
rm ${TRUSTSTORE_FILEPATH} 2>/dev/null
rm ${CERTIFICATE_FILEPATH} 2>/dev/null

# Generates a keypair wrapped in a self-signed (X.509) certificate.
# Some defaults of the -genkeypair command: -alias "mykey" -keyalg "DSA" -keysize 1024 -validity 90
echo Generating a new keystore in ${KEYSTORE_FILEPATH}...
${KEYTOOL_PATH} -genkeypair -alias openTCS -keyalg RSA -dname "c=DE" -storepass ${PASSWORD} -keypass ${PASSWORD} -validity 365 -storetype PKCS12 -keystore ${KEYSTORE_FILEPATH}

# Exports the (wrapping) self-signed certificate from the generated keypair.
# '-rfc' - Output the certificate in a printable encoding format (by default the -export command outputs a certificate in binary encoding)
# '2>nul' - Suppress output of this command
${KEYTOOL_PATH} -exportcert -alias openTCS -file ${CERTIFICATE_FILEPATH} -keystore ${KEYSTORE_FILEPATH} -storepass ${PASSWORD} -rfc 2>/dev/null

# Adds the exported certificate to a new keystore and its trusted certificates.
echo Generating a new truststore in ${TRUSTSTORE_FILEPATH}...
${KEYTOOL_PATH} -importcert -alias openTCS -file ${CERTIFICATE_FILEPATH} -storepass ${PASSWORD} -storetype PKCS12 -keystore ${TRUSTSTORE_FILEPATH} -noprompt 2>/dev/null

# Delete the exported certificate since it's not really needed.
rm ${CERTIFICATE_FILEPATH}

echo Copy the generated truststore to the openTCS PlantOverview\'s /config folder or a corresponding location of your application.
