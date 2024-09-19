/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
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
