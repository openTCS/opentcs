/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.kernel.extensions.servicewebapi.v1;

import java.util.List;
import java.util.concurrent.Executors;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;
import org.opentcs.components.kernel.services.DispatcherService;
import org.opentcs.components.kernel.services.VehicleService;
import org.opentcs.data.ObjectUnknownException;
import org.opentcs.data.model.Vehicle;
import org.opentcs.data.order.ReroutingType;
import org.opentcs.data.order.TransportOrder;
import org.opentcs.kernel.extensions.servicewebapi.KernelExecutorWrapper;

/**
 * Unit tests for {@link TransportOrderDispatcherHandler}.
 */
class TransportOrderDispatcherHandlerTest {

  private VehicleService vehicleService;
  private DispatcherService dispatcherService;
  private KernelExecutorWrapper executorWrapper;
  private TransportOrderDispatcherHandler handler;

  private Vehicle vehicle;
  private TransportOrder order;

  @BeforeEach
  void setUp() {
    vehicleService = mock();
    dispatcherService = mock();
    executorWrapper = new KernelExecutorWrapper(Executors.newSingleThreadExecutor());

    handler = new TransportOrderDispatcherHandler(vehicleService,
                                                  dispatcherService,
                                                  executorWrapper);

    vehicle = new Vehicle("some-vehicle");
    order = new TransportOrder("some-order", List.of())
        .withProcessingVehicle(vehicle.getReference());

    given(vehicleService.fetchObject(Vehicle.class, "some-vehicle"))
        .willReturn(vehicle);
    given(vehicleService.fetchObject(TransportOrder.class, "some-order"))
        .willReturn(order);
  }

  @Test
  void triggerDispatcher() {
    handler.triggerDispatcher();

    then(dispatcherService).should().dispatch();
  }

  @Test
  void tryImmediateAssignmentUsingKnownOrder() {
    handler.tryImmediateAssignment("some-order");

    then(dispatcherService).should().assignNow(order.getReference());
  }

  @Test
  void throwOnImmediateAssignmentUsingUnknownOrderName() {
    assertThatExceptionOfType(ObjectUnknownException.class)
        .isThrownBy(() -> handler.tryImmediateAssignment("some-unknown-order"));
  }

  @Test
  void withdrawByTransportOrderRegularly() {
    handler.withdrawByTransportOrder("some-order", false, false);

    then(dispatcherService).should().withdrawByTransportOrder(order.getReference(), false);
  }

  @Test
  void withdrawByTransportOrderImmediately() {
    handler.withdrawByTransportOrder("some-order", true, false);

    then(dispatcherService).should().withdrawByTransportOrder(order.getReference(), true);
  }

  @Test
  void withdrawByTransportOrderAlsoDisablingVehicle() {
    handler.withdrawByTransportOrder("some-order", false, true);

    then(vehicleService)
        .should()
        .updateVehicleIntegrationLevel(vehicle.getReference(),
                                       Vehicle.IntegrationLevel.TO_BE_RESPECTED);
    then(dispatcherService).should().withdrawByTransportOrder(order.getReference(), false);
  }

  @Test
  void throwOnWithdrawUsingUnknownOrderName() {
    assertThatExceptionOfType(ObjectUnknownException.class)
        .isThrownBy(() -> handler.withdrawByTransportOrder("some-unknown-order", false, false));
  }

  @Test
  void withdrawByVehicleRegularly() {
    handler.withdrawByVehicle("some-vehicle", false, false);

    then(dispatcherService).should().withdrawByVehicle(vehicle.getReference(), false);
  }

  @Test
  void withdrawByVehicleImmediately() {
    handler.withdrawByVehicle("some-vehicle", true, false);

    then(dispatcherService).should().withdrawByVehicle(vehicle.getReference(), true);
  }

  @Test
  void withdrawByVehicleAlsoDisablingVehicle() {
    handler.withdrawByVehicle("some-vehicle", false, true);

    then(vehicleService)
        .should()
        .updateVehicleIntegrationLevel(vehicle.getReference(),
                                       Vehicle.IntegrationLevel.TO_BE_RESPECTED);
    then(dispatcherService).should().withdrawByVehicle(vehicle.getReference(), false);
  }

  @Test
  void throwOnWithdrawUsingUnknownVehicleName() {
    assertThatExceptionOfType(ObjectUnknownException.class)
        .isThrownBy(() -> handler.withdrawByVehicle("some-unknown-vehicle", false, false));
  }

  @Test
  void rerouteRegularly() {
    handler.reroute("some-vehicle", false);

    then(dispatcherService).should().reroute(vehicle.getReference(), ReroutingType.REGULAR);
  }

  @Test
  void rerouteForcibly() {
    handler.reroute("some-vehicle", true);

    then(dispatcherService).should().reroute(vehicle.getReference(), ReroutingType.FORCED);
  }

  @Test
  void throwOnRerouteUsingUnknownVehicleName() {
    assertThatExceptionOfType(ObjectUnknownException.class)
        .isThrownBy(() -> handler.reroute("some-unknown-vehicle", false));
  }
}
