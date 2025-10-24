// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.kernel.extensions.servicewebapi.v1.converter;

import static java.util.Objects.requireNonNull;
import static org.opentcs.kernel.extensions.servicewebapi.v1.binding.plantmodel.LocationRepresentationTO.DEFAULT;
import static org.opentcs.kernel.extensions.servicewebapi.v1.binding.plantmodel.LocationRepresentationTO.LOAD_TRANSFER_ALT_1;
import static org.opentcs.kernel.extensions.servicewebapi.v1.binding.plantmodel.LocationRepresentationTO.LOAD_TRANSFER_ALT_2;
import static org.opentcs.kernel.extensions.servicewebapi.v1.binding.plantmodel.LocationRepresentationTO.LOAD_TRANSFER_ALT_3;
import static org.opentcs.kernel.extensions.servicewebapi.v1.binding.plantmodel.LocationRepresentationTO.LOAD_TRANSFER_ALT_4;
import static org.opentcs.kernel.extensions.servicewebapi.v1.binding.plantmodel.LocationRepresentationTO.LOAD_TRANSFER_ALT_5;
import static org.opentcs.kernel.extensions.servicewebapi.v1.binding.plantmodel.LocationRepresentationTO.LOAD_TRANSFER_GENERIC;
import static org.opentcs.kernel.extensions.servicewebapi.v1.binding.plantmodel.LocationRepresentationTO.NONE;
import static org.opentcs.kernel.extensions.servicewebapi.v1.binding.plantmodel.LocationRepresentationTO.RECHARGE_ALT_1;
import static org.opentcs.kernel.extensions.servicewebapi.v1.binding.plantmodel.LocationRepresentationTO.RECHARGE_ALT_2;
import static org.opentcs.kernel.extensions.servicewebapi.v1.binding.plantmodel.LocationRepresentationTO.RECHARGE_GENERIC;
import static org.opentcs.kernel.extensions.servicewebapi.v1.binding.plantmodel.LocationRepresentationTO.WORKING_ALT_1;
import static org.opentcs.kernel.extensions.servicewebapi.v1.binding.plantmodel.LocationRepresentationTO.WORKING_ALT_2;
import static org.opentcs.kernel.extensions.servicewebapi.v1.binding.plantmodel.LocationRepresentationTO.WORKING_GENERIC;

import jakarta.inject.Inject;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.opentcs.access.to.model.LocationRepresentationTO;
import org.opentcs.access.to.model.LocationTypeCreationTO;
import org.opentcs.data.model.LocationType;
import org.opentcs.data.model.visualization.LocationRepresentation;
import org.opentcs.kernel.extensions.servicewebapi.v1.binding.plantmodel.LocationTypeTO;

/**
 * Includes the conversion methods for all LocationType classes.
 */
public class LocationTypeConverter {

  private final PropertyConverter pConverter;

  @Inject
  public LocationTypeConverter(PropertyConverter pConverter) {
    this.pConverter = requireNonNull(pConverter, "pConverter");
  }

  public List<LocationTypeCreationTO> toLocationTypeCreationTOs(List<LocationTypeTO> locTypes) {
    return locTypes.stream()
        .map(
            locationType -> new LocationTypeCreationTO(locationType.getName())
                .withAllowedOperations(locationType.getAllowedOperations())
                .withAllowedPeripheralOperations(
                    locationType.getAllowedPeripheralOperations()
                )
                .withProperties(pConverter.toPropertyMap(locationType.getProperties()))
                .withLayout(
                    new LocationTypeCreationTO.Layout(
                        convertToLocationRepresentation(
                            locationType.getLayout().getLocationRepresentation()
                        )
                    )
                )
        )
        .collect(Collectors.toCollection(ArrayList::new));
  }

