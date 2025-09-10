// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.kernel.extensions.servicewebapi.v1.converter;

import static java.util.Objects.requireNonNull;

import jakarta.inject.Inject;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.opentcs.access.to.model.LocationCreationTO;
import org.opentcs.data.model.Couple;
import org.opentcs.data.model.Location;
import org.opentcs.data.model.Triple;
import org.opentcs.data.model.visualization.LocationRepresentation;
import org.opentcs.kernel.extensions.servicewebapi.v1.binding.plantmodel.LocationRepresentationTO;
import org.opentcs.kernel.extensions.servicewebapi.v1.binding.plantmodel.LocationTO;
import org.opentcs.kernel.extensions.servicewebapi.v1.binding.shared.CoupleTO;
import org.opentcs.kernel.extensions.servicewebapi.v1.binding.shared.LinkTO;
import org.opentcs.kernel.extensions.servicewebapi.v1.binding.shared.TripleTO;

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
                new Triple(
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
                        new Couple(
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

  public List<LocationTO> toLocationTOs(Set<Location> locations) {
    return locations.stream()
        .map(
            location -> new LocationTO(
                location.getName(),
                location.getType().getName(),
                new TripleTO(
                    location.getPosition().getX(),
                    location.getPosition().getY(),
                    location.getPosition().getZ()
                )
            )
                .setLocked(location.isLocked())
                .setProperties(pConverter.toPropertyTOs(location.getProperties()))
                .setLinks(toLinkTOs(location.getAttachedLinks()))
                .setLayout(
                    new LocationTO.Layout()
                        .setLayerId(location.getLayout().getLayerId())
                        .setLocationRepresentation(
                            convertToLocationRepresentationTO(
                                location.getLayout().getLocationRepresentation()
                            )
                        )
                        .setLabelOffset(
                            new CoupleTO(
                                location.getLayout().getLabelOffset().getX(),
                                location.getLayout().getLabelOffset().getY()
                            )
                        )
                        .setPosition(
                            new CoupleTO(
                                location.getPosition().getX(),
                                location.getPosition().getY()
                            )
                        )
                )
        )
        .sorted(Comparator.comparing(LocationTO::getName))
        .collect(Collectors.toList());
  }

  private Map<String, Set<String>> toLinkMap(List<LinkTO> links) {
    return links.stream()
        .collect(Collectors.toMap(LinkTO::getPointName, LinkTO::getAllowedOperations));
  }

  private List<LinkTO> toLinkTOs(Set<Location.Link> links) {
    return links.stream()
        .map(
            link -> new LinkTO()
                .setPointName(link.getPoint().getName())
                .setAllowedOperations(link.getAllowedOperations())
        )
        .collect(Collectors.toList());
  }

  private LocationRepresentation convertToLocationRepresentation(
      LocationRepresentationTO locationRepresentation
  ) {
    return switch (locationRepresentation) {
      case DEFAULT -> LocationRepresentation.DEFAULT;
      case LOAD_TRANSFER_ALT_1 -> LocationRepresentation.LOAD_TRANSFER_ALT_1;
      case LOAD_TRANSFER_ALT_2 -> LocationRepresentation.LOAD_TRANSFER_ALT_2;
      case LOAD_TRANSFER_ALT_3 -> LocationRepresentation.LOAD_TRANSFER_ALT_3;
      case LOAD_TRANSFER_ALT_4 -> LocationRepresentation.LOAD_TRANSFER_ALT_4;
      case LOAD_TRANSFER_ALT_5 -> LocationRepresentation.LOAD_TRANSFER_ALT_5;
      case LOAD_TRANSFER_GENERIC -> LocationRepresentation.LOAD_TRANSFER_GENERIC;
      case NONE -> LocationRepresentation.NONE;
      case RECHARGE_ALT_1 -> LocationRepresentation.RECHARGE_ALT_1;
      case RECHARGE_ALT_2 -> LocationRepresentation.RECHARGE_ALT_2;
      case RECHARGE_GENERIC -> LocationRepresentation.RECHARGE_GENERIC;
      case WORKING_ALT_1 -> LocationRepresentation.WORKING_ALT_1;
      case WORKING_ALT_2 -> LocationRepresentation.WORKING_ALT_2;
      case WORKING_GENERIC -> LocationRepresentation.WORKING_GENERIC;
    };
  }

  private LocationRepresentationTO convertToLocationRepresentationTO(
      LocationRepresentation locationRepresentation
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
