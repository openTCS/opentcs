/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.kernel.extensions.servicewebapi.v1;

import static java.util.Objects.requireNonNull;
import javax.inject.Inject;
import org.opentcs.components.kernel.services.DispatcherService;
import org.opentcs.components.kernel.services.VehicleService;
import org.opentcs.data.ObjectUnknownException;
import org.opentcs.data.model.Vehicle;
import org.opentcs.data.order.ReroutingType;
import org.opentcs.data.order.TransportOrder;
import org.opentcs.kernel.extensions.servicewebapi.KernelExecutorWrapper;

/**
 * Handles requests related to transport order dispatching.
 */
public class TransportOrderDispatcherHandler {

  private final VehicleService vehicleService;
  private final DispatcherService dispatcherService;
  private final KernelExecutorWrapper executorWrapper;

  /**
   * Creates a new instance.
   *
   * @param vehicleService Used to update vehicle state.
   * @param dispatcherService Used to withdraw transport orders.
   * @param executorWrapper Executes calls via the kernel executor and waits for the outcome.
   */
  @Inject
  public TransportOrderDispatcherHandler(VehicleService vehicleService,
                                         DispatcherService dispatcherService,
                                         KernelExecutorWrapper executorWrapper) {
    this.vehicleService = requireNonNull(vehicleService, "vehicleService");
    this.dispatcherService = requireNonNull(dispatcherService, "dispatcherService");
    this.executorWrapper = requireNonNull(executorWrapper, "executorWrapper");
  }

  public void triggerDispatcher() {
    executorWrapper.callAndWait(() -> dispatcherService.dispatch());
  }

  public void tryImmediateAssignment(String name)
      throws ObjectUnknownException, IllegalArgumentException {
    requireNonNull(name, "name");

    executorWrapper.callAndWait(() -> {
      TransportOrder order = vehicleService.fetchObject(TransportOrder.class, name);
      if (order == null) {
        throw new ObjectUnknownException("Unknown transport order: " + name);
      }

      dispatcherService.assignNow(order.getReference());
    });
  }

  public void withdrawByTransportOrder(String name, boolean immediate, boolean disableVehicle)
      throws ObjectUnknownException {
    requireNonNull(name, "name");

    executorWrapper.callAndWait(() -> {
      if (vehicleService.fetchObject(TransportOrder.class, name) == null) {
        throw new ObjectUnknownException("Unknown transport order: " + name);
      }

      TransportOrder order = vehicleService.fetchObject(TransportOrder.class, name);
      if (disableVehicle && order.getProcessingVehicle() != null) {
        vehicleService.updateVehicleIntegrationLevel(order.getProcessingVehicle(),
                                                     Vehicle.IntegrationLevel.TO_BE_RESPECTED);
      }

      dispatcherService.withdrawByTransportOrder(order.getReference(), immediate);
    });
  }

  public void withdrawByVehicle(String name, boolean immediate, boolean disableVehicle)
      throws ObjectUnknownException {
    requireNonNull(name, "name");

    executorWrapper.callAndWait(() -> {
      Vehicle vehicle = vehicleService.fetchObject(Vehicle.class, name);
      if (vehicle == null) {
        throw new ObjectUnknownException("Unknown vehicle: " + name);
      }

      if (disableVehicle) {
        vehicleService.updateVehicleIntegrationLevel(vehicle.getReference(),
                                                     Vehicle.IntegrationLevel.TO_BE_RESPECTED);
      }

      dispatcherService.withdrawByVehicle(vehicle.getReference(), immediate);
    });
  }

  public void reroute(String vehicleName, boolean forced)
      throws ObjectUnknownException {
    requireNonNull(vehicleName, "vehicleName");

    executorWrapper.callAndWait(() -> {
      Vehicle vehicle = vehicleService.fetchObject(Vehicle.class, vehicleName);
      if (vehicle == null) {
        throw new ObjectUnknownException("Unknown vehicle: " + vehicleName);
      }

      dispatcherService.reroute(
          vehicle.getReference(),
          forced ? ReroutingType.FORCED : ReroutingType.REGULAR
      );
    });
  }

}
