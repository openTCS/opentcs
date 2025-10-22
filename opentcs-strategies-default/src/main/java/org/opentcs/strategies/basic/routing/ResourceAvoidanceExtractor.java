// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.strategies.basic.routing;

import static java.util.Objects.requireNonNull;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import jakarta.inject.Inject;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
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
  public ResourcesToAvoid extractResourcesToAvoid(
      @Nullable
      TransportOrder order
  ) {
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
      Optional<Point> point = objectService.fetch(Point.class, resourceToAvoid);
      if (point.isPresent()) {
        pointsToAvoid.add(point.get());
        continue;
      }

      Optional<Path> path = objectService.fetch(Path.class, resourceToAvoid);
      if (path.isPresent()) {
        pathsToAvoid.add(path.get());
        continue;
      }

      Optional<Location> location = objectService.fetch(Location.class, resourceToAvoid);
      if (location.isPresent()) {
        for (Location.Link link : location.get().getAttachedLinks()) {
          pointsToAvoid.add(objectService.fetch(Point.class, link.getPoint()).orElseThrow());
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
      Optional<Point> point = objectService.fetch(Point.class, resourceToAvoid.getName());
      if (point.isPresent()) {
        pointsToAvoid.add(point.get());
        continue;
      }

      Optional<Path> path = objectService.fetch(Path.class, resourceToAvoid.getName());
      if (path.isPresent()) {
        pathsToAvoid.add(path.get());
        continue;
      }

      Optional<Location> location = objectService.fetch(Location.class, resourceToAvoid.getName());
      if (location.isPresent()) {
        for (Location.Link link : location.get().getAttachedLinks()) {
          pointsToAvoid.add(objectService.fetch(Point.class, link.getPoint()).orElseThrow());
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

    /**
     * Transforms the sets of paths and points to avoid to a single set of TCSResourceReferences.
     *
     * @return A set of TCSResourceReferences referencing the points and paths to avoid.
     */
    public Set<TCSResourceReference<?>> toResourceReferenceSet() {
      return Stream.concat(
          paths.stream().map(path -> path.getReference()),
          points.stream().map(point -> point.getReference())
      ).collect(Collectors.toSet());
    }
  }
}
