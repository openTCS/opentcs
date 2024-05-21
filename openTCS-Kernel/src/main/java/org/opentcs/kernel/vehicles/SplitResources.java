/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.kernel.vehicles;

import java.util.ArrayList;
import java.util.List;
import static java.util.Objects.requireNonNull;
import java.util.Set;
import javax.annotation.Nonnull;
import org.opentcs.data.model.TCSResource;

/**
 * A vehicle's resources, split into resources the vehicle has already passed (including the
 * resources for the vehicle's current position) and resources that still lay ahead of it.
 */
public class SplitResources {

  private final List<Set<TCSResource<?>>> resourcesPassed;
  private final List<Set<TCSResource<?>>> resourcesAhead;

  /**
   * Creates a new instance.
   *
   * @param resourcesPassed The passed resources, including the ones for the vehicle's current
   * position.
   * @param resourcesAhead The resources ahead of the vehicle.
   */
  public SplitResources(@Nonnull List<Set<TCSResource<?>>> resourcesPassed,
                        @Nonnull List<Set<TCSResource<?>>> resourcesAhead) {
    this.resourcesPassed = requireNonNull(resourcesPassed, "resourcesPassed");
    this.resourcesAhead = requireNonNull(resourcesAhead, "resourcesAhead");
  }

  /**
   * Returns the resources the vehicle has already passed, from oldest to youngest, with the
   * youngest being the resources for the vehicle's current position.
   *
   * @return The resources the vehicle has already passed, from oldest to youngest, with the
   * youngest being the resources for the vehicle's current position.
   */
  public List<Set<TCSResource<?>>> getResourcesPassed() {
    return resourcesPassed;
  }

  /**
   * Returns the resources ahead of the vehicle, from oldest to youngest.
   *
   * @return The resources ahead of the vehicle, from oldest to youngest.
   */
  public List<Set<TCSResource<?>>> getResourcesAhead() {
    return resourcesAhead;
  }

  /**
   * Returns a new instance created from the given iterable of resources, split at the element that
   * contains the given delimiter (resources).
   *
   * @param resourceSets The iterable of resources to be split, from oldest to youngest.
   * @param delimiter The delimiter / resources for the vehicle's current position.
   * @return A new instance created from the given iterable of resources, split at the element that
   * contains the given delimiter (resources).
   */
  public static SplitResources from(@Nonnull Iterable<Set<TCSResource<?>>> resourceSets,
                                    @Nonnull Set<TCSResource<?>> delimiter) {
    requireNonNull(resourceSets, "resourceSets");
    requireNonNull(delimiter, "delimiter");

    List<Set<TCSResource<?>>> resourcesPassed = new ArrayList<>();
    List<Set<TCSResource<?>>> resourcesAhead = new ArrayList<>();
    List<Set<TCSResource<?>>> resourcesToPutIn = resourcesPassed;

    for (Set<TCSResource<?>> curSet : resourceSets) {
      resourcesToPutIn.add(curSet);

      if (!delimiter.isEmpty() && curSet.containsAll(delimiter)) {
        resourcesToPutIn = resourcesAhead;
      }
    }

    return new SplitResources(resourcesPassed, resourcesAhead);
  }

}
