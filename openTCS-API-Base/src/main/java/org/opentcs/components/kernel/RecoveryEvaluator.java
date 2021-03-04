/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.components.kernel;

import javax.annotation.Nonnull;
import org.opentcs.components.Lifecycle;
import org.opentcs.util.annotations.ScheduledApiChange;

/**
 * Objects implementing this interface may evaluate the kernel's recovery
 * status.
 *
 * @see org.opentcs.access.queries.QueryRecoveryStatus
 * @see org.opentcs.access.Kernel#query(java.lang.Class)
 * @author Stefan Walter (Fraunhofer IML)
 * @deprecated The definition of <em>recovered</em> is unclear. Unless it is clearly specified,
 * evaluation of a state of recovery should not be part of the API.
 */
@Deprecated
@ScheduledApiChange(when = "5.0")
public interface RecoveryEvaluator
    extends Lifecycle {

  /**
   * Returns an evaluation of the kernel's recovery status.
   *
   * @return An evaluation of the kernel's recovery status.
   */
  @Nonnull
  org.opentcs.access.queries.QueryRecoveryStatus evaluateRecovery();
}
