// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.kernel.extensions.servicewebapi.v8.binding.request.converter;

import static java.util.Objects.requireNonNull;

import jakarta.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.opentcs.access.to.model.CoupleCreationTO;
import org.opentcs.access.to.model.LocationCreationTO;
import org.opentcs.access.to.model.LocationRepresentationTO;
import org.opentcs.access.to.model.TripleCreationTO;
import org.opentcs.kernel.extensions.servicewebapi.v8.binding.request.data.LocationTO;
import org.opentcs.kernel.extensions.servicewebapi.v8.binding.request.data.shared.LinkTO;

/**
 * Includes the conversion methods for all Location classes.
 */
public class LocationConverter {

  private final PropertyConverter pConverter;

  @Inject
  public LocationConverter(PropertyConverter pConverter) {
    this.pConverter = requireNonNull(pConverter, "pConverter");
  }

  public List<LocationCreationTO> toLocationCreationTOs(List<LocationTO> locations) {
    return locations.stream()
        .map(
            location -> new LocationCreationTO(
                location.getName(),
                location.getTypeName(),
                new TripleCreationTO(
                    location.getPosition().getX(),
                    location.getPosition().getY(),
                    location.getPosition().getZ()
                )
            )
                .withProperties(pConverter.toPropertyMap(location.getProperties()))
                .withLinks(toLinkMap(location.getLinks()))
                .withLocked(location.isLocked())
                .withLayout(
                    new LocationCreationTO.Layout(
                        new CoupleCreationTO(
                            location.getLayout().getLabelOffset().getX(),
                            location.getLayout().getLabelOffset().getY()
                        ),
                        convertToLocationRepresentation(
                            location.getLayout().getLocationRepresentation()
                        ),
                        location.getLayout().getLayerId()
                    )
                )
        )
        .collect(Collectors.toCollection(ArrayList::new));
  }

  private Map<String, Set<String>> toLinkMap(List<LinkTO> links) {
    return links.stream()
        .collect(Collectors.toMap(LinkTO::getPointName, LinkTO::getAllowedOperations));
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
