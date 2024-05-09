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
import org.opentcs.data.model.TCSResourceReference;
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
   * Extracts resources that are referenced in the
   * {@link ObjectPropConstants#TRANSPORT_ORDER_RESOURCES_TO_AVOID} property of the given
   * {@link TransportOrder}.
   * <p>
   * The extraction result will contain {@link Point}s and {@link Path}s that are referenced by
   * their name in the property's value and also points that are linked to a {@link Location},
   * whereby the name of the location is referenced in the property's value.
   * </p>
   *
   * @param order The transport order.
   * @return The extracted resources.
   */
  @Nonnull
  public ResourcesToAvoid extractResourcesToAvoid(@Nullable TransportOrder order) {
    if (order == null) {
      return ResourcesToAvoid.EMPTY;
    }

    String resourcesToAvoidString
        = order.getProperty(ObjectPropConstants.TRANSPORT_ORDER_RESOURCES_TO_AVOID);
    if (resourcesToAvoidString == null) {
      return ResourcesToAvoid.EMPTY;
    }

    Set<Point> pointsToAvoid = new HashSet<>();
    Set<Path> pathsToAvoid = new HashSet<>();
    Set<String> resourcesToAvoidByName = Set.of(resourcesToAvoidString.split(","));
    for (String resourceToAvoid : resourcesToAvoidByName) {
      Point point = objectService.fetchObject(Point.class, resourceToAvoid);
      if (point != null) {
        pointsToAvoid.add(point);
        continue;
      }

      Path path = objectService.fetchObject(Path.class, resourceToAvoid);
      if (path != null) {
        pathsToAvoid.add(path);
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

    return new ResourcesToAvoid(pointsToAvoid, pathsToAvoid);
  }

  /**
   * Extracts resources in the given set of references.
   * <p>
   * The extraction result will contain {@link Point}s and {@link Path}s referenced in the given
   * set, and also points that are linked to {@link Location}s referenced in the given set.
   * </p>
   *
   * @param resourcesToAvoid The set of references.
   * @return The extracted resources.
   */
  @Nonnull
  public ResourcesToAvoid extractResourcesToAvoid(Set<TCSResourceReference<?>> resourcesToAvoid) {
    requireNonNull(resourcesToAvoid, "resourcesToAvoid");

    if (resourcesToAvoid.isEmpty()) {
      return ResourcesToAvoid.EMPTY;
    }

    Set<Point> pointsToAvoid = new HashSet<>();
    Set<Path> pathsToAvoid = new HashSet<>();

    for (TCSResourceReference<?> resourceToAvoid : resourcesToAvoid) {
      Point point = objectService.fetchObject(Point.class, resourceToAvoid.getName());
      if (point != null) {
        pointsToAvoid.add(point);
        continue;
      }

      Path path = objectService.fetchObject(Path.class, resourceToAvoid.getName());
      if (path != null) {
        pathsToAvoid.add(path);
        continue;
      }

      Location location = objectService.fetchObject(Location.class, resourceToAvoid.getName());
      if (location != null) {
        for (Location.Link link : location.getAttachedLinks()) {
          pointsToAvoid.add(objectService.fetchObject(Point.class, link.getPoint()));
        }
        continue;
      }

      LOG.debug("Ignoring resource '{}' which is not a point, path or location.", resourceToAvoid);
    }

    return new ResourcesToAvoid(pointsToAvoid, pathsToAvoid);
  }

  /**
   * A wrapper for resources to be avoided.
   */
  public static class ResourcesToAvoid {

    /**
     * An instance representing no resources to be avoided.
     */
    public static final ResourcesToAvoid EMPTY = new ResourcesToAvoid(Set.of(), Set.of());
    private final Set<Point> points;
    private final Set<Path> paths;

    /**
     * Creates a new instance.
     *
     * @param points The set of points to be avoided.
     * @param paths The set of paths to be avoided.
     */
    private ResourcesToAvoid(Set<Point> points, Set<Path> paths) {
      this.points = requireNonNull(points, "points");
      this.paths = requireNonNull(paths, "paths");
    }

    /**
     * Returns the set of points to be avoided.
     *
     * @return The set of points to be avoided.
     */
    public Set<Point> getPoints() {
      return points;
    }

    /**
     * Returns the set of paths to avoid.
     *
     * @return The set of paths to avoid.
     */
    public Set<Path> getPaths() {
      return paths;
    }

    /**
     * Checks whether there are any resources to be avoided.
     *
     * @return {@code true}, if there are any resources to be avoided, otherwise {@code false}.
     */
    public boolean isEmpty() {
      return points.isEmpty() && paths.isEmpty();
    }
  }
}
