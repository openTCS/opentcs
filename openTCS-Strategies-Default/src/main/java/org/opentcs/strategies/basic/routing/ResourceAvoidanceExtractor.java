/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.strategies.basic.routing;

import java.util.HashSet;
import static java.util.Objects.requireNonNull;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;
import org.opentcs.components.kernel.services.TCSObjectService;
import org.opentcs.data.ObjectPropConstants;
import org.opentcs.data.model.Location;
import org.opentcs.data.model.Path;
import org.opentcs.data.model.Point;
import org.opentcs.data.order.TransportOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides methods for extracting {@link Point}s, {@link Path}s and {@link Location}s to be
 * avoided by vehicles processing a {@link TransportOrder} where the
 * {@link ObjectPropConstants#TRANSPORT_ORDER_RESOURCES_TO_AVOID} property is set.
 */
public class ResourceAvoidanceExtractor {

  private static final Logger LOG = LoggerFactory.getLogger(ResourceAvoidanceExtractor.class);
  private final TCSObjectService objectService;

  /**
   * Creates a new instance.
   *
   * @param objectService The objects service to be used.
   */
  @Inject
  public ResourceAvoidanceExtractor(TCSObjectService objectService) {
    this.objectService = requireNonNull(objectService, "objectService");
  }

  /**
   * Extracts the {@link Point}s that are explicitly or implicitly referenced in the
   * {@link ObjectPropConstants#TRANSPORT_ORDER_RESOURCES_TO_AVOID} property of the given
   * {@link TransportOrder}.
   * <p>
   * Explicit references are points whose name contained in the property's value. Implicit
   * references are points that are source or destination of a path OR are linked to a location,
   * whereby the name of the path/location is contained in the property's value.
   * </p>
   *
   * @param order The transport order.
   * @return The extract set of points.
   */
  @Nonnull
  public Set<Point> extractPointsToAvoid(@Nullable TransportOrder order) {
    if (order == null) {
      return Set.of();
    }

    String resourcesToAvoidString
        = order.getProperty(ObjectPropConstants.TRANSPORT_ORDER_RESOURCES_TO_AVOID);
    if (resourcesToAvoidString == null) {
      return Set.of();
    }

    Set<Point> pointsToAvoid = new HashSet<>();
    Set<String> resourcesToAvoidByName = Set.of(resourcesToAvoidString.split(","));
    for (String resourceToAvoid : resourcesToAvoidByName) {
      Point point = objectService.fetchObject(Point.class, resourceToAvoid);
      if (point != null) {
        pointsToAvoid.add(point);
        continue;
      }

      Path path = objectService.fetchObject(Path.class, resourceToAvoid);
      if (path != null) {
        pointsToAvoid.add(objectService.fetchObject(Point.class, path.getSourcePoint()));
        pointsToAvoid.add(objectService.fetchObject(Point.class, path.getDestinationPoint()));
        continue;
      }

      Location location = objectService.fetchObject(Location.class, resourceToAvoid);
      if (location != null) {
        for (Location.Link link : location.getAttachedLinks()) {
          pointsToAvoid.add(objectService.fetchObject(Point.class, link.getPoint()));
        }
        continue;
      }

      LOG.debug("Ignoring resource '{}' which is not a point, path or location.", resourceToAvoid);
    }

    return pointsToAvoid;
  }
}
