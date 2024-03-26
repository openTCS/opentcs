/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.strategies.basic.routing.jgrapht;

import java.util.Collection;
import org.jgrapht.Graph;
import org.opentcs.components.kernel.routing.Edge;
import org.opentcs.data.model.Path;
import org.opentcs.data.model.Point;
import org.opentcs.data.model.Vehicle;

/**
 * Translates model data to weighted graphs.
 */
public interface ModelGraphMapper {

  /**
   * Translates the given points and paths to a weighted graph.
   *
   * @param points The points of the model.
   * @param paths The paths of the model.
   * @param vehicle The vehicle for which to build the graph.
   * @return A weighted graph representing the topology to be used for the given vehicle.
   */
  Graph<String, Edge> translateModel(Collection<Point> points,
                                     Collection<Path> paths,
                                     Vehicle vehicle);

  /**
   * Re-translates the given paths and replaces corresponding edges in a copy of the provided graph.
   * <p>
   * If a path cannnot be translated to an edge but the provided graph contained an edge for that
   * path, the edge will <em>not</em> be contained in the returned graph copy.
   * </p>
   *
   * @param paths The paths to re-translate.
   * @param vehicle The vehicle for which to update the graph.
   * @param graph The graph to whose copy the re-translated paths are to be added.
   * @return A copy of the provided graph including the re-translated paths.
   */
  Graph<String, Edge> updateGraph(Collection<Path> paths,
                                  Vehicle vehicle,
                                  Graph<String, Edge> graph);
}
