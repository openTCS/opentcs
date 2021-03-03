/*
 * openTCS copyright information:
 * Copyright (c) 2014 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.strategies.basic.recovery;

import java.util.Collections;
import java.util.Set;
import org.junit.*;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.opentcs.access.LocalKernel;
import org.opentcs.data.model.Location;
import org.opentcs.data.model.LocationType;
import org.opentcs.data.model.Vehicle;
import org.opentcs.data.order.DriveOrder;
import org.opentcs.data.order.TransportOrder;

/**
 * Test for the NESRecoveryEvaluator.
 * Only the well-defined behaviour in some extremal values is tested.
 * Anything in between is too fuzzy as the recovery status depends on the
 * internal threshold value and the sum of the squares of alle vehicle's energy
 * levels.
 *
 * @author Tobias Marquardt (Fraunhofer IML)
 */
public class NESRecoveryEvaluatorTest {

  /**
   * The kernel used by the evaluator.
   */
  private LocalKernel kernel;
  /**
   * The evaluator instance which is tested.
   */
  private DefaultRecoveryEvaluator evaluator;

  @Before
  public void setUp() {
    kernel = mock(LocalKernel.class);
    evaluator = new DefaultRecoveryEvaluator(kernel, 0.7);
  }

  /**
   * All orders are finished and all vehicles have full energy.
   */
  @Test
  public void testRecoveredWhenFullEnergyAndAllOrdersFinished() {
    Vehicle vehicle = new Vehicle(1, "Test vehicle");
    vehicle.setEnergyLevel(100);
    when(kernel.getTCSObjects(Vehicle.class)).thenReturn(Collections.singleton(vehicle));

    LocationType locType = new LocationType(2, "Test location type");
    Location location = new Location(3, "Test location", locType.getReference());
    DriveOrder.Destination destination
        = new DriveOrder.Destination(location.getReference(),
                                     DriveOrder.Destination.OP_NOP);
    TransportOrder order
        = new TransportOrder(2,
                             "Test order",
                             Collections.singletonList(destination),
                             System.currentTimeMillis());
    order.setState(TransportOrder.State.FINISHED);
    when(kernel.getTCSObjects(TransportOrder.class))
        .thenReturn(Collections.singleton(order));

    assertTrue(evaluator.evaluateRecovery().isRecovered());
  }

  /**
   * There are no orders and all vehicles have full energy.
   */
  @Test
  public void testRecoveredWhenFullEnergyAndNoOrders() {
    Vehicle vehicle = new Vehicle(1, "Test vehicle");
    vehicle.setEnergyLevel(100);
    when(kernel.getTCSObjects(Vehicle.class)).thenReturn(Collections.singleton(vehicle));

    assertTrue(evaluator.evaluateRecovery().isRecovered());
  }

  /**
   * All vehicles have no energy.
   */
  @Test
  public void testNotRecoveredWhenNoEnergy() {
    Vehicle vehicle = new Vehicle(1, "Test vehicle");
    vehicle.setEnergyLevel(0);
    when(kernel.getTCSObjects(Vehicle.class)).thenReturn(Collections.singleton(vehicle));

    assertFalse(evaluator.evaluateRecovery().isRecovered());
  }

  /**
   * No vehicles in model.
   */
  @Test
  public void testNotRecoveredWhenNoVehicles() {
    Set<Vehicle> noVehicle = Collections.emptySet();
    when(kernel.getTCSObjects(Vehicle.class)).thenReturn(noVehicle);

    assertFalse(evaluator.evaluateRecovery().isRecovered());
  }

  /**
   * There are unfinished orders.
   */
  @Test
  public void testNotRecoveredWhenUnfinishedOrders() {
    Vehicle vehicle = new Vehicle(1, "Test vehicle");
    vehicle.setEnergyLevel(100);
    when(kernel.getTCSObjects(Vehicle.class)).thenReturn(Collections.singleton(vehicle));

    LocationType locType = new LocationType(2, "Test location type");
    Location location = new Location(3, "Test location", locType.getReference());
    DriveOrder.Destination destination
        = new DriveOrder.Destination(location.getReference(),
                                     DriveOrder.Destination.OP_NOP);
    TransportOrder order
        = new TransportOrder(2,
                             "Test order",
                             Collections.singletonList(destination),
                             System.currentTimeMillis());
    order.setState(TransportOrder.State.DISPATCHABLE);
    when(kernel.getTCSObjects(TransportOrder.class))
        .thenReturn(Collections.singleton(order));

    assertFalse(evaluator.evaluateRecovery().isRecovered());
  }
}
