/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.strategies.basic.dispatching.phase.assignment.priorization;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.theInstance;
import org.junit.*;
import static org.junit.Assert.*;
import org.opentcs.data.model.Vehicle;
import org.opentcs.strategies.basic.dispatching.priorization.vehicle.VehicleComparatorIdleFirst;

/**
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public class VehicleComparatorIdleFirstTest {

  private VehicleComparatorIdleFirst comparator;

  @Before
  public void setUp() {
    comparator = new VehicleComparatorIdleFirst();
  }

  @Test
  public void sortIdleVehiclesUp() {
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
