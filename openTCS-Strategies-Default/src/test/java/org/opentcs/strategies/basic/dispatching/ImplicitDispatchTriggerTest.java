/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.strategies.basic.dispatching;

import org.junit.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import org.opentcs.components.kernel.Dispatcher;
import org.opentcs.data.TCSObjectEvent;
import org.opentcs.data.model.Vehicle;
import org.opentcs.data.order.OrderSequence;

/**
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public class ImplicitDispatchTriggerTest {

  private Dispatcher dispatcher;

  private ImplicitDispatchTrigger listener;

  @Before
  public void setUp() {
    dispatcher = mock(Dispatcher.class);
    listener = new ImplicitDispatchTrigger(dispatcher);
  }

  @Test
  public void dispatchWhenIdleAndEnergyLevelChanged() {
    Vehicle vehicleOld = new Vehicle("someVehicle")
        .withIntegrationLevel(Vehicle.IntegrationLevel.TO_BE_UTILIZED)
        .withProcState(Vehicle.ProcState.IDLE)
        .withState(Vehicle.State.IDLE)
        .withEnergyLevel(100);
    Vehicle vehicleNew = vehicleOld.withEnergyLevel(99);

    listener.onEvent(new TCSObjectEvent(vehicleNew,
                                        vehicleOld,
                                        TCSObjectEvent.Type.OBJECT_MODIFIED));

    verify(dispatcher).dispatch();
  }

  @Test
  public void noDispatchWhenNotIdleAndEnergyLevelChanged() {
    Vehicle vehicleOld = new Vehicle("someVehicle")
        .withIntegrationLevel(Vehicle.IntegrationLevel.TO_BE_UTILIZED)
        .withProcState(Vehicle.ProcState.PROCESSING_ORDER)
        .withState(Vehicle.State.EXECUTING)
        .withEnergyLevel(100);
    Vehicle vehicleNew = vehicleOld.withEnergyLevel(99);

    listener.onEvent(new TCSObjectEvent(vehicleNew,
                                        vehicleOld,
                                        TCSObjectEvent.Type.OBJECT_MODIFIED));

    verify(dispatcher, never()).dispatch();
  }

  @Test
  public void dispatchWhenProcStateBecameIdle() {
    Vehicle vehicleOld = new Vehicle("someVehicle")
        .withIntegrationLevel(Vehicle.IntegrationLevel.TO_BE_UTILIZED)
        .withProcState(Vehicle.ProcState.PROCESSING_ORDER)
        .withState(Vehicle.State.EXECUTING);
    Vehicle vehicleNew = vehicleOld.withProcState(Vehicle.ProcState.IDLE)
        .withState(Vehicle.State.IDLE);

    listener.onEvent(new TCSObjectEvent(vehicleNew,
                                        vehicleOld,
                                        TCSObjectEvent.Type.OBJECT_MODIFIED));

    verify(dispatcher).dispatch();
  }

  @Test
  public void dispatchWhenProcStateBecameAwaitingOrder() {
    Vehicle vehicleOld = new Vehicle("someVehicle")
        .withIntegrationLevel(Vehicle.IntegrationLevel.TO_BE_UTILIZED)
        .withProcState(Vehicle.ProcState.PROCESSING_ORDER);
    Vehicle vehicleNew = vehicleOld.withProcState(Vehicle.ProcState.AWAITING_ORDER);

    listener.onEvent(new TCSObjectEvent(vehicleNew,
                                        vehicleOld,
                                        TCSObjectEvent.Type.OBJECT_MODIFIED));

    verify(dispatcher).dispatch();
  }

  @Test
  public void dispatchWhenOrderSequenceNulled() {
    Vehicle vehicleOld = new Vehicle("someVehicle")
        .withIntegrationLevel(Vehicle.IntegrationLevel.TO_BE_UTILIZED)
        .withOrderSequence(new OrderSequence("someSequence").getReference());
    Vehicle vehicleNew = vehicleOld.withOrderSequence(null);

    listener.onEvent(new TCSObjectEvent(vehicleNew,
                                        vehicleOld,
                                        TCSObjectEvent.Type.OBJECT_MODIFIED));

    verify(dispatcher).dispatch();
  }

}
