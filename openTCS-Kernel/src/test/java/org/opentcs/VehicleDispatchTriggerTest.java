/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.opentcs.common.SameThreadExecutorService;
import org.opentcs.components.kernel.services.DispatcherService;
import org.opentcs.data.TCSObjectEvent;
import org.opentcs.data.model.Vehicle;
import org.opentcs.data.order.OrderSequence;
import org.opentcs.data.order.ReroutingType;
import org.opentcs.kernel.KernelApplicationConfiguration;
import org.opentcs.kernel.VehicleDispatchTrigger;
import org.opentcs.util.event.EventBus;

/**
 * Unit tests for {@link VehicleDispatchTrigger}.
 */
public class VehicleDispatchTriggerTest {

  private EventBus eventBus;
  private KernelApplicationConfiguration config;
  private DispatcherService dispatcher;

  private VehicleDispatchTrigger trigger;

  @BeforeEach
  public void setUp() {
    eventBus = mock(EventBus.class);
    dispatcher = mock(DispatcherService.class);
    config = mock(KernelApplicationConfiguration.class);
    when(config.rerouteOnDriveOrderFinished()).thenReturn(false);
    trigger = new VehicleDispatchTrigger(
        new SameThreadExecutorService(),
        eventBus,
        dispatcher,
        config);
  }

  @Test
  void dispatchWhenIdleAndEnergyLevelChanged() {
    Vehicle vehicleOld = new Vehicle("someVehicle")
        .withIntegrationLevel(Vehicle.IntegrationLevel.TO_BE_UTILIZED)
        .withProcState(Vehicle.ProcState.IDLE)
        .withState(Vehicle.State.IDLE)
        .withEnergyLevel(100);
    Vehicle vehicleNew = vehicleOld.withEnergyLevel(99);

    trigger.onEvent(new TCSObjectEvent(vehicleNew,
                                       vehicleOld,
                                       TCSObjectEvent.Type.OBJECT_MODIFIED));

    verify(dispatcher).dispatch();
  }

  @Test
  void noDispatchWhenNotIdleAndEnergyLevelChanged() {
    Vehicle vehicleOld = new Vehicle("someVehicle")
        .withIntegrationLevel(Vehicle.IntegrationLevel.TO_BE_UTILIZED)
        .withProcState(Vehicle.ProcState.PROCESSING_ORDER)
        .withState(Vehicle.State.EXECUTING)
        .withEnergyLevel(100);
    Vehicle vehicleNew = vehicleOld.withEnergyLevel(99);

    trigger.onEvent(new TCSObjectEvent(vehicleNew,
                                       vehicleOld,
                                       TCSObjectEvent.Type.OBJECT_MODIFIED));

    verify(dispatcher, never()).dispatch();
  }

  @Test
  void dispatchWhenProcStateBecameIdle() {
    Vehicle vehicleOld = new Vehicle("someVehicle")
        .withIntegrationLevel(Vehicle.IntegrationLevel.TO_BE_UTILIZED)
        .withProcState(Vehicle.ProcState.PROCESSING_ORDER)
        .withState(Vehicle.State.EXECUTING);
    Vehicle vehicleNew = vehicleOld.withProcState(Vehicle.ProcState.IDLE)
        .withState(Vehicle.State.IDLE);

    trigger.onEvent(new TCSObjectEvent(vehicleNew,
                                       vehicleOld,
                                       TCSObjectEvent.Type.OBJECT_MODIFIED));

    verify(dispatcher).dispatch();
  }

  @Test
  void dispatchWhenProcStateBecameAwaitingOrder() {
    Vehicle vehicleOld = new Vehicle("someVehicle")
        .withIntegrationLevel(Vehicle.IntegrationLevel.TO_BE_UTILIZED)
        .withProcState(Vehicle.ProcState.PROCESSING_ORDER);
    Vehicle vehicleNew = vehicleOld.withProcState(Vehicle.ProcState.AWAITING_ORDER);

    trigger.onEvent(new TCSObjectEvent(vehicleNew,
                                       vehicleOld,
                                       TCSObjectEvent.Type.OBJECT_MODIFIED));

    verify(dispatcher).dispatch();
  }

  @Test
  void dispatchWhenOrderSequenceNulled() {
    Vehicle vehicleOld = new Vehicle("someVehicle")
        .withIntegrationLevel(Vehicle.IntegrationLevel.TO_BE_UTILIZED)
        .withOrderSequence(new OrderSequence("someSequence").getReference());
    Vehicle vehicleNew = vehicleOld.withOrderSequence(null);

    trigger.onEvent(new TCSObjectEvent(vehicleNew,
                                       vehicleOld,
                                       TCSObjectEvent.Type.OBJECT_MODIFIED));

    verify(dispatcher).dispatch();
  }

  @Test
  public void rerouteWhenProcStateBecameAwaitingOrder() {
    Vehicle vehicleOld = new Vehicle("someVehicle")
        .withIntegrationLevel(Vehicle.IntegrationLevel.TO_BE_UTILIZED)
        .withProcState(Vehicle.ProcState.PROCESSING_ORDER);
    Vehicle vehicleNew = vehicleOld.withProcState(Vehicle.ProcState.AWAITING_ORDER);

    when(config.rerouteOnDriveOrderFinished()).thenReturn(true);

    trigger.onEvent(new TCSObjectEvent(vehicleNew,
                                       vehicleOld,
                                       TCSObjectEvent.Type.OBJECT_MODIFIED));

    verify(dispatcher).dispatch();
    verify(dispatcher).reroute(vehicleNew.getReference(), ReroutingType.REGULAR);
  }

}
