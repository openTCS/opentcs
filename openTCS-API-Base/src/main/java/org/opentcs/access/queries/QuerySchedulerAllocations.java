/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.access.queries;

import java.io.Serializable;
import java.util.Collections;
import java.util.Map;
import static java.util.Objects.requireNonNull;
import java.util.Set;
import org.opentcs.access.Kernel;
import org.opentcs.data.model.TCSResource;
import org.opentcs.util.annotations.ScheduledApiChange;

/**
 * A query for resources currently allocated.
 *
 * @author Stefan Walter (Fraunhofer IML)
 * @deprecated Instead of queries, explicit service calls should be used.
 */
@Deprecated
@ScheduledApiChange(when = "5.0")
@Availability(Kernel.State.OPERATING)
public class QuerySchedulerAllocations
    extends Query<QuerySchedulerAllocations>
    implements Serializable {

  /**
   * The current state of allocations.
   */
  private final Map<String, Set<TCSResource<?>>> allocations;

  /**
   * Creates a new instance.
   *
   * @param allocations The current state of allocations.
   */
  public QuerySchedulerAllocations(Map<String, Set<TCSResource<?>>> allocations) {
    this.allocations = Collections.unmodifiableMap(requireNonNull(allocations, "allocations"));
  }

  /**
   * Returns the current state of allocations.
   *
   * @return The current state of allocations.
   */
  public Map<String, Set<TCSResource<?>>> getAllocations() {
    return allocations;
  }
}
