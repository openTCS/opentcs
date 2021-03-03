#!/bin/sh
#
# Start the openTCS plant overview client.
#

# Set openTCS base and home directory
export OPENTCS_BASE=.
export OPENTCS_HOME=.

# Initialize environment
. ${OPENTCS_HOME}/bin/initOpenTCSEnvironment.sh

# Set the class path
export OPENTCS_CP="${OPENTCS_LIBDIR}/*"
export OPENTCS_CP="${OPENTCS_CP}:${OPENTCS_LIBDIR}/openTCS-extensions/*"

# Start plant overview
${JAVA} -enableassertions \
    -Dopentcs.home="${OPENTCS_HOME}" \
    -Dopentcs.dockinglayout="${OPENTCS_CONFIGDIR}/plantoverview-docking-layout.xml" \
    -Dopentcs.kernel.host=localhost \
    -Dopentcs.kernel.port=1099 \
    -Djava.util.logging.config.file=${OPENTCS_CONFIGDIR}/logging-plantoverview.config \
    -Dorg.opentcs.util.configuration.xml.file="${OPENTCS_CONFIGDIR}/openTCS-plantoverview-config.xml" \
    -Dorg.opentcs.util.configuration.saveonexit=true \
    -Xrunjdwp:transport=dt_socket,address=8002,server=y,suspend=n \
    -XX:-OmitStackTraceInFastThrow \
    -classpath "${OPENTCS_CP}" \
    -splash:bin/splash-image.gif \
    org.opentcs.guing.RunPlantOverview
