// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.strategies.basic.dispatching.phase;

import static java.util.Objects.requireNonNull;

import java.util.Collection;
import org.opentcs.data.model.Vehicle;

/**
 * The result of a vehicle filter operation.
 */
public class VehicleFilterResult {

  private final Vehicle vehicle;

  private final Collection<String> filterReasons;

  public VehicleFilterResult(Vehicle vehicle, Collection<String> filterReasons) {
    this.vehicle = requireNonNull(vehicle, "vehicle");
    this.filterReasons = requireNonNull(filterReasons, "filterReasons");
  }

  public Vehicle getVehicle() {
    return vehicle;
  }

  public Collection<String> getFilterReasons() {
    return filterReasons;
  }

  public boolean isFiltered() {
    return !filterReasons.isEmpty();
  }
}
