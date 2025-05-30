// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.kernel.vehicles;

import static java.util.Objects.requireNonNull;
import static org.opentcs.util.Assertions.checkArgument;

import jakarta.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.opentcs.data.model.Path;
import org.opentcs.data.model.TCSResource;

/**
 * Utility methods for resource-related computations.
 */
public class ResourceMath {

  /**
   * Prevents instantiation.
   */
  private ResourceMath() {
  }

  /**
   * Returns the number of resource sets that could be freed based on the given vehicle length.
   *
   * @param resourcesPassed A list of passed resource sets still allocated for a vehicle, from
   * oldest to youngest, including those for the vehicle's current position (= the last set in the
   * list).
   * @param vehicleLength The vehicle's length. Must be a positive value.
   * @return The number of resource sets from {@code resourcesPassed} that could be freed because
   * they are not covered by the vehicle's length any more.
   */
  public static int freeableResourceSetCount(
      @Nonnull
      List<Set<TCSResource<?>>> resourcesPassed,
      long vehicleLength
  ) {
    requireNonNull(resourcesPassed, "resourcesPassed");
    checkArgument(vehicleLength > 0, "vehicleLength <= 0");

    // We want to iterate over the passed resources from youngest (= current position) to oldest, so
    // we reverse the list here.
    List<Set<TCSResource<?>>> reversedPassedResources = new ArrayList<>(resourcesPassed);
    Collections.reverse(reversedPassedResources);

    long remainingRequiredLength = vehicleLength;
    int result = 0;
    for (Set<TCSResource<?>> curSet : reversedPassedResources) {
      if (remainingRequiredLength > 0) {
        remainingRequiredLength -= requiredLength(curSet);
      }
      else {
        result++;
      }
    }

    return result;
  }

  private static long requiredLength(Set<TCSResource<?>> resources) {
    return resources.stream()
        .filter(resource -> resource instanceof Path)
        .mapToLong(resource -> ((Path) resource).getLength())
        .sum();
  }

}
