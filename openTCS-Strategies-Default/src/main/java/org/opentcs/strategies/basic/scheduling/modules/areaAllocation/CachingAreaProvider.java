/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.strategies.basic.scheduling.modules.areaAllocation;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import static java.util.Objects.requireNonNull;
import java.util.Optional;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.inject.Inject;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryCollection;
import org.opentcs.components.kernel.services.TCSObjectService;
import org.opentcs.data.model.Envelope;
import org.opentcs.data.model.Path;
import org.opentcs.data.model.Point;
import org.opentcs.data.model.TCSResource;
import static org.opentcs.strategies.basic.scheduling.modules.areaAllocation.CustomGeometryFactory.EMPTY_GEOMETRY;

/**
 * An {@link AreaProvider} implementation that, upon initialization, computes and caches the areas
 * for the {@link Envelope}s defined at all {@link Point}s and {@link Path}s.
 */
public class CachingAreaProvider
    implements AreaProvider {

  private final TCSObjectService objectService;
  private final CustomGeometryFactory geometryFactory = new CustomGeometryFactory();
  private final Map<CacheKey, Geometry> cache = new HashMap<>();
  private boolean initialized;

  /**
   * Creates a new instance.
   *
   * @param objectService The object service to use.
   */
  @Inject
  public CachingAreaProvider(TCSObjectService objectService) {
    this.objectService = requireNonNull(objectService, "objectService");
  }

  @Override
  public void initialize() {
    if (isInitialized()) {
      return;
    }

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

    initialized = false;
  }

  @Override
  public GeometryCollection getAreas(@Nonnull String envelopeKey,
                                     @Nonnull Set<TCSResource<?>> resources) {
    requireNonNull(envelopeKey, "envelopeKey");
    requireNonNull(resources, "resources");

    Geometry[] computedAreas = resources.stream()
        .map(resource -> lookupArea(envelopeKey, resource))
        .filter(geometry -> geometry != EMPTY_GEOMETRY)
        .toArray(Geometry[]::new);

    return geometryFactory.createGeometryCollection(computedAreas);
  }

  private void populateCache() {
    Set<Point> points = objectService.fetchObjects(Point.class,
                                                   point -> !point.getVehicleEnvelopes().isEmpty());
    for (Point point : points) {
      for (Map.Entry<String, Envelope> entry : point.getVehicleEnvelopes().entrySet()) {
        String envelopeKey = entry.getKey();
        computeArea(envelopeKey, point)
            .ifPresent(geometry -> cache.put(new CacheKey(envelopeKey, point), geometry));
      }
    }

    Set<Path> paths = objectService.fetchObjects(Path.class,
                                                 path -> !path.getVehicleEnvelopes().isEmpty());
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

    Envelope envelope = vehicleEnvelopes.get(envelopeKey);
    Coordinate[] coordinates = envelope.getVertices().stream()
        .map(vertex -> new Coordinate(vertex.getX(), vertex.getY()))
        .toArray(Coordinate[]::new);

    return Optional.of(geometryFactory.createPolygonOrEmptyGeometry(coordinates));
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
}
