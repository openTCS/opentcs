#!/bin/sh
#
# Start the openTCS kernel.
#

# Set openTCS base and home directory
export OPENTCS_BASE=.
export OPENTCS_HOME=.

# Initialize environment
. ${OPENTCS_HOME}/bin/initOpenTCSEnvironment.sh

# Set the class path
export OPENTCS_CP="${OPENTCS_LIBDIR}/*"
export OPENTCS_CP="${OPENTCS_CP}:${OPENTCS_LIBDIR}/openTCS-extensions/*"

# Start kernel
${JAVA} -enableassertions \
    -Dopentcs.base="${OPENTCS_BASE}" \
    -Dopentcs.home="${OPENTCS_HOME}" \
    -Djava.util.logging.config.file=${OPENTCS_CONFIGDIR}/logging.config \
    -Djava.security.policy=file:${OPENTCS_CONFIGDIR}/java.policy \
    -Dorg.opentcs.util.configuration.xml.file="${OPENTCS_CONFIGDIR}/openTCS-config.xml" \
    -Dorg.opentcs.util.configuration.saveonexit=true \
    -Dorg.opentcs.virtualvehicle.profiles.file="${OPENTCS_CONFIGDIR}/virtualvehicle-profiles.xml" \
    -Xrunjdwp:transport=dt_socket,address=8000,server=y,suspend=n \
    -XX:-OmitStackTraceInFastThrow \
    -classpath "${OPENTCS_CP}" \
    -splash:bin/splash-image.gif \
    org.opentcs.kernel.RunKernel
