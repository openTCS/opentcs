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
import org.opentcs.strategies.basic.dispatching.priorization.vehicle.VehicleComparatorByName;

/**
 *
 * @author Mustafa Yalciner (Fraunhofer IML)
 */
public class VehicleComparatorByNameTest {
  private VehicleComparatorByName comparator;

  @Before
  public void setUp() {
    comparator = new VehicleComparatorByName();
  }

  
  @Test
  public void sortsAlphabeticallyByName() {
    Vehicle vehicle1 = new Vehicle("AA");
    Vehicle vehicle2 = new Vehicle("CC");
    Vehicle vehicle3 = new Vehicle("AB");

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
