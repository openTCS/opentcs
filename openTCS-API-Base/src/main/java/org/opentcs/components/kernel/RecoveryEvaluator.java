/*
 * openTCS copyright information:
 * Copyright (c) 2012 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.components.kernel;

import javax.annotation.Nonnull;
import org.opentcs.access.queries.QueryRecoveryStatus;
import org.opentcs.components.Lifecycle;

/**
 * Objects implementing this interface may evaluate the kernel's recovery
 * status.
 *
 * @see org.opentcs.access.queries.QueryRecoveryStatus
 * @see org.opentcs.access.Kernel#query(java.lang.Class)
 * @author Stefan Walter (Fraunhofer IML)
 */
public interface RecoveryEvaluator
    extends Lifecycle {

  /**
   * Returns an evaluation of the kernel's recovery status.
   *
   * @return An evaluation of the kernel's recovery status.
   */
  @Nonnull
  QueryRecoveryStatus evaluateRecovery();
}
