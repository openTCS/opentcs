/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.strategies.basic.routing.jgrapht;

import java.util.Collection;
import static java.util.Objects.requireNonNull;
import java.util.Set;
import java.util.stream.Collectors;
import javax.inject.Inject;
import org.opentcs.data.model.Point;

/**
 * Mapper to translate a collection of {@link Point}s to names of vertices.
 */
public class PointVertexMapper {

  /**
   * Creates a new instance.
   */
  @Inject
  public PointVertexMapper() {
  }

  /**
   * Translates the given {@link Point}s to names of vertices.
   *
   * @param points The points to translate to names of vertices.
   * @return The translated names of vertices.
   */
  public Set<String> translatePoints(Collection<Point> points) {
    requireNonNull(points, "points");

    return points.stream()
        .map(Point::getName)
        .collect(Collectors.toSet());
  }
}
