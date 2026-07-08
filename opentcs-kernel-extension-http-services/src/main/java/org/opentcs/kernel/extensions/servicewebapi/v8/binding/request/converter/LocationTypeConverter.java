// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.kernel.extensions.servicewebapi.v8.binding.request.converter;

import static java.util.Objects.requireNonNull;

import jakarta.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.opentcs.access.to.model.LocationRepresentationTO;
import org.opentcs.access.to.model.LocationTypeCreationTO;
import org.opentcs.kernel.extensions.servicewebapi.v8.binding.request.data.LocationTypeTO;

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

  @SuppressWarnings("checkstyle:LineLength")
  private LocationRepresentationTO convertToLocationRepresentation(
      org.opentcs.kernel.extensions.servicewebapi.v8.binding.request.data.LocationRepresentationTO locationRepresentation
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
}
