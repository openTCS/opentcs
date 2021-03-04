/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.strategies.basic.routing.jgrapht;

import static java.util.Objects.requireNonNull;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.opentcs.data.model.Path;

/**
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public class ModelEdge
    extends DefaultWeightedEdge {

  private final Path modelPath;

  private final boolean travellingReverse;

  public ModelEdge(Path modelPath, boolean travellingReverse) {
    this.modelPath = requireNonNull(modelPath, "modelPath");
    this.travellingReverse = travellingReverse;
  }

  public Path getModelPath() {
    return modelPath;
  }

  public boolean isTravellingReverse() {
    return travellingReverse;
  }

  @Override
  public String toString() {
    return "ModelEdge{"
        + "modelPath=" + modelPath
        + ", travellingReverse=" + travellingReverse
        + '}';
  }

}
