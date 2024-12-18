// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.strategies.basic.dispatching.phase.assignment;

import static java.util.Objects.requireNonNull;

import jakarta.inject.Inject;
import org.opentcs.components.kernel.Router;
import org.opentcs.components.kernel.services.InternalTransportOrderService;
import org.opentcs.components.kernel.services.InternalVehicleService;
import org.opentcs.data.model.Vehicle;
import org.opentcs.data.order.TransportOrder;
import org.opentcs.drivers.vehicle.VehicleControllerPool;
import org.opentcs.strategies.basic.dispatching.Phase;
import org.opentcs.strategies.basic.dispatching.TransportOrderUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Assigns the next drive order to each vehicle waiting for it, or finishes the respective transport
 * order if the vehicle has finished its last drive order.
 */
public class AssignNextDriveOrdersPhase
    implements
      Phase {

  /**
   * This class's Logger.
   */
  private static final Logger LOG = LoggerFactory.getLogger(AssignNextDriveOrdersPhase.class);
  private final InternalTransportOrderService transportOrderService;
  private final InternalVehicleService vehicleService;
  /**
   * The Router instance calculating route costs.
   */
  private final Router router;
  /**
   * The vehicle controller pool.
   */
  private final VehicleControllerPool vehicleControllerPool;
  private final TransportOrderUtil transportOrderUtil;
  /**
   * Indicates whether this component is initialized.
   */
  private boolean initialized;

  @Inject
  public AssignNextDriveOrdersPhase(
      InternalTransportOrderService transportOrderService,
      InternalVehicleService vehicleService,
      Router router,
      VehicleControllerPool vehicleControllerPool,
      TransportOrderUtil transportOrderUtil
  ) {
    this.transportOrderService = requireNonNull(transportOrderService, "transportOrderService");
    this.vehicleService = requireNonNull(vehicleService, "vehicleService");
    this.router = requireNonNull(router, "router");
    this.vehicleControllerPool = requireNonNull(vehicleControllerPool, "vehicleControllerPool");
    this.transportOrderUtil = requireNonNull(transportOrderUtil, "transportOrderUtil");
  }

  @Override
  public void initialize() {
    if (isInitialized()) {
      return;
    }
    initialized = true;
  }

  @Override
  public boolean isInitialized() {
    return initialized;
  }

  @Override
  public void terminate() {
    if (!isInitialized()) {
      return;
    }
    initialized = false;
  }

  @Override
  public void run() {
    transportOrderService.fetchObjects(Vehicle.class).stream()
        .filter(vehicle -> vehicle.hasProcState(Vehicle.ProcState.AWAITING_ORDER))
        .forEach(vehicle -> checkForNextDriveOrder(vehicle));
  }

  private void checkForNextDriveOrder(Vehicle vehicle) {
    LOG.debug("Vehicle '{}' finished a drive order.", vehicle.getName());
    // The vehicle is processing a transport order and has finished a drive order.
    // See if there's another drive order to be processed.
    transportOrderService.updateTransportOrderNextDriveOrder(vehicle.getTransportOrder());
    TransportOrder vehicleOrder = transportOrderService.fetchObject(
        TransportOrder.class,
        vehicle.getTransportOrder()
    );
    if (vehicleOrder.getCurrentDriveOrder() == null) {
      LOG.debug(
          "Vehicle '{}' finished transport order '{}'",
          vehicle.getName(),
          vehicleOrder.getName()
      );
      // The current transport order has been finished - update its state and that of the vehicle.
      transportOrderUtil.updateTransportOrderState(
          vehicle.getTransportOrder(),
          TransportOrder.State.FINISHED
      );
      // Update the vehicle's procState, implicitly dispatching it again.
      vehicleService.updateVehicleProcState(vehicle.getReference(), Vehicle.ProcState.IDLE);
      vehicleService.updateVehicleTransportOrder(vehicle.getReference(), null);
      // Update transport orders that are dispatchable now that this one has been finished.
      transportOrderUtil.markNewDispatchableOrders();
    }
    else {
      LOG.debug("Assigning next drive order to vehicle '{}'...", vehicle.getName());
      if (transportOrderUtil.mustAssign(vehicleOrder.getCurrentDriveOrder(), vehicle)) {
        // Get an up-to-date copy of the transport order in case the route changed.
        vehicleOrder = transportOrderService.fetchObject(
            TransportOrder.class,
            vehicle.getTransportOrder()
        );

        // Let the vehicle controller know about the new drive order.
        vehicleControllerPool.getVehicleController(vehicle.getName())
            .setTransportOrder(vehicleOrder);

        // The vehicle is still processing a transport order.
        vehicleService.updateVehicleProcState(
            vehicle.getReference(),
            Vehicle.ProcState.PROCESSING_ORDER
        );
      }
      // If the drive order need not be assigned, immediately check for another one.
      else {
        vehicleService.updateVehicleProcState(
            vehicle.getReference(),
            Vehicle.ProcState.AWAITING_ORDER
        );
        checkForNextDriveOrder(vehicle);
      }
    }
  }
}
