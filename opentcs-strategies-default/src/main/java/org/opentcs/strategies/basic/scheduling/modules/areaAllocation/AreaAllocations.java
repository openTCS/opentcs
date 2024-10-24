// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.strategies.basic.scheduling.modules.areaAllocation;

import jakarta.inject.Inject;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import org.locationtech.jts.geom.GeometryCollection;
import org.opentcs.data.TCSObjectReference;
import org.opentcs.data.model.Vehicle;

/**
 * A container for keeping track of areas allocated by vehicles.
 */
public class AreaAllocations {

  private final Map<TCSObjectReference<Vehicle>, GeometryCollection> allocatedAreasByVehicles
      = new HashMap<>();

  @Inject
  public AreaAllocations() {
  }

  /**
   * Clears the area allocations for all vehicles.
   */
  public void clearAreaAllocations() {
    allocatedAreasByVehicles.clear();
  }

  /**
   * Sets the allocation for the given vehicle to the given allocated areas, discarding any previous
   * area allocation.
   *
   * @param vehicleRef The vehicle reference.
   * @param allocatedAreas The allocated areas to set as the vehicle's current area allocation.
   */
  public void setAreaAllocation(
      TCSObjectReference<Vehicle> vehicleRef,
      GeometryCollection allocatedAreas
  ) {
    allocatedAreasByVehicles.put(vehicleRef, allocatedAreas);
  }

  /**
   * Clears the area allocation for the given vehicle.
   *
   * @param vehicleRef The vehicle reference.
   */
  public void clearAreaAllocation(TCSObjectReference<Vehicle> vehicleRef) {
    allocatedAreasByVehicles.remove(vehicleRef);
  }

  /**
   * Checks if the given vehicle is allowed to allocate the given ares.
   *
   * @param vehicleRef The vehicle reference.
   * @param requestedAreas The requested areas (to be allocated).
   * @return {@code true}, if the vehicle is allowed to allocate the given areas, otherwise
   * {@code false} (i.e. in case some of the reuqested areas are already allocated by other
   * vehicles).
   */
  public boolean isAreaAllocationAllowed(
      TCSObjectReference<Vehicle> vehicleRef,
      GeometryCollection requestedAreas
  ) {
    return allocatedAreasByVehicles.entrySet().stream()
        // Only check areas allocated by vehicles other than the given vehicle.
        .filter(entry -> !Objects.equals(entry.getKey(), vehicleRef))
        .noneMatch(entry -> entry.getValue().intersects(requestedAreas));
  }
}
