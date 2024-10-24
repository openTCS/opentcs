// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.strategies.basic.routing.jgrapht;

import jakarta.annotation.Nonnull;
import jakarta.inject.Inject;
import org.opentcs.strategies.basic.routing.edgeevaluator.EdgeEvaluatorHops;

/**
 * Uses {@link EdgeEvaluatorHops} to translate a collection of points and paths into a
 * weighted graph.
 */
public class GeneralModelGraphMapper
    extends
      AbstractModelGraphMapper {

  /**
   * Creates a new instance.
   *
   * @param edgeEvaluatorHops Computes the weights of single edges in the graph.
   * @param mapperComponentsFactory A factory for creating mapper-related components.
   */
  @Inject
  public GeneralModelGraphMapper(
      @Nonnull
      EdgeEvaluatorHops edgeEvaluatorHops,
      @Nonnull
      MapperComponentsFactory mapperComponentsFactory
  ) {
    super(
        mapperComponentsFactory.createPointVertexMapper(),
        mapperComponentsFactory.createPathEdgeMapper(edgeEvaluatorHops, false)
    );
  }
}
