// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.strategies.basic.routing.jgrapht;

import static java.util.Objects.requireNonNull;

import jakarta.inject.Inject;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;
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
