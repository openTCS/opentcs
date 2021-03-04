/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.strategies.basic.routing.jgrapht;

import java.util.Collection;
import org.jgrapht.WeightedGraph;
import org.opentcs.data.model.Path;
import org.opentcs.data.model.Point;
import org.opentcs.data.model.Vehicle;

/**
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public interface ModelGraphMapper {

  WeightedGraph<String, ModelEdge> translateModel(Collection<Point> points,
                                                  Collection<Path> paths,
                                                  Vehicle vehicle);
}
