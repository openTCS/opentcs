/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.common.exchange;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import static java.util.Objects.requireNonNull;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import org.opentcs.data.model.TCSResourceReference;
import org.opentcs.data.model.Vehicle;

/**
 * Keeps track of the resources claimed and allocated by vehicles.
 */
public class AllocationHistory {

  /**
   * The resources currently and previously claimed and allocated by vehicles mapped to the
   * respective vehicle's name.
   */
  private final Map<String, Entry> history = new HashMap<>();

  /**
   * Creates a new instance.
   */
  public AllocationHistory() {
  }

  /**
   * Updates the allocation history for the given vehicle and returns the updated history entry
   * containing the vehicle's currently and previously claimed and allocated resources.
   * <p>
   * The currently allocated resources are divided into two lists, one for resources that still lie
   * ahead for the vehicle's current drive order and one for resources that lie behind (including
   * the resources for the route step last travelled by the vehicle). If the resources that lie
   * ahead of the vehicle cannot be determined, all currently allocated resources are added to the
   * 'behind' list.
   *
   * @param vehicle The vehicle.
   * @return The updated history entry containing the vehicle's currently and previously claimed and
   * allocated resources.
   */
  @Nonnull
  public Entry updateHistory(@Nonnull Vehicle vehicle) {
    requireNonNull(vehicle, "vehicle");

    Set<TCSResourceReference<?>> newClaimedResources = vehicle.getClaimedResources().stream()
        .flatMap(Collection::stream)
        .collect(Collectors.toSet());
    SplitResources allocatedResources = SplitResources.from(vehicle.getAllocatedResources(),
                                                            vehicle.getCurrentPosition());
    Set<TCSResourceReference<?>> newAllocatedResourcesBehind
        = allocatedResources.getAllocatedResourcesBehind().stream()
              .flatMap(Set::stream)
              .collect(Collectors.toSet());
    Set<TCSResourceReference<?>> newAllocatedResourcesAhead
        = allocatedResources.getAllocatedResourcesAhead().stream()
              .flatMap(Set::stream)
              .collect(Collectors.toSet());
    Set<TCSResourceReference<?>> noLongerClaimedOrAllocatedResources
        = determineNoLongerClaimedOrAllocatedResources(vehicle.getName(),
                                                       newClaimedResources,
                                                       newAllocatedResourcesAhead,
                                                       newAllocatedResourcesBehind);

    Entry newEntry = new Entry(newClaimedResources,
                               newAllocatedResourcesAhead,
                               newAllocatedResourcesBehind,
                               noLongerClaimedOrAllocatedResources);

    history.put(vehicle.getName(), newEntry);

    return newEntry;
  }

  private Set<TCSResourceReference<?>> determineNoLongerClaimedOrAllocatedResources(
      String vehicleName,
      Set<TCSResourceReference<?>> newCurrentClaimedResources,
      Set<TCSResourceReference<?>> newCurrentAllocatedResourcesAhead,
      Set<TCSResourceReference<?>> newCurrentAllocatedResourcesBehind) {
    Set<TCSResourceReference<?>> result = new HashSet<>();
    result.addAll(getEntryFor(vehicleName).getCurrentClaimedResources());
    result.addAll(getEntryFor(vehicleName).getCurrentAllocatedResourcesAhead());
    result.addAll(getEntryFor(vehicleName).getCurrentAllocatedResourcesBehind());
    result.removeAll(newCurrentClaimedResources);
    result.removeAll(newCurrentAllocatedResourcesAhead);
    result.removeAll(newCurrentAllocatedResourcesBehind);
    return result;
  }

  private Entry getEntryFor(String vehicleName) {
    return history.computeIfAbsent(
        vehicleName, v -> new Entry(Set.of(), Set.of(), Set.of(), Set.of())
    );
  }

  /**
   * An entry in the allocation history holding the resources currently and previously claimed and
   * allocated by a vehicle.
   */
  public static class Entry {

    private final Set<TCSResourceReference<?>> currentClaimedResources;
    private final Set<TCSResourceReference<?>> currentAllocatedResourcesAhead;
    private final Set<TCSResourceReference<?>> currentAllocatedResourcesBehind;
    private final Set<TCSResourceReference<?>> previouslyClaimedOrAllocatedResources;

    Entry(Set<TCSResourceReference<?>> currentClaimedResources,
          Set<TCSResourceReference<?>> currentAllocatedResourcesAhead,
          Set<TCSResourceReference<?>> currentAllocatedResourcesBehind,
          Set<TCSResourceReference<?>> previouslyClaimedOrAllocatedResources) {
      this.currentClaimedResources = requireNonNull(currentClaimedResources,
                                                    "currentClaimedResources");
      this.currentAllocatedResourcesAhead = requireNonNull(currentAllocatedResourcesAhead,
                                                           "currentAllocatedResourcesAhead");
      this.currentAllocatedResourcesBehind = requireNonNull(currentAllocatedResourcesBehind,
                                                            "currentAllocatedResourcesBehind");
      this.previouslyClaimedOrAllocatedResources
          = requireNonNull(previouslyClaimedOrAllocatedResources,
                           "previouslyClaimedOrAllocatedResources");
    }

    public Set<TCSResourceReference<?>> getCurrentClaimedResources() {
      return currentClaimedResources;
    }

    public Set<TCSResourceReference<?>> getCurrentAllocatedResourcesAhead() {
      return currentAllocatedResourcesAhead;
    }

    public Set<TCSResourceReference<?>> getCurrentAllocatedResourcesBehind() {
      return currentAllocatedResourcesBehind;
    }

    public Set<TCSResourceReference<?>> getPreviouslyClaimedOrAllocatedResources() {
      return previouslyClaimedOrAllocatedResources;
    }
  }
}
