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
import static org.junit.Assert.assertThat;
import org.opentcs.data.model.Vehicle;
import org.opentcs.strategies.basic.dispatching.priorization.vehicle.VehicleComparatorByEnergyLevel;

/**
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public class VehicleComparatorByEnergyLevelTest {

  private VehicleComparatorByEnergyLevel comparator;

  @Before
  public void setUp() {
    comparator = new VehicleComparatorByEnergyLevel();
  }

  @Test
  public void sortHighEnergyLevelsUp() {
    Vehicle vehicle1 = new Vehicle("Vehicle1").withEnergyLevel(99);
    Vehicle vehicle2 = vehicle1.withEnergyLevel(50);
    Vehicle vehicle3 = vehicle1.withEnergyLevel(98);

    List<Vehicle> list = new ArrayList<>();
    list.add(vehicle1);
    list.add(vehicle2);
    list.add(vehicle3);

    Collections.sort(list, comparator);

    assertThat(list.get(0), is(theInstance(vehicle1)));
    assertThat(list.get(1), is(theInstance(vehicle3)));
    assertThat(list.get(2), is(theInstance(vehicle2)));
  }

}
