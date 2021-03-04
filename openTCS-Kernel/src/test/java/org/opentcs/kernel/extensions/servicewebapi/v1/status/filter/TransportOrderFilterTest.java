/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.kernel.extensions.servicewebapi.v1.status.filter;

import java.util.LinkedList;
import org.junit.*;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.opentcs.data.TCSObjectReference;
import org.opentcs.data.model.Vehicle;
import org.opentcs.data.order.TransportOrder;

/**
 *
 * @author Mustafa Yalciner (Fraunhofer IML)
 */
public class TransportOrderFilterTest {

  private TransportOrder transportOrder1;
  private TransportOrder transportOrder2;
  private TransportOrder transportOrder3;
  private TransportOrder transportOrder4;
  private TransportOrder transportOrder5;
  private TCSObjectReference<Vehicle> vehicle1Reference;

  public TransportOrderFilterTest() {
  }

  @Before
  public void setUp() {
    vehicle1Reference = new Vehicle("Vehicle-001").getReference();
    transportOrder1 = new TransportOrder("TransportOrder-001", new LinkedList<>())
        .withIntendedVehicle(vehicle1Reference);
    transportOrder2 = new TransportOrder("TransportOrder-002", new LinkedList<>())
        .withIntendedVehicle(vehicle1Reference);
    transportOrder3 = new TransportOrder("TransportOrder-003", new LinkedList<>())
        .withIntendedVehicle(new Vehicle("Vehicle-002").getReference());
    transportOrder4 = new TransportOrder("TransportOrder-004", new LinkedList<>())
        .withIntendedVehicle(new Vehicle("Vehicle-003").getReference());
    transportOrder5 = new TransportOrder("TransportOrder-005", new LinkedList<>())
        .withIntendedVehicle(new Vehicle("Vehicle-004").getReference());
  }

  @Test
  public void acceptsAllForNoParams() {
    TransportOrderFilter emptyFilter = new TransportOrderFilter(null);

    assertTrue(emptyFilter.test(transportOrder1));
    assertTrue(emptyFilter.test(transportOrder2));
    assertTrue(emptyFilter.test(transportOrder3));
    assertTrue(emptyFilter.test(transportOrder4));
    assertTrue(emptyFilter.test(transportOrder5));

  }

  @Test
  public void detectsIntendedVehicle1() {
    TransportOrderFilter intendedVehicleFilter
        = new TransportOrderFilter(vehicle1Reference.getName());

    assertTrue(intendedVehicleFilter.test(transportOrder1));
    assertTrue(intendedVehicleFilter.test(transportOrder2));
    assertFalse(intendedVehicleFilter.test(transportOrder3));
    assertFalse(intendedVehicleFilter.test(transportOrder4));
    assertFalse(intendedVehicleFilter.test(transportOrder5));
  }

}
