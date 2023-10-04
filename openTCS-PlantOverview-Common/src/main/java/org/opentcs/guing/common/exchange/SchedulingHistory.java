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
import javax.annotation.Nonnull;
import org.opentcs.guing.base.model.FigureDecorationDetails;
import org.opentcs.guing.base.model.ModelComponent;

/**
 * Keeps track of the resources (represented by their respective {@link ModelComponent}s) allocated
 * and claimed by vehicles.
 */
public class SchedulingHistory {

  /**
   * The resources (represented by their respective {@link ModelComponent}s) allocated and claimed
   * by vehicles mapped to the respective vehicle's name.
   */
  private final Map<String, Set<FigureDecorationDetails>> allocatedAndClaimedComponents
      = new HashMap<>();

  /**
   * Creates a new instance.
   */
  public SchedulingHistory() {
  }

  /**
   * Remembers the given set of components as the new allocated and claimed components for the given
   * vehicle name and returns the set difference of the old and the new drive order components
   * (e.g. the components that are no longer allocated or claimed by the vehicle).
   *
   * @param vehicleName The name of the vehicle.
   * @param newComponents The new allocated and claimed components.
   * @return The set difference of the old and the new allocated and claimed components.
   */
  @Nonnull
  public Set<FigureDecorationDetails> updateAllocatedAndClaimedComponents(
      @Nonnull String vehicleName,
      @Nonnull Set<FigureDecorationDetails> newComponents) {
    requireNonNull(vehicleName, "vehicleName");
    requireNonNull(newComponents, "newComponents");

    Set<FigureDecorationDetails> currentComponents = getAllocatedAndClaimedComponents(vehicleName);
    
    Set<FigureDecorationDetails> result = new HashSet<>(currentComponents);
    result.removeAll(newComponents);

    currentComponents.clear();
    currentComponents.addAll(newComponents);

    return result;
  }

  private Set<FigureDecorationDetails> getAllocatedAndClaimedComponents(String vehicleName) {
    return allocatedAndClaimedComponents.computeIfAbsent(vehicleName, v -> new HashSet<>());
  }
}
