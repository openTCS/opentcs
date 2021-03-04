/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.strategies.basic.routing.jgrapht;

import java.util.HashSet;
import java.util.Set;
import org.opentcs.data.model.Vehicle;

/**
 * A {@link EdgeEvaluator} computing costs as the sum of the costs computed by its components.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public class EdgeEvaluatorComposite
    implements EdgeEvaluator {

  /**
   * The components.
   */
  private final Set<EdgeEvaluator> components = new HashSet<>();

  /**
   * Creates a new instance.
   */
  public EdgeEvaluatorComposite() {
  }

  @Override
  public double computeWeight(ModelEdge edge, Vehicle vehicle) {
    double result = 0.0;
    for (EdgeEvaluator component : components) {
      result += component.computeWeight(edge, vehicle);
    }
    return result;
  }

  /**
   * Returns the {@link EdgeEvaluator}s that make up this composite.
   *
   * @return The components.
   */
  public Set<EdgeEvaluator> getComponents() {
    return components;
  }
}
