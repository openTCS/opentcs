/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.strategies.basic.dispatching.selection.vehicles;

import java.util.ArrayList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import org.opentcs.components.kernel.services.TCSObjectService;
import org.opentcs.data.TCSObjectReference;
import org.opentcs.data.model.Point;
import org.opentcs.data.model.Vehicle;
import org.opentcs.data.order.OrderSequence;
import org.opentcs.data.order.TransportOrder;
import org.opentcs.strategies.basic.dispatching.DefaultDispatcherConfiguration;
import org.opentcs.strategies.basic.dispatching.OrderReservationPool;

/**
 * Test for {@link IsAvailableForAnyOrder}.
 */
public class IsAvailableForAnyOrderTest {

  private IsAvailableForAnyOrder isAvailableForAnyOrder;
  private Vehicle vehicleAvailableForAnyOrder;
  private TransportOrder transportOrder;
  private OrderReservationPool orderReservationPool;
  private List<TCSObjectReference<TransportOrder>> reservationsList;

  @BeforeEach
  public void setUp() {
    TCSObjectService objectService = mock();
    DefaultDispatcherConfiguration configuration = mock();
    orderReservationPool = mock();

    isAvailableForAnyOrder = new IsAvailableForAnyOrder(objectService,
                                                        orderReservationPool,
                                                        configuration);

    transportOrder = new TransportOrder("T1", List.of())
        .withDispensable(false);

    vehicleAvailableForAnyOrder = new Vehicle("V1")
        .withIntegrationLevel(Vehicle.IntegrationLevel.TO_BE_UTILIZED)
        .withCurrentPosition(new Point("p1").getReference())
        .withState(Vehicle.State.IDLE)
        .withProcState(Vehicle.ProcState.IDLE);

    given(objectService.fetchObject(TransportOrder.class, transportOrder.getReference()))
        .willReturn(transportOrder);

    reservationsList = new ArrayList<>();
    given(orderReservationPool.findReservations(any()))
        .willReturn(reservationsList);
  }

  @Test
  public void checkVehicleIsAvailable() {
    Vehicle vehicle = vehicleAvailableForAnyOrder;

    assertTrue(isAvailableForAnyOrder.test(vehicle));
  }

  @Test
  public void checkVehicleIsPaused() {
    Vehicle vehicle = vehicleAvailableForAnyOrder.withPaused(true);

    assertFalse(isAvailableForAnyOrder.test(vehicle));
  }

  @ParameterizedTest
  @EnumSource(value = Vehicle.IntegrationLevel.class,
              names = {"TO_BE_IGNORED", "TO_BE_NOTICED", "TO_BE_RESPECTED"})
  public void checkVehicleIsNotFullyIntegrated(Vehicle.IntegrationLevel integrationLevel) {
    Vehicle vehicle = vehicleAvailableForAnyOrder.withIntegrationLevel(integrationLevel);

    assertFalse(isAvailableForAnyOrder.test(vehicle));
  }

  @Test
  public void checkVehicleHasNoPosition() {
    Vehicle vehicle = vehicleAvailableForAnyOrder.withCurrentPosition(null);

    assertFalse(isAvailableForAnyOrder.test(vehicle));
  }

  @Test
  public void checkVehicleHasOrderSequence() {
    Vehicle vehicle = vehicleAvailableForAnyOrder
        .withOrderSequence(new OrderSequence("OS").getReference());

    assertFalse(isAvailableForAnyOrder.test(vehicle));
  }

  @Test
  public void checkVehicleHasCriticalEnergyLevel() {
    Vehicle vehicle = vehicleAvailableForAnyOrder.withEnergyLevel(0);

    assertFalse(isAvailableForAnyOrder.test(vehicle));
  }

  @Test
  public void checkVehicleNeedsMoreCharging() {
    Vehicle vehicle = vehicleAvailableForAnyOrder
        .withEnergyLevel(10)
        .withState(Vehicle.State.CHARGING);

    assertFalse(isAvailableForAnyOrder.test(vehicle));
  }

  @Test
  public void checkVehicleProcessesOrderThatIsNotDispensable() {
    Vehicle vehicle = vehicleAvailableForAnyOrder
        .withProcState(Vehicle.ProcState.PROCESSING_ORDER)
        .withTransportOrder(transportOrder.getReference());

    assertFalse(isAvailableForAnyOrder.test(vehicle));
  }

  @Test
  public void checkVehicleHasOrderReservation() {
    Vehicle vehicle = vehicleAvailableForAnyOrder;

    reservationsList.add(transportOrder.getReference());

    assertFalse(isAvailableForAnyOrder.test(vehicle));
  }
}
