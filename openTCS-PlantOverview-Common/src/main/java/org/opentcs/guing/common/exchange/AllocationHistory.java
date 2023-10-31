/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.common.exchange;

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
   *
   * @param vehicle The vehicle.
   * @return The updated history entry containing the vehicle's currently and previously claimed and
   * allocated resources.
   */
  @Nonnull
  public Entry updateHistory(@Nonnull Vehicle vehicle) {
    requireNonNull(vehicle, "vehicle");

    Set<TCSResourceReference<?>> newClaimedResources = vehicle.getClaimedResources().stream()
        .flatMap(resourceSet -> resourceSet.stream())
        .collect(Collectors.toSet());
    Set<TCSResourceReference<?>> newAllocatedResources = vehicle.getAllocatedResources().stream()
        .flatMap(resourceSet -> resourceSet.stream())
        .collect(Collectors.toSet());
    Set<TCSResourceReference<?>> noLongerClaimedOrAllocatedResources
        = determineNoLongerClaimedOrAllocatedResources(vehicle.getName(),
                                                       newClaimedResources,
                                                       newAllocatedResources);

    Entry newEntry = new Entry(newClaimedResources,
                               newAllocatedResources,
                               noLongerClaimedOrAllocatedResources);

    history.put(vehicle.getName(), newEntry);

    return newEntry;
  }

  private Set<TCSResourceReference<?>> determineNoLongerClaimedOrAllocatedResources(
      String vehicleName,
      Set<TCSResourceReference<?>> newCurrentClaimedResources,
      Set<TCSResourceReference<?>> newCurrentAllocatedResources) {
    Set<TCSResourceReference<?>> result = new HashSet<>();
    result.addAll(getEntryFor(vehicleName).getCurrentClaimedResources());
    result.addAll(getEntryFor(vehicleName).getCurrentAllocatedResources());
    result.removeAll(newCurrentClaimedResources);
    result.removeAll(newCurrentAllocatedResources);
    return result;
  }

  private Entry getEntryFor(String vehicleName) {
    return history.computeIfAbsent(
        vehicleName, v -> new Entry(Set.of(), Set.of(), Set.of())
    );
  }

  /**
   * An entry in the allocation history holding the resources currently and previously claimed and
   * allocated by a vehicle.
   */
  public static class Entry {

    private final Set<TCSResourceReference<?>> currentClaimedResources;
    private final Set<TCSResourceReference<?>> currentAllocatedResources;
    private final Set<TCSResourceReference<?>> previouslyClaimedOrAllocatedResources;

    Entry(Set<TCSResourceReference<?>> currentClaimedResources,
          Set<TCSResourceReference<?>> currentAllocatedResources,
          Set<TCSResourceReference<?>> previouslyClaimedOrAllocatedResources) {
      this.currentClaimedResources = requireNonNull(currentClaimedResources,
                                                    "currentClaimedResources");
      this.currentAllocatedResources = requireNonNull(currentAllocatedResources,
                                                      "currentAllocatedResources");
      this.previouslyClaimedOrAllocatedResources
          = requireNonNull(previouslyClaimedOrAllocatedResources,
                           "previouslyClaimedOrAllocatedResources");
    }

    public Set<TCSResourceReference<?>> getCurrentClaimedResources() {
      return currentClaimedResources;
    }

    public Set<TCSResourceReference<?>> getCurrentAllocatedResources() {
      return currentAllocatedResources;
    }

    public Set<TCSResourceReference<?>> getPreviouslyClaimedOrAllocatedResources() {
      return previouslyClaimedOrAllocatedResources;
    }
  }
}
