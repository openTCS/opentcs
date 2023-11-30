/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.common.exchange;

import java.util.ArrayList;
import java.util.List;
import static java.util.Objects.requireNonNull;
import java.util.Set;
import javax.annotation.Nonnull;
import org.opentcs.data.TCSObjectReference;
import org.opentcs.data.model.TCSResourceReference;

/**
 * An AllocationHistory entry's allocated resources, split into resources that lie behind the
 * vehicle (including the resources for the vehicle's current position) and resources that still lie
 * ahead of it.
 */
public class SplitResources {

  private final List<Set<TCSResourceReference<?>>> allocatedResourcesBehind;
  private final List<Set<TCSResourceReference<?>>> allocatedResourcesAhead;

  /**
   * Creates a new instance.
   *
   * @param allocatedResourcesBehind The resources behind the vehicle, including the ones
   * for the vehicle's current position.
   * @param allocatedResourcesAhead The resources ahead of the vehicle.
   */
  public SplitResources(@Nonnull List<Set<TCSResourceReference<?>>> allocatedResourcesBehind,
                        @Nonnull List<Set<TCSResourceReference<?>>> allocatedResourcesAhead) {
    this.allocatedResourcesBehind = requireNonNull(allocatedResourcesBehind,
                                                   "allocatedResourcesBehind");
    this.allocatedResourcesAhead = requireNonNull(allocatedResourcesAhead,
                                                  "allocatedResourcesAhead");
  }

  /**
   * Returns the resources behind the vehicle, including the ones for the vehicle's
   * current step.
   *
   * @return The resources behind the vehicle, including the ones for the vehicle's
   * current step.
   */
  public List<Set<TCSResourceReference<?>>> getAllocatedResourcesBehind() {
    return allocatedResourcesBehind;
  }

  /**
   * Returns the resources ahead of the vehicle.
   *
   * @return The resources ahead of the vehicle.
   */
  public List<Set<TCSResourceReference<?>>> getAllocatedResourcesAhead() {
    return allocatedResourcesAhead;
  }

  /**
   * Returns a new instance created from the given list of resource sets, split at the given
   * delimiter.
   *
   * @param resourceSets The list of resource sets to be split in order of appearance in the route.
   * @param delimiter The delimiter / vehicle's current position.
   * @return A new instance created from the given list of resource sets, split at the given
   * delimiter. If the delimiter is null, all given resources are added to allocatedResourcesBehind.
   */
  public static SplitResources from(@Nonnull List<Set<TCSResourceReference<?>>> resourceSets,
                                    TCSObjectReference<?> delimiter) {
    requireNonNull(resourceSets, "resources");
    if (delimiter == null) {
      return new SplitResources(resourceSets, List.of());
    }

    List<Set<TCSResourceReference<?>>> resourcesBehind = new ArrayList<>();
    List<Set<TCSResourceReference<?>>> resourcesAhead = new ArrayList<>();
    List<Set<TCSResourceReference<?>>> resourcesToPutIn = resourcesBehind;

    for (Set<TCSResourceReference<?>> curSet : resourceSets) {
      resourcesToPutIn.add(curSet);

      if (curSet.contains(delimiter)) {
        resourcesToPutIn = resourcesAhead;
      }
    }

    return new SplitResources(resourcesBehind, resourcesAhead);
  }
}
