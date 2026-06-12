// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.strategies.basic.scheduling.modules.areaAllocation;

import static java.util.Objects.requireNonNull;

import java.util.Map;
import java.util.Optional;
import org.locationtech.jts.geom.GeometryCollection;

/**
 * A collection of {@link GeometryCollection}s across multiple planes.
 */
public class MultiPlaneGeometryCollection {

  private final Map<Long, GeometryCollection> collectionsByPlane;

  /**
   * Creates a new instance.
   *
   * @param collectionsByPlane A map of planes mapped to the {@link GeometryCollection} that belongs
   * to them.
   */
  public MultiPlaneGeometryCollection(Map<Long, GeometryCollection> collectionsByPlane) {
    this.collectionsByPlane = requireNonNull(collectionsByPlane, "collectionsByPlane");
  }

  /**
   * Returns {@code true}, if this collection contains a {@link GeometryCollection} for the
   * given plane.
   *
   * @param plane The plane.
   * @return {@code true}, if this collection contains a {@link GeometryCollection} for the
   * given plane.
   */
  public boolean containsCollection(long plane) {
    return collectionsByPlane.containsKey(plane);
  }

  /**
   * Returns the {@link GeometryCollection} for the given plane.
   *
   * @param plane The plane.
   * @return The {@link GeometryCollection} for the given plane, or an empty {@link Optional} if
   * there is no {@link GeometryCollection}.
   */
  public Optional<GeometryCollection> get(long plane) {
    return Optional.ofNullable(collectionsByPlane.get(plane));
  }

  /**
   * Returns {@code true}, if this collection is empty.
   *
   * @return {@code true}, if this collection is empty.
   */
  public boolean isEmpty() {
    return collectionsByPlane.isEmpty();
  }

  /**
   * Checks whether the {@link GeometryCollection}s in this collection intersect with the ones in
   * the given {@link MultiPlaneGeometryCollection}.
   *
   * @param other The {@link MultiPlaneGeometryCollection} to check for intersections.
   * @return {@code true}, if any two {@link GeometryCollection}s on the same plane intersect.
   */
  public boolean intersects(MultiPlaneGeometryCollection other) {
    return collectionsByPlane.entrySet().stream()
        .filter(entry -> other.containsCollection(entry.getKey()))
        .anyMatch(
            entry -> entry.getValue().intersects(
                other.get(entry.getKey()).orElseThrow()
            )
        );
  }
}
