/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.strategies.basic.dispatching.orderselection;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.junit.*;
import static org.junit.Assert.*;
import org.junit.runner.*;
import org.opentcs.data.model.Vehicle;
import org.opentcs.strategies.basic.dispatching.VehicleOrderSelection;

/**
 *
 * @author Mustafa Yalciner (Fraunhofer IML)
 */
@RunWith(JUnitParamsRunner.class)
public class NoOrderSelectionStrategyTest {

  private NoOrderSelectionStrategy instance;

  @Before
  public void setUp() {
    instance = new NoOrderSelectionStrategy();
    instance.initialize();
  }

  @After
  public void tearDown() {
    instance.terminate();
  }

  @Test
  public void returnNullForDispatchableVehicleCharging() {
    Vehicle vehicle = new Vehicle("TestVehicle")
        .withEnergyLevel(51)
        .withEnergyLevelCritical(15)
        .withEnergyLevelGood(50)
        .withState(Vehicle.State.CHARGING);
    VehicleOrderSelection result = instance.selectOrder(vehicle);
    assertNull(result);
  }

  @Test
  public void returnNullForDispatchableVehicleIdle() {
    Vehicle vehicle = new Vehicle("TestVehicle")
        .withEnergyLevel(10)
        .withEnergyLevelCritical(15)
        .withEnergyLevelGood(60)
        .withState(Vehicle.State.IDLE);
    VehicleOrderSelection result = instance.selectOrder(vehicle);
    assertNull(result);
  }

  @Test
  @Parameters({"ERROR", "EXECUTING", "UNAVAILABLE", "UNKNOWN"})
  public void returnUnassignableForDispatchableVehicleWithState(Vehicle.State state) {
    Vehicle vehicle = new Vehicle("TestVehicle")
        .withEnergyLevel(10)
        .withEnergyLevelCritical(15)
        .withEnergyLevelGood(60)
        .withState(state);
    VehicleOrderSelection result = instance.selectOrder(vehicle);
    assertNotNull(result);
    assertFalse(result.isAssignable());
  }

  @Test
  public void returnUnassignableForChargingDegradedVehicle() {
    Vehicle vehicle = new Vehicle("TestVehicle")
        .withEnergyLevel(50)
        .withEnergyLevelCritical(15)
        .withEnergyLevelGood(50)
        .withState(Vehicle.State.CHARGING);
    VehicleOrderSelection result = instance.selectOrder(vehicle);
    assertNotNull(result);
    assertFalse(result.isAssignable());
  }

}
