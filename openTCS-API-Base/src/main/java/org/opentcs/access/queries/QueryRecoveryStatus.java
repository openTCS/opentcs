/*
 * openTCS copyright information:
 * Copyright (c) 2012 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.access.queries;

import java.io.Serializable;
import org.opentcs.access.Kernel;

/**
 * A query for the current recovery status of the overall system (vehicle and
 * resources availability).
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
@Availability(Kernel.State.OPERATING)
public class QueryRecoveryStatus
    extends Query<QueryRecoveryStatus>
    implements Serializable {

  /**
   * Whether the system is recovered or not.
   */
  private final boolean recovered;

  /**
   * Creates a new instance.
   *
   * @param recovered Whether the system is recovered or not.
   */
  public QueryRecoveryStatus(boolean recovered) {
    this.recovered = recovered;
  }

  /**
   * Checks whether the system is recovered or not.
   *
   * @return <code>true</code> if, and only if, the system is recovered.
   */
  public boolean isRecovered() {
    return recovered;
  }
}
