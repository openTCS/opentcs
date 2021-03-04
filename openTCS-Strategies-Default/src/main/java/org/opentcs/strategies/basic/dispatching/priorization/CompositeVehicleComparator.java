/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.strategies.basic.dispatching.priorization;

import com.google.common.collect.Lists;
import java.util.Comparator;
import java.util.Map;
import javax.inject.Inject;
import org.opentcs.data.model.Vehicle;
import org.opentcs.strategies.basic.dispatching.DefaultDispatcherConfiguration;
import org.opentcs.strategies.basic.dispatching.priorization.vehicle.VehicleComparatorByEnergyLevel;
import org.opentcs.strategies.basic.dispatching.priorization.vehicle.VehicleComparatorByName;
import static org.opentcs.util.Assertions.checkArgument;

/**
 * A composite of all configured vehicle comparators.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public class CompositeVehicleComparator
    implements Comparator<Vehicle> {

  /**
   * A comparator composed of all configured comparators, in the configured order.
   */
  private final Comparator<Vehicle> compositeComparator;

  @Inject
  public CompositeVehicleComparator(DefaultDispatcherConfiguration configuration,
                                    Map<String, Comparator<Vehicle>> availableComparators) {
    // At the end, if all other comparators failed to see a difference, compare by energy level.
    // As the energy level of two distinct vehicles may still be the same, finally compare by name.
    // Add configured comparators before these two.
    Comparator<Vehicle> composite
        = new VehicleComparatorByEnergyLevel().thenComparing(new VehicleComparatorByName());

    for (String priorityKey : Lists.reverse(configuration.vehiclePriorities())) {
      Comparator<Vehicle> configuredComparator = availableComparators.get(priorityKey);
      checkArgument(configuredComparator != null, "Unknown vehicle priority key: %s", priorityKey);
      composite = configuredComparator.thenComparing(composite);
    }
    this.compositeComparator = composite;
  }

  @Override
  public int compare(Vehicle o1, Vehicle o2) {
    return compositeComparator.compare(o1, o2);
  }

}
