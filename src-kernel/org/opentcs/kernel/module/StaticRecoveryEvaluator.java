/*
 * openTCS copyright information:
 * Copyright (c) 2012 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.kernel.module;

import org.opentcs.access.queries.QueryRecoveryStatus;
import org.opentcs.algorithms.RecoveryEvaluator;

/**
 * A recovery evaluator always returning the same result. *
 * @author Stefan Walter (Fraunhofer IML)
 */
public class StaticRecoveryEvaluator
    implements RecoveryEvaluator {

  /**
   * The evaluation result to be returned.
   */
  private final boolean evaluationResult;

  /**
   * Creates a new instance.
   *
   * @param evaluationResult The evaluation result to be returned.
   */
  public StaticRecoveryEvaluator(boolean evaluationResult) {
    this.evaluationResult = evaluationResult;
  }

  @Override
  public QueryRecoveryStatus evaluateRecovery() {
    return new QueryRecoveryStatus(evaluationResult);
  }
}
