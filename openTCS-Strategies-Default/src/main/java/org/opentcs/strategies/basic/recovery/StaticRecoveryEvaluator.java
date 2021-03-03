/*
 * openTCS copyright information:
 * Copyright (c) 2012 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.strategies.basic.recovery;

import org.opentcs.access.queries.QueryRecoveryStatus;
import org.opentcs.components.kernel.RecoveryEvaluator;

/**
 * A recovery evaluator always returning the same result.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public class StaticRecoveryEvaluator
    implements RecoveryEvaluator {

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
  public QueryRecoveryStatus evaluateRecovery() {
    return new QueryRecoveryStatus(evaluationResult);
  }
}
