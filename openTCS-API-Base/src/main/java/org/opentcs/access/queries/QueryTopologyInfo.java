/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.access.queries;

import java.io.Serializable;
import java.util.Objects;
import org.opentcs.access.Kernel;
import org.opentcs.util.annotations.ScheduledApiChange;

/**
 * A query for general information about the topology currently set up on the
 * kernel side.
 *
 * @author Stefan Walter (Fraunhofer IML)
 * @deprecated Instead of queries, explicit service calls should be used.
 */
@Deprecated
@ScheduledApiChange(when = "5.0")
@Availability({Kernel.State.MODELLING,
               Kernel.State.OPERATING})
public class QueryTopologyInfo
    extends Query<QueryTopologyInfo>
    implements Serializable {

  /**
   * Some general topology information.
   */
  private final String topologyInfo;

  /**
   * Creates a new instance.
   *
   * @param info Some general topology information.
   */
  public QueryTopologyInfo(String info) {
    topologyInfo = Objects.requireNonNull(info, "info is null");
  }

  /**
   * Returns some general topology information.
   *
   * @return Some general topology information.
   */
  public String getTopologyInfo() {
    return topologyInfo;
  }
}
