/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.access;

import java.io.Serializable;
import java.util.Collections;
import java.util.Map;
import static java.util.Objects.requireNonNull;
import java.util.Set;
import javax.annotation.Nonnull;
import org.opentcs.data.model.TCSResource;
import org.opentcs.data.model.Vehicle;
import org.opentcs.util.annotations.ScheduledApiChange;

/**
 * Represents the current state of resource allocations.
 *
 * @deprecated Acquire allocations from {@link Vehicle}, instead.
 */
@Deprecated
@ScheduledApiChange(when = "6.0", details = "Will be removed.")
public class SchedulerAllocationState
    implements Serializable {

  /**
   * The current state of allocations.
   */
  private final Map<String, Set<TCSResource<?>>> allocationStates;

  /**
   * Creates a new instance.
   *
   * @param allocationStates The current state of allocations.
   */
  public SchedulerAllocationState(@Nonnull Map<String, Set<TCSResource<?>>> allocationStates) {
    this.allocationStates = Collections.unmodifiableMap(requireNonNull(allocationStates,
                                                                       "allocationStates"));
  }

  /**
   * Returns the current state of allocations.
   *
   * @return The current state of allocations.
   */
  public Map<String, Set<TCSResource<?>>> getAllocationStates() {
    return allocationStates;
  }
}
