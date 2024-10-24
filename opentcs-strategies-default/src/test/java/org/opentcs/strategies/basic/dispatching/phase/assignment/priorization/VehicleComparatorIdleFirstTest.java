// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.strategies.basic.dispatching.phase.assignment.priorization;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.theInstance;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opentcs.data.model.Vehicle;
import org.opentcs.strategies.basic.dispatching.priorization.vehicle.VehicleComparatorIdleFirst;

/**
 */
class VehicleComparatorIdleFirstTest {

  private VehicleComparatorIdleFirst comparator;

  @BeforeEach
  void setUp() {
    comparator = new VehicleComparatorIdleFirst();
  }

  @Test
  void sortIdleVehiclesUp() {
    Vehicle vehicle1 = new Vehicle("Vehicle1").withState(Vehicle.State.CHARGING);
    Vehicle vehicle2 = vehicle1.withState(Vehicle.State.IDLE);
    Vehicle vehicle3 = vehicle1.withState(Vehicle.State.CHARGING);

    List<Vehicle> list = new ArrayList<>();
    list.add(vehicle1);
    list.add(vehicle2);
    list.add(vehicle3);

    Collections.sort(list, comparator);

    assertThat(list.get(0), is(theInstance(vehicle2)));
  }

}
