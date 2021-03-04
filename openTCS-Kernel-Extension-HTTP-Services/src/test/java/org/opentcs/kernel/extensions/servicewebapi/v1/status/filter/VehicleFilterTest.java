/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.kernel.extensions.servicewebapi.v1.status.filter;

import org.junit.*;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.opentcs.data.model.Vehicle;

/**
 *
 * @author Mustafa Yalciner (Fraunhofer IML)
 */
public class VehicleFilterTest {

  private Vehicle vehicle1;
  private Vehicle vehicle2;
  private Vehicle vehicle3;
  private Vehicle vehicle4;

  public VehicleFilterTest() {
  }

  @Before
  public void setUp() {
    vehicle1 = new Vehicle("Vehicle-001")
        .withProcState(Vehicle.ProcState.IDLE);

    vehicle2 = new Vehicle("Vehicle-002")
        .withProcState(Vehicle.ProcState.PROCESSING_ORDER);

    vehicle3 = new Vehicle("Vehicle-003")
        .withProcState(Vehicle.ProcState.AWAITING_ORDER);

    vehicle4 = new Vehicle("Vehicle-004")
        .withProcState(Vehicle.ProcState.UNAVAILABLE);
  }

  @Test
  public void acceptsAllForNoParams() {
    VehicleFilter emptyFilter = new VehicleFilter(null);
    assertTrue(emptyFilter.test(vehicle1));
    assertTrue(emptyFilter.test(vehicle2));
    assertTrue(emptyFilter.test(vehicle3));
    assertTrue(emptyFilter.test(vehicle4));
  }

  @Test
  public void detectsIdleVehicles() {
    VehicleFilter idleVehiclesFilter = new VehicleFilter("IDLE");
    assertTrue(idleVehiclesFilter.test(vehicle1));
    assertFalse(idleVehiclesFilter.test(vehicle2));
    assertFalse(idleVehiclesFilter.test(vehicle3));
    assertFalse(idleVehiclesFilter.test(vehicle4));
  }
}
