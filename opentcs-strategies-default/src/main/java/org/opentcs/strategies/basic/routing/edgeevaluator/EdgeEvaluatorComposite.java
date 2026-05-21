// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.strategies.basic.routing.edgeevaluator;

import static java.util.Objects.requireNonNull;
import static org.opentcs.util.Assertions.checkArgument;

import jakarta.annotation.Nonnull;
import jakarta.inject.Inject;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.opentcs.components.kernel.routing.Edge;
import org.opentcs.components.kernel.routing.EdgeEvaluator;
import org.opentcs.components.kernel.routing.RoutingContext;
import org.opentcs.data.model.Vehicle;
import org.opentcs.strategies.basic.routing.jgrapht.ShortestPathConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A {@link EdgeEvaluator} computing costs as the sum of the costs computed by all configured
 * evaluators.
 */
public class EdgeEvaluatorComposite
    implements
      EdgeEvaluator {

  /**
   * This class's logger.
   */
  private static final Logger LOG = LoggerFactory.getLogger(EdgeEvaluatorComposite.class);
  /**
   * The evaluators.
   */
  private final Set<EdgeEvaluator> evaluators = new HashSet<>();
  /**
   * Indicates whether parallel graph computation is supported.
   */
  private final boolean parallelGraphComputationSupported;

  /**
   * Creates a new instance.
   *
   * @param configuration The configuration to use.
   * @param availableEvaluators The configured evaluators to use.
   */
  @Inject
  public EdgeEvaluatorComposite(
      ShortestPathConfiguration configuration,
      Map<String, EdgeEvaluator> availableEvaluators
  ) {
    if (availableEvaluators.isEmpty()) {
      LOG.warn("No edge evaluator enabled, falling back to distance-based evaluation.");
      evaluators.add(new EdgeEvaluatorDistance());
    }
    else {
      for (String evaluatorKey : configuration.edgeEvaluators()) {
        checkArgument(
            availableEvaluators.containsKey(evaluatorKey),
            "Unknown edge evaluator key: %s",
            evaluatorKey
        );
        evaluators.add(availableEvaluators.get(evaluatorKey));
      }
    }
    List<String> nonParallelEvaluatorNames = evaluators.stream()
        .filter(evaluator -> !evaluator.isParallelGraphComputationSupported())
        .map(evaluator -> evaluator.getClass().getName())
        .toList();
    this.parallelGraphComputationSupported = nonParallelEvaluatorNames.isEmpty();
    LOG.info("Parallel graph computation supported: {}", parallelGraphComputationSupported);
    if (!parallelGraphComputationSupported) {
      LOG.info("Parallel graph computation not supported by: {}", nonParallelEvaluatorNames);
    }
  }

  @Override
  public boolean isParallelGraphComputationSupported() {
    return parallelGraphComputationSupported;
  }

  @Override
  public void onRoutingContextUpdated(
      @Nonnull
      RoutingContext context
  ) {
    requireNonNull(context, "context");

    for (EdgeEvaluator component : evaluators) {
      long timeStampBefore = System.currentTimeMillis();
      component.onRoutingContextUpdated(context);
      LOG.debug(
          "Updated routing context in {} milliseconds for edge evaluator {}.",
          System.currentTimeMillis() - timeStampBefore,
          component
      );
    }
  }

  @Override
  public void onGraphComputationStart(Vehicle vehicle) {
    for (EdgeEvaluator component : evaluators) {
      component.onGraphComputationStart(vehicle);
    }
  }

  @Override
  public void onGraphComputationEnd(Vehicle vehicle) {
    for (EdgeEvaluator component : evaluators) {
      component.onGraphComputationEnd(vehicle);
    }
  }

  @Override
  public double computeWeight(Edge edge, Vehicle vehicle) {
    double result = 0.0;
    for (EdgeEvaluator component : evaluators) {
      result += component.computeWeight(edge, vehicle);
    }
    return result;
  }
}
