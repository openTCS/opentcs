// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.commadapter.vehicle.vda5050.v2_0.ordermapping;

import static java.util.Objects.requireNonNull;
import static org.opentcs.commadapter.vehicle.vda5050.v2_0.ObjectProperties.PROPKEY_VEHICLE_OPTIONAL_ORDER_PARAMETER_PREFIX;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import org.opentcs.commadapter.vehicle.vda5050.common.OptionalParameterSupport;
import org.opentcs.data.model.Vehicle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Sets up filtering of unsupported properties in order messages.
 */
public class UnsupportedPropertiesExtractor
    implements
      Function<Vehicle, Map<String, OptionalParameterSupport>> {

  private static final Logger LOG = LoggerFactory.getLogger(UnsupportedPropertiesExtractor.class);

  public UnsupportedPropertiesExtractor() {
  }

  @Override
  public Map<String, OptionalParameterSupport> apply(
      @Nonnull
      Vehicle vehicle
  ) {
    requireNonNull(vehicle, "vehicle");
    Map<String, OptionalParameterSupport> vehicleOptionalParameters
        = initializeDefaultOptionalParameters();

    vehicle.getProperties().entrySet().stream()
        .filter(entry -> entry.getKey().startsWith(PROPKEY_VEHICLE_OPTIONAL_ORDER_PARAMETER_PREFIX))
        .forEach(
            entry -> vehicleOptionalParameters.replace(
                entry.getKey(), determineOptionalParameterSupportValue(entry.getValue())
            )
        );

    return vehicleOptionalParameters.entrySet()
        .stream()
        .filter(entry -> entry.getValue().equals(OptionalParameterSupport.NOT_SUPPORTED))
        .collect(
            Collectors.toMap(
                entry -> entry.getKey().substring(
                    (PROPKEY_VEHICLE_OPTIONAL_ORDER_PARAMETER_PREFIX + ".").length()
                ),
                Map.Entry::getValue
            )
        );
  }

  private OptionalParameterSupport determineOptionalParameterSupportValue(String value) {
    try {
      return OptionalParameterSupport.valueOf(value);
    }
    catch (IllegalArgumentException exc) {
      LOG.info(
          "Unknown optional parameter support value: '{}', falling back to default: 'SUPPORTED'.",
          value
      );
      return OptionalParameterSupport.SUPPORTED;
    }
  }

  private static Map<String, OptionalParameterSupport> initializeDefaultOptionalParameters() {
    return new HashMap<>() {
      {
        put(
            PROPKEY_VEHICLE_OPTIONAL_ORDER_PARAMETER_PREFIX + ".zoneSetId",
            OptionalParameterSupport.SUPPORTED
        );
        put(
            PROPKEY_VEHICLE_OPTIONAL_ORDER_PARAMETER_PREFIX + ".nodes.nodeDescription",
            OptionalParameterSupport.SUPPORTED
        );
        put(
            PROPKEY_VEHICLE_OPTIONAL_ORDER_PARAMETER_PREFIX + ".nodes.nodePosition",
            OptionalParameterSupport.SUPPORTED
        );
        put(
            PROPKEY_VEHICLE_OPTIONAL_ORDER_PARAMETER_PREFIX + ".nodes.nodePosition.theta",
            OptionalParameterSupport.SUPPORTED
        );
        put(
            PROPKEY_VEHICLE_OPTIONAL_ORDER_PARAMETER_PREFIX
                + ".nodes.nodePosition.allowedDeviationXY",
            OptionalParameterSupport.SUPPORTED
        );
        put(
            PROPKEY_VEHICLE_OPTIONAL_ORDER_PARAMETER_PREFIX
                + ".nodes.nodePosition.allowedDeviationTheta",
            OptionalParameterSupport.SUPPORTED
        );
        put(
            PROPKEY_VEHICLE_OPTIONAL_ORDER_PARAMETER_PREFIX + ".nodes.nodePosition.mapDescription",
            OptionalParameterSupport.SUPPORTED
        );
        put(
            PROPKEY_VEHICLE_OPTIONAL_ORDER_PARAMETER_PREFIX + ".nodes.actions.actionDescription",
            OptionalParameterSupport.SUPPORTED
        );
        put(
            PROPKEY_VEHICLE_OPTIONAL_ORDER_PARAMETER_PREFIX + ".nodes.actions.actionParameters",
            OptionalParameterSupport.SUPPORTED
        );
        put(
            PROPKEY_VEHICLE_OPTIONAL_ORDER_PARAMETER_PREFIX + ".edges.edgeDescription",
            OptionalParameterSupport.SUPPORTED
        );
        put(
            PROPKEY_VEHICLE_OPTIONAL_ORDER_PARAMETER_PREFIX + ".edges.maxSpeed",
            OptionalParameterSupport.SUPPORTED
        );
        put(
            PROPKEY_VEHICLE_OPTIONAL_ORDER_PARAMETER_PREFIX + ".edges.maxHeight",
            OptionalParameterSupport.SUPPORTED
        );
        put(
            PROPKEY_VEHICLE_OPTIONAL_ORDER_PARAMETER_PREFIX + ".edges.minHeight",
            OptionalParameterSupport.SUPPORTED
        );
        put(
            PROPKEY_VEHICLE_OPTIONAL_ORDER_PARAMETER_PREFIX + ".edges.orientation",
            OptionalParameterSupport.SUPPORTED
        );
        put(
            PROPKEY_VEHICLE_OPTIONAL_ORDER_PARAMETER_PREFIX + ".edges.orientationType",
            OptionalParameterSupport.SUPPORTED
        );
        put(
            PROPKEY_VEHICLE_OPTIONAL_ORDER_PARAMETER_PREFIX + ".edges.direction",
            OptionalParameterSupport.SUPPORTED
        );
        put(
            PROPKEY_VEHICLE_OPTIONAL_ORDER_PARAMETER_PREFIX + ".edges.rotationAllowed",
            OptionalParameterSupport.SUPPORTED
        );
        put(
            PROPKEY_VEHICLE_OPTIONAL_ORDER_PARAMETER_PREFIX + ".edges.maxRotationSpeed",
            OptionalParameterSupport.SUPPORTED
        );
        put(
            PROPKEY_VEHICLE_OPTIONAL_ORDER_PARAMETER_PREFIX + ".edges.trajectory",
            OptionalParameterSupport.SUPPORTED
        );
        put(
            PROPKEY_VEHICLE_OPTIONAL_ORDER_PARAMETER_PREFIX + ".edges.length",
            OptionalParameterSupport.SUPPORTED
        );
        put(
            PROPKEY_VEHICLE_OPTIONAL_ORDER_PARAMETER_PREFIX + ".edges.trajectory.degree",
            OptionalParameterSupport.SUPPORTED
        );
        put(
            PROPKEY_VEHICLE_OPTIONAL_ORDER_PARAMETER_PREFIX
                + ".edges.trajectory.controlPoints.weight",
            OptionalParameterSupport.SUPPORTED
        );
      }
    };
  }
}
