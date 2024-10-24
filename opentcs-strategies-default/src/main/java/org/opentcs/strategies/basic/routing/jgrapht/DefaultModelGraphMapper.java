// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.strategies.basic.routing.jgrapht;

import jakarta.annotation.Nonnull;
import jakarta.inject.Inject;
import org.opentcs.strategies.basic.routing.edgeevaluator.EdgeEvaluatorComposite;

/**
 * Uses {@link EdgeEvaluatorComposite} to translate a collection of points and paths into a
 * weighted graph.
 */
public class DefaultModelGraphMapper
    extends
      AbstractModelGraphMapper {

  /**
   * Creates a new instance.
   *
   * @param edgeEvaluator Computes the weights of single edges in the graph.
   * @param mapperComponentsFactory A factory for creating mapper-related components.
   */
  @Inject
  public DefaultModelGraphMapper(
      @Nonnull
      EdgeEvaluatorComposite edgeEvaluator,
      @Nonnull
      MapperComponentsFactory mapperComponentsFactory
  ) {
    super(
        mapperComponentsFactory.createPointVertexMapper(),
        mapperComponentsFactory.createPathEdgeMapper(edgeEvaluator, true)
    );
  }
}
