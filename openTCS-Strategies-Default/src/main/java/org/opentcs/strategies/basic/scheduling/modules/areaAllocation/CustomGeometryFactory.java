/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.strategies.basic.scheduling.modules.areaAllocation;

import static java.util.Objects.requireNonNull;
import javax.annotation.Nonnull;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;
import org.opentcs.data.model.Envelope;
import org.opentcs.data.model.Path;

/**
 * A {@link GeometryFactory} extended by custom methods for creating geometries.
 */
public class CustomGeometryFactory
    extends GeometryFactory {

  /**
   * A constant for an empty {@link Geometry}.
   */
  public static final Geometry EMPTY_GEOMETRY = new GeometryFactory().createGeometryCollection();

  /**
   * Creates a new instance.
   */
  public CustomGeometryFactory() {
  }

  /**
   * Creates a {@link Geometry} with the given coordinates.
   * <p>
   * Based on the number of given coordinates, this method returns
   * <ul>
   * <li>an {@link CustomGeometryFactory#EMPTY_GEOMETRY}, for 0, 1, 2 or 3 coordinates, since we
   * need at least 4 coordinates for a valid polygon.</li>
   * <li>a {@link Polygon}, for 4 or more coordinates.</li>
   * </ul>
   * <p>
   * Background: In the context of {@link AreaAllocationModule}, {@link Geometry}s are created for
   * {@link Envelope}s defined at {@link Point}s and {@link Path}s. These envelopes can consist
   * of one or more vertices. This method is supposed to create valid geometries regardless of the
   * number of vertices (which is otherwise not possible using a single method of the JTS library).
   *
   * @param coordinates The coordinates to create a {@link Geometry} with.
   * @return A {@link Geometry}
   */
  public Geometry createPolygonOrEmptyGeometry(@Nonnull Coordinate... coordinates) {
    requireNonNull(coordinates, "coordinates");

    switch (coordinates.length) {
      case 0:
      case 1:
      case 2:
      case 3:
        return EMPTY_GEOMETRY;
      default:
        return this.createPolygon(coordinates);
    }
  }
}
