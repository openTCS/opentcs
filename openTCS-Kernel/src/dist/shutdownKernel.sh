#!/bin/sh
#
# Shut down the openTCS kernel.
#

# Set base directory names.
export OPENTCS_BASE=.
export OPENTCS_LIBDIR="${OPENTCS_BASE}/lib"

# Set the class path
export OPENTCS_CP="${OPENTCS_LIBDIR}/*"
export OPENTCS_CP="${OPENTCS_CP}:${OPENTCS_LIBDIR}/openTCS-extensions/*"

if [ -n "${OPENTCS_JAVAVM}" ]; then
    export JAVA="${OPENTCS_JAVAVM}"
else
    # XXX Be a bit more clever to find out the name of the JVM runtime.
    export JAVA="java"
fi

${JAVA} -classpath "${OPENTCS_CP}" \
    org.opentcs.kernel.ShutdownKernel localhost 55100