  public List<LocationTypeTO> toLocationTypeTOs(Set<LocationType> locationTypes) {
    return locationTypes.stream()
        .map(
            locationType -> new LocationTypeTO(locationType.getName())
                .setProperties(pConverter.toPropertyTOs(locationType.getProperties()))
                .setAllowedOperations(locationType.getAllowedOperations())
                .setAllowedPeripheralOperations(locationType.getAllowedPeripheralOperations())
                .setLayout(
                    new LocationTypeTO.Layout()
                        .setLocationRepresentation(
                            convertToLocationRepresentationTO(
                                locationType.getLayout().getLocationRepresentation()
                            )
                        )
                )
        )
        .sorted(Comparator.comparing(LocationTypeTO::getName))
        .collect(Collectors.toList());
  }

  @SuppressWarnings("checkstyle:LineLength")
  private LocationRepresentationTO convertToLocationRepresentation(
      org.opentcs.kernel.extensions.servicewebapi.v1.binding.plantmodel.LocationRepresentationTO locationRepresentation
  ) {
    return switch (locationRepresentation) {
      case DEFAULT -> LocationRepresentationTO.DEFAULT;
      case LOAD_TRANSFER_ALT_1 -> LocationRepresentationTO.LOAD_TRANSFER_ALT_1;
      case LOAD_TRANSFER_ALT_2 -> LocationRepresentationTO.LOAD_TRANSFER_ALT_2;
      case LOAD_TRANSFER_ALT_3 -> LocationRepresentationTO.LOAD_TRANSFER_ALT_3;
      case LOAD_TRANSFER_ALT_4 -> LocationRepresentationTO.LOAD_TRANSFER_ALT_4;
      case LOAD_TRANSFER_ALT_5 -> LocationRepresentationTO.LOAD_TRANSFER_ALT_5;
      case LOAD_TRANSFER_GENERIC -> LocationRepresentationTO.LOAD_TRANSFER_GENERIC;
      case NONE -> LocationRepresentationTO.NONE;
      case RECHARGE_ALT_1 -> LocationRepresentationTO.RECHARGE_ALT_1;
      case RECHARGE_ALT_2 -> LocationRepresentationTO.RECHARGE_ALT_2;
      case RECHARGE_GENERIC -> LocationRepresentationTO.RECHARGE_GENERIC;
      case WORKING_ALT_1 -> LocationRepresentationTO.WORKING_ALT_1;
      case WORKING_ALT_2 -> LocationRepresentationTO.WORKING_ALT_2;
      case WORKING_GENERIC -> LocationRepresentationTO.WORKING_GENERIC;
    };
  }

  private org.opentcs.kernel.extensions.servicewebapi.v1.binding.plantmodel.LocationRepresentationTO
      convertToLocationRepresentationTO(
          LocationRepresentation locationRepresentation
      ) {
    return switch (locationRepresentation) {
      case DEFAULT -> DEFAULT;
      case LOAD_TRANSFER_ALT_1 -> LOAD_TRANSFER_ALT_1;
      case LOAD_TRANSFER_ALT_2 -> LOAD_TRANSFER_ALT_2;
      case LOAD_TRANSFER_ALT_3 -> LOAD_TRANSFER_ALT_3;
      case LOAD_TRANSFER_ALT_4 -> LOAD_TRANSFER_ALT_4;
      case LOAD_TRANSFER_ALT_5 -> LOAD_TRANSFER_ALT_5;
      case LOAD_TRANSFER_GENERIC -> LOAD_TRANSFER_GENERIC;
      case NONE -> NONE;
      case RECHARGE_ALT_1 -> RECHARGE_ALT_1;
      case RECHARGE_ALT_2 -> RECHARGE_ALT_2;
      case RECHARGE_GENERIC -> RECHARGE_GENERIC;
      case WORKING_ALT_1 -> WORKING_ALT_1;
      case WORKING_ALT_2 -> WORKING_ALT_2;
      case WORKING_GENERIC -> WORKING_GENERIC;
    };
  }
}
