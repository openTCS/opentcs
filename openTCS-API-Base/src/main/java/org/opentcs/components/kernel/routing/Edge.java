/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.components.kernel.routing;

import static java.util.Objects.requireNonNull;
import javax.annotation.Nonnull;
import org.opentcs.data.model.Path;

/**
 * A wrapper for {@link Path}s that can be used to build routing graphs.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public class Edge {

  /**
   * The path in the model that is traversed on this edge.
   */
  private final Path path;
  /**
   * Whether the path is travelled in reverse direction.
   */
  private final boolean travellingReverse;

  /**
   * Creates a new instance.
   *
   * @param modelPath The path in the model that is traversed on this edge.
   * @param travellingReverse Whether the path is travelled in reverse direction.
   */
  public Edge(@Nonnull Path modelPath, boolean travellingReverse) {
    this.path = requireNonNull(modelPath, "modelPath");
    this.travellingReverse = travellingReverse;
  }

  /**
   * Returns the path in the model that is traversed on this edge.
   *
   * @return The path in the model that is traversed on this edge.
   */
  public Path getPath() {
    return path;
  }

  /**
   * Indicates whether the path is travelled in reverse direction.
   *
   * @return Whether the path is travelled in reverse direction.
   */
  public boolean isTravellingReverse() {
    return travellingReverse;
  }

  @Override
  public String toString() {
    return "Edge{"
        + "path=" + path + ", "
        + "travellingReverse=" + travellingReverse
        + '}';
  }
}
