/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.strategies.basic.routing.jgrapht;

import javax.annotation.Nonnull;
import org.opentcs.components.kernel.routing.EdgeEvaluator;

/**
 * A factory for creating mapper-related components.
 */
public interface MapperComponentsFactory {

  /**
   * Creates a {@link PointVertexMapper}.
   *
   * @return A {@link PointVertexMapper}.
   */
  PointVertexMapper createPointVertexMapper();

  /**
   * Creates a {@link PathEdgeMapper}.
   *
   * @param edgeEvaluator Computes the weight of single edges.
   * @param excludeLockedPaths Whether locked paths should be excluded from mapping.
   * @return A {@link PathEdgeMapper}.
   */
  PathEdgeMapper createPathEdgeMapper(@Nonnull EdgeEvaluator edgeEvaluator,
                                      boolean excludeLockedPaths);
}
