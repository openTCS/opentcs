// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.strategies.basic.util;

import static java.util.Objects.requireNonNull;

import jakarta.annotation.Nonnull;
import jakarta.inject.Inject;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.geom.util.AffineTransformation;
import org.opentcs.data.model.Envelope;
import org.opentcs.data.model.Path;
import org.opentcs.data.model.Pose;

/**
 * A {@link GeometryFactory} extended by custom methods for creating geometries.
 */
public class CustomGeometryFactory
    extends
      GeometryFactory {

  /**
   * A constant for an empty {@link Geometry}.
   */
  public static final Geometry EMPTY_GEOMETRY = new GeometryFactory().createGeometryCollection();

  /**
   * Creates a new instance.
   */
  @Inject
  public CustomGeometryFactory() {
  }

  /**
   * Creates a (geometric) point from the given (plant model) point.
   *
   * @param point The plant model point.
   * @return A geometric point.
   */
  public Point createPoint(org.opentcs.data.model.Point point) {
    requireNonNull(point, "point");

    return createPoint(
        new Coordinate(
            point.getPose().getPosition().getX(),
            point.getPose().getPosition().getY()
        )
    );
  }

  /**
   * Creates a {@link Geometry} with the vertices of the given envelope.
   *
   * @param envelope The envelope to create a {@code Geometry} from.
   * @return A {@link Geometry} with the vertices of the given envelope.
   */
  public Geometry createPolygonOrEmptyGeometry(Envelope envelope) {
    requireNonNull(envelope, "envelope");

    return createPolygonOrEmptyGeometry(
        envelope.getVertices().stream()
            .map(vertex -> new Coordinate(vertex.getX(), vertex.getY()))
            .toArray(Coordinate[]::new)
    );
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
   * Background: In the context of some scheduler modules, {@link Geometry}s are created for
   * {@link Envelope}s defined at {@link Point}s and {@link Path}s. These envelopes can consist
   * of one or more vertices. This method is supposed to create valid geometries regardless of the
   * number of vertices (which is otherwise not possible using a single method of the JTS library).
   *
   * @param coordinates The coordinates to create a {@link Geometry} with.
   * @return A {@link Geometry}
   */
  public Geometry createPolygonOrEmptyGeometry(
      @Nonnull
      Coordinate... coordinates
  ) {
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

  /**
   * Creates an affine transformation to rotate and translate according to the given pose.
   *
   * @param pose The pose to create a transformation for.
   * @return An affine transformation to rotate and translate according to the given pose.
   */
  public AffineTransformation createTransformationForPose(Pose pose) {
    return new AffineTransformation()
        .rotate(Math.toRadians(pose.getOrientationAngle()))
        .translate(pose.getPosition().getX(), pose.getPosition().getY());
  }
}
