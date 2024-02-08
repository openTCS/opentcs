/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.kernel.extensions.servicewebapi.v1.converter;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import static java.util.Objects.requireNonNull;
import java.util.Set;
import java.util.stream.Collectors;
import javax.inject.Inject;
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
                    locationType.getAllowedPeripheralOperations())
                .withProperties(pConverter.toPropertyMap(locationType.getProperties()))
                .withLayout(new LocationTypeCreationTO.Layout(
                    LocationRepresentation.valueOf(
                        locationType.getLayout().getLocationRepresentation()))))
        .collect(Collectors.toCollection(ArrayList::new));
  }

  public List<LocationTypeTO> toLocationTypeTOs(Set<LocationType> locationTypes) {
    return locationTypes.stream()
        .map(locationType -> new LocationTypeTO(locationType.getName())
        .setProperties(pConverter.toPropertyTOs(locationType.getProperties()))
        .setAllowedOperations(locationType.getAllowedOperations())
        .setAllowedPeripheralOperations(locationType.getAllowedPeripheralOperations())
        .setLayout(new LocationTypeTO.Layout()
            .setLocationRepresentation(
                locationType.getLayout().getLocationRepresentation().name())))
        .sorted(Comparator.comparing(LocationTypeTO::getName))
        .collect(Collectors.toList());
  }
}
