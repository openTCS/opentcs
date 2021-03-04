/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.strategies.basic.recovery;

import org.opentcs.util.annotations.ScheduledApiChange;

/**
 * A recovery evaluator always returning the same result.
 *
 * @author Stefan Walter (Fraunhofer IML)
 * @deprecated The definition of <em>recovered</em> is unclear. Unless it is clearly specified,
 * evaluation of a state of recovery should not be part of the API.
 */
@Deprecated
@ScheduledApiChange(when = "5.0")
public class StaticRecoveryEvaluator
    implements org.opentcs.components.kernel.RecoveryEvaluator {

  /**
   * The evaluation result to be returned.
   */
  private final boolean evaluationResult;
  /**
   * Indicates whether this component is enabled.
   */
  private boolean initialized;

  /**
   * Creates a new instance.
   *
   * @param evaluationResult The evaluation result to be returned.
   */
  public StaticRecoveryEvaluator(boolean evaluationResult) {
    this.evaluationResult = evaluationResult;
  }

  @Override
  public void initialize() {
    initialized = true;
  }

  @Override
  public boolean isInitialized() {
    return initialized;
  }

  @Override
  public void terminate() {
    initialized = false;
  }

  @Override
  public org.opentcs.access.queries.QueryRecoveryStatus evaluateRecovery() {
    return new org.opentcs.access.queries.QueryRecoveryStatus(evaluationResult);
  }
}
