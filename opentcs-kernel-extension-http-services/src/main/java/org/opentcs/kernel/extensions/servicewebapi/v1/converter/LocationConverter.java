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
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.opentcs.access.to.model.CoupleCreationTO;
import org.opentcs.access.to.model.LocationCreationTO;
import org.opentcs.access.to.model.LocationRepresentationTO;
import org.opentcs.access.to.model.TripleCreationTO;
import org.opentcs.data.model.Location;
import org.opentcs.data.model.visualization.LocationRepresentation;
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
