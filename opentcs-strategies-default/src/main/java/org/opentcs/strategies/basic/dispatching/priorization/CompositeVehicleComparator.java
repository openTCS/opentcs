// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.strategies.basic.dispatching.priorization;

import static org.opentcs.util.Assertions.checkArgument;

import com.google.common.collect.Lists;
import jakarta.inject.Inject;
import java.util.Comparator;
import java.util.Map;
import org.opentcs.data.model.Vehicle;
import org.opentcs.strategies.basic.dispatching.DefaultDispatcherConfiguration;
import org.opentcs.strategies.basic.dispatching.priorization.vehicle.VehicleComparatorByEnergyLevel;
import org.opentcs.strategies.basic.dispatching.priorization.vehicle.VehicleComparatorByName;

/**
 * A composite of all configured vehicle comparators.
 */
public class CompositeVehicleComparator
    implements
      Comparator<Vehicle> {

  /**
   * A comparator composed of all configured comparators, in the configured order.
   */
  private final Comparator<Vehicle> compositeComparator;

  @Inject
  public CompositeVehicleComparator(
      DefaultDispatcherConfiguration configuration,
      Map<String, Comparator<Vehicle>> availableComparators
  ) {
    // At the end, if all other comparators failed to see a difference, compare by energy level.
    // As the energy level of two distinct vehicles may still be the same, finally compare by name.
    // Add configured comparators before these two.
    Comparator<Vehicle> composite
        = new VehicleComparatorByEnergyLevel().thenComparing(new VehicleComparatorByName());

    for (String priorityKey : Lists.reverse(configuration.vehiclePriorities())) {
      Comparator<Vehicle> configuredComparator = availableComparators.get(priorityKey);
      checkArgument(
          configuredComparator != null,
          "Unknown vehicle priority key: '%s'",
          priorityKey
      );
      composite = configuredComparator.thenComparing(composite);
    }
    this.compositeComparator = composite;
  }

  @Override
  public int compare(Vehicle o1, Vehicle o2) {
    return compositeComparator.compare(o1, o2);
  }

}
