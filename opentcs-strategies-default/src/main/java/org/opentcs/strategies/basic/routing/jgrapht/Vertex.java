// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.strategies.basic.routing.jgrapht;

import static java.util.Objects.requireNonNull;

import jakarta.annotation.Nonnull;
import org.opentcs.data.TCSObjectReference;
import org.opentcs.data.model.Point;

/**
 * A wrapper for {@link Point}s that can be used to build routing graphs.
 */
public class Vertex {

  /**
   * A reference to the point in the model, that this vertex represents.
   */
  private final TCSObjectReference<Point> point;

  /**
   * Creates a new instance.
   *
   * @param point The reference to the point in the model, this vertex represents.
   */
  public Vertex(
      @Nonnull
      TCSObjectReference<Point> point
  ) {
    this.point = requireNonNull(point, "point");
  }

  /**
   * Returns the reference to the point in the model, that this vertex represents.
   *
   * @return The reference to the point in the model, that this vertex represents.
   */
  public TCSObjectReference<Point> getPoint() {
    return point;
  }

  @Override
  public String toString() {
    return "Vertex{"
        + "point=" + point.getName()
        + '}';
  }
}
