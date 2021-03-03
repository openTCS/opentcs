#!/bin/sh
#
# Initialize environment variables for the actual startup scripts.
#

# Ensure that the openTCS base directory is known.
if [ "${OPENTCS_BASE}x" = "x" ]; then
    echo "OPENTCS_BASE not set, aborting."
    exit 1
fi

# Ensure that the openTCS home directory is known.
if [ "${OPENTCS_HOME}x" = "x" ]; then
    echo "OPENTCS_HOME not set, aborting."
    exit 1
fi

if [ "${OPENTCS_ENV_SET}x" = "x" ]; then
    echo "Initializing openTCS environment..."
    OPENTCS_ENV_SET=1

    if [ -n "${OPENTCS_JAVAVM}" ]; then
        export JAVA="${OPENTCS_JAVAVM}"
    else
        # XXX Be a bit more clever to find out the name of the JVM runtime.
        export JAVA="java"
    fi

    # Set base directory names.
    export OPENTCS_CONFIGDIR="${OPENTCS_HOME}/config"
    export OPENTCS_LIBDIR="${OPENTCS_BASE}/lib"
fi
