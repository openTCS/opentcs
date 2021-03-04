/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.strategies.basic.routing.jgrapht;

import org.opentcs.data.model.Vehicle;

/**
 * Computes the weight of edges.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public interface EdgeEvaluator {

  double computeWeight(ModelEdge edge, Vehicle vehicle);
}
