/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.strategies.basic.dispatching.vehicleselection;

import java.util.LinkedList;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThan;
import org.junit.*;
import static org.junit.Assert.*;
import org.opentcs.data.model.Vehicle;

/**
 *
 * @author Mustafa Yalciner (Fraunhofer IML)
 */
public class ClosestVehicleComparatorTest {

  private ClosestVehicleComparator vehicComparator;

  @Before
  public void setUp() {
    vehicComparator = new ClosestVehicleComparator();
  }

  @Test
  public void shouldCompareCostsFirst() {
    Vehicle vehicle1 = new Vehicle("vehicle1").withEnergyLevel(60);
    Vehicle vehicle2 = new Vehicle("vehicle2").withEnergyLevel(70);
    VehicleCandidate candidate1 = new VehicleCandidate(vehicle1, 60, new LinkedList<>());
    VehicleCandidate candidate2 = new VehicleCandidate(vehicle2, 50, new LinkedList<>());
    int result = vehicComparator.compare(candidate1, candidate2);
    assertThat(result, is(greaterThan(0)));
  }

  @Test
  public void shouldCompareEnergyLevelSecond() {
    Vehicle vehicle1 = new Vehicle("vehicle1").withEnergyLevel(80);
    Vehicle vehicle2 = new Vehicle("vehicle2").withEnergyLevel(70);
    VehicleCandidate candidate1 = new VehicleCandidate(vehicle1, 50, new LinkedList<>());
    VehicleCandidate candidate2 = new VehicleCandidate(vehicle2, 50, new LinkedList<>());
    int result = vehicComparator.compare(candidate1, candidate2);
    assertThat(result, is(lessThan(0)));
  }

  @Test
  public void shouldCompareNameThird() {
    Vehicle vehicle1 = new Vehicle("vehicle1").withEnergyLevel(70);
    Vehicle vehicle2 = new Vehicle("vehicle2").withEnergyLevel(70);
    VehicleCandidate candidate1 = new VehicleCandidate(vehicle1, 50, new LinkedList<>());
    VehicleCandidate candidate2 = new VehicleCandidate(vehicle2, 50, new LinkedList<>());
    int result = vehicComparator.compare(candidate1, candidate2);
    assertThat(result, is(lessThan(0)));
  }

}
