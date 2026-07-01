// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.strategies.basic.scheduling.modules.areaAllocation;

import static java.util.Objects.requireNonNull;
import static org.opentcs.strategies.basic.util.CustomGeometryFactory.EMPTY_GEOMETRY;

import jakarta.annotation.Nonnull;
import jakarta.inject.Inject;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.locationtech.jts.geom.Geometry;
import org.opentcs.components.kernel.services.InternalTCSObjectService;
import org.opentcs.data.model.Envelope;
import org.opentcs.data.model.Path;
import org.opentcs.data.model.Point;
import org.opentcs.data.model.TCSResource;
import org.opentcs.strategies.basic.util.CustomGeometryFactory;
import org.opentcs.strategies.basic.util.MultiPlaneGeometryCollection;

/**
 * An {@link AreaProvider} implementation that, upon initialization, computes and caches the areas
 * for the {@link Envelope}s defined at all {@link Point}s and {@link Path}s.
 */
public class CachingAreaProvider
    implements
      AreaProvider {

  private final InternalTCSObjectService objectService;
  private final CustomGeometryFactory geometryFactory = new CustomGeometryFactory();
  private final Map<CacheKey, Geometry> cache = new HashMap<>();
  private final Map<String, Point> points = new HashMap<>();
  private boolean initialized;

  /**
   * Creates a new instance.
   *
   * @param objectService The object service to use.
   */
  @Inject
  public CachingAreaProvider(InternalTCSObjectService objectService) {
    this.objectService = requireNonNull(objectService, "objectService");
  }

  @Override
  public void initialize() {
    if (isInitialized()) {
      return;
    }

    objectService.stream(Point.class).forEach(point -> points.put(point.getName(), point));
    populateCache();

    initialized = true;
  }

  @Override
  public boolean isInitialized() {
    return initialized;
  }

  @Override
  public void terminate() {
    if (!isInitialized()) {
      return;
    }

    cache.clear();
    points.clear();

    initialized = false;
  }

  @Override
  public MultiPlaneGeometryCollection getAreas(
      @Nonnull
      String envelopeKey,
      @Nonnull
      Set<TCSResource<?>> resources
  ) {
    requireNonNull(envelopeKey, "envelopeKey");
    requireNonNull(resources, "resources");

    return new MultiPlaneGeometryCollection(
        resources.stream()
            .flatMap(resource -> toPlaneGeometries(envelopeKey, resource).stream())
            .collect(
                Collectors.groupingBy(
                    PlaneGeometry::plane,
                    Collectors.mapping(
                        PlaneGeometry::geometry,
                        Collectors.collectingAndThen(
                            Collectors.toList(),
                            list -> geometryFactory.createGeometryCollection(
                                list.toArray(Geometry[]::new)
                            )
                        )
                    )
                )
            )
    );
  }

  private void populateCache() {
    Set<Point> points = objectService.fetch(
        Point.class,
        point -> !point.getVehicleEnvelopes().isEmpty()
    );
    for (Point point : points) {
      for (Map.Entry<String, Envelope> entry : point.getVehicleEnvelopes().entrySet()) {
        String envelopeKey = entry.getKey();
        computeArea(envelopeKey, point)
            .ifPresent(geometry -> cache.put(new CacheKey(envelopeKey, point), geometry));
      }
    }

    Set<Path> paths = objectService.fetch(
        Path.class,
        path -> !path.getVehicleEnvelopes().isEmpty()
    );
    for (Path path : paths) {
      for (Map.Entry<String, Envelope> entry : path.getVehicleEnvelopes().entrySet()) {
        String envelopeKey = entry.getKey();
        computeArea(envelopeKey, path)
            .ifPresent(geometry -> cache.put(new CacheKey(envelopeKey, path), geometry));
      }
    }
  }

  private Optional<Geometry> computeArea(String envelopeKey, TCSResource<?> resource) {
    Map<String, Envelope> vehicleEnvelopes = extractVehicleEnvelopes(resource);

    if (!vehicleEnvelopes.containsKey(envelopeKey)) {
      return Optional.empty();
    }

    return Optional.of(
        geometryFactory.createPolygonOrEmptyGeometry(vehicleEnvelopes.get(envelopeKey))
    );
  }

  private Map<String, Envelope> extractVehicleEnvelopes(TCSResource<?> resource) {
    if (resource instanceof Point) {
      return ((Point) resource).getVehicleEnvelopes();
    }
    else if (resource instanceof Path) {
      return ((Path) resource).getVehicleEnvelopes();
    }
    else {
      return Map.of();
    }
  }

  private Set<PlaneGeometry> toPlaneGeometries(String envelopeKey, TCSResource<?> resource) {
    if (resource instanceof Point point) {
      Geometry area = lookupArea(envelopeKey, point);
      if (area == EMPTY_GEOMETRY) {
        return Set.of();
      }

      return Set.of(new PlaneGeometry(point.getPose().getPosition().getZ(), area));
    }
    else if (resource instanceof Path path) {
      Geometry area = lookupArea(envelopeKey, path);
      if (area == EMPTY_GEOMETRY) {
        return Set.of();
      }

      Point srcPoint = points.get(path.getSourcePoint().getName());
      Point destPoint = points.get(path.getDestinationPoint().getName());
      return srcPoint.getPose().getPosition().getZ() == destPoint.getPose().getPosition().getZ()
          ? Set.of(new PlaneGeometry(srcPoint.getPose().getPosition().getZ(), area))
          // If the path's source and destination point are on different planes, add a geometry for
          // each of them to allow for intersection detection on both planes.
          : Set.of(
              new PlaneGeometry(srcPoint.getPose().getPosition().getZ(), area),
              new PlaneGeometry(destPoint.getPose().getPosition().getZ(), area)
          );
    }
    else {
      return Set.of();
    }
  }

  private Geometry lookupArea(String envelopeKey, TCSResource<?> resource) {
    return cache.getOrDefault(new CacheKey(envelopeKey, resource), EMPTY_GEOMETRY);
  }

  /**
   * Combines the envelope key and the resource (for which there is a corresponding area) to be used
   * as the cache's key.
   */
  private static class CacheKey {

    private final String envelopeKey;
    private final TCSResource<?> resource;

    /**
     * Creates a new instance.
     *
     * @param envelopeKey The envelope key.
     * @param resource The resource.
     */
    CacheKey(String envelopeKey, TCSResource<?> resource) {
      this.envelopeKey = requireNonNull(envelopeKey, "envelopeKey");
      this.resource = requireNonNull(resource, "resource");
    }

    @Override
    public int hashCode() {
      int hash = 7;
      hash = 37 * hash + Objects.hashCode(this.envelopeKey);
      hash = 37 * hash + Objects.hashCode(this.resource);
      return hash;
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj) {
        return true;
      }
      if (!(obj instanceof CacheKey)) {
        return false;
      }

      CacheKey other = (CacheKey) obj;
      return Objects.equals(this.envelopeKey, other.envelopeKey)
          && Objects.equals(this.resource, other.resource);
    }
  }

  private record PlaneGeometry(long plane, Geometry geometry) {}
}
