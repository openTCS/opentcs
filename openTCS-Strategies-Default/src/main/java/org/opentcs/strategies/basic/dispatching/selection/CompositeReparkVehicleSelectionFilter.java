/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.strategies.basic.dispatching.selection;

import static java.util.Objects.requireNonNull;
import java.util.Set;
import javax.inject.Inject;
import org.opentcs.data.model.Vehicle;

/**
 * A collection of {@link ReparkVehicleSelectionFilter}s.
 * 
 * @author Stefan Walter (Fraunhofer IML)
 */
public class CompositeReparkVehicleSelectionFilter
    implements ReparkVehicleSelectionFilter {

  /**
   * The {@link ParkVehicleSelectionFilter}s.
   */
  private final Set<ReparkVehicleSelectionFilter> filters;
  
  @Inject
  public CompositeReparkVehicleSelectionFilter(Set<ReparkVehicleSelectionFilter> filters) {
    this.filters = requireNonNull(filters, "filters");
  }

  @Override
  public boolean test(Vehicle vehicle) {
    boolean result = true;
    for (ReparkVehicleSelectionFilter filter : filters) {
      result &= filter.test(vehicle);
    }
    return result;
  }
}
