/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.strategies.basic.dispatching.selection.vehicles;

import java.util.Collection;
import static java.util.Objects.requireNonNull;
import java.util.Set;
import java.util.stream.Collectors;
import javax.inject.Inject;
import org.opentcs.data.model.Vehicle;
import org.opentcs.strategies.basic.dispatching.selection.ParkVehicleSelectionFilter;

/**
 * A collection of {@link ParkVehicleSelectionFilter}s.
 *
 * @author Martin Grzenia (Fraunhofer IML)
 */
public class CompositeParkVehicleSelectionFilter
    implements ParkVehicleSelectionFilter {

  /**
   * The {@link ParkVehicleSelectionFilter}s.
   */
  private final Set<ParkVehicleSelectionFilter> filters;

  @Inject
  public CompositeParkVehicleSelectionFilter(Set<ParkVehicleSelectionFilter> filters) {
    this.filters = requireNonNull(filters, "filters");
  }

  @Override
  public Collection<String> apply(Vehicle vehicle) {
    return filters.stream()
        .flatMap(filter -> filter.apply(vehicle).stream())
        .collect(Collectors.toList());
  }
}
