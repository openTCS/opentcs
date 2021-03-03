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
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import org.opentcs.data.model.TCSResource;
import org.opentcs.access.Kernel;

/**
 * A query for resources currently allocated.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
@Availability(Kernel.State.OPERATING)
public final class QuerySchedulerAllocations
    extends Query<QuerySchedulerAllocations>
    implements Serializable {

  /**
   * The current state of allocations.
   */
  private final Map<String, Set<TCSResource>> allocations;

  /**
   * Creates a new instance.
   *
   * @param allocations The current state of allocations.
   */
  public QuerySchedulerAllocations(Map<String, Set<TCSResource>> allocations) {
    this.allocations = Collections.unmodifiableMap(
        Objects.requireNonNull(allocations, "allocations is null"));
  }

  /**
   * Returns the current state of allocations.
   *
   * @return The current state of allocations.
   */
  public Map<String, Set<TCSResource>> getAllocations() {
    return allocations;
  }
}
