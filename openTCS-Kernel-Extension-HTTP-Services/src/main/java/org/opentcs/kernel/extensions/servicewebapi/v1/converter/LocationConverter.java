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
import java.util.Map;
import static java.util.Objects.requireNonNull;
import java.util.Set;
import java.util.stream.Collectors;
import javax.inject.Inject;
import org.opentcs.access.to.model.LocationCreationTO;
import org.opentcs.data.model.Couple;
import org.opentcs.data.model.Location;
import org.opentcs.data.model.Triple;
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
        .map(location -> new LocationCreationTO(location.getName(),
                                                location.getTypeName(),
                                                new Triple(location.getPosition().getX(),
                                                           location.getPosition().getY(),
                                                           location.getPosition().getZ()))
        .withProperties(pConverter.toPropertyMap(location.getProperties()))
        .withLinks(toLinkMap(location.getLinks()))
        .withLocked(location.isLocked())
        .withLayout(new LocationCreationTO.Layout(
            new Couple(location.getLayout().getPosition().getX(),
                       location.getLayout().getPosition().getY()),
            new Couple(location.getLayout().getLabelOffset().getX(),
                       location.getLayout().getLabelOffset().getY()),
            LocationRepresentation.valueOf(
                location.getLayout().getLocationRepresentation()),
            location.getLayout().getLayerId())))
        .collect(Collectors.toCollection(ArrayList::new));
  }

  public List<LocationTO> toLocationTOs(Set<Location> locations) {
    return locations.stream()
        .map(location -> new LocationTO(location.getName(),
                                        location.getType().getName(),
                                        new TripleTO(location.getPosition().getX(),
                                                     location.getPosition().getY(),
                                                     location.getPosition().getZ()))
        .setLocked(location.isLocked())
        .setProperties(pConverter.toPropertyTOs(location.getProperties()))
        .setLinks(toLinkTOs(location.getAttachedLinks()))
        .setLayout(new LocationTO.Layout()
            .setLayerId(location.getLayout().getLayerId())
            .setLocationRepresentation(
                location.getLayout().getLocationRepresentation().name())
            .setLabelOffset(new CoupleTO(location.getLayout().getLabelOffset().getX(),
                                         location.getLayout().getLabelOffset().getY()))
            .setPosition(new CoupleTO(location.getLayout().getPosition().getX(),
                                      location.getLayout().getPosition().getY()))))
        .sorted(Comparator.comparing(LocationTO::getName))
        .collect(Collectors.toList());
  }

  private Map<String, Set<String>> toLinkMap(List<LinkTO> links) {
    return links.stream()
        .collect(Collectors.toMap(LinkTO::getPointName, LinkTO::getAllowedOperations));
  }

  private List<LinkTO> toLinkTOs(Set<Location.Link> links) {
    return links.stream()
        .map(link -> new LinkTO()
        .setPointName(link.getPoint().getName())
        .setAllowedOperations(link.getAllowedOperations()))
        .collect(Collectors.toList());
  }
}
