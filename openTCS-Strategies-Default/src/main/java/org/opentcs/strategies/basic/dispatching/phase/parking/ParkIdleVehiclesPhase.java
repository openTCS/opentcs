/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.strategies.basic.dispatching.phase.parking;

import java.util.Arrays;
import java.util.List;
import static java.util.Objects.requireNonNull;
import java.util.Optional;
import java.util.UUID;
import javax.inject.Inject;
import org.opentcs.access.to.order.DestinationCreationTO;
import org.opentcs.access.to.order.TransportOrderCreationTO;
import org.opentcs.components.kernel.Router;
import org.opentcs.components.kernel.services.InternalTransportOrderService;
import org.opentcs.data.model.Point;
import org.opentcs.data.model.Vehicle;
import org.opentcs.data.order.DriveOrder;
import org.opentcs.data.order.TransportOrder;
import org.opentcs.strategies.basic.dispatching.DefaultDispatcherConfiguration;
import org.opentcs.strategies.basic.dispatching.Phase;
import org.opentcs.strategies.basic.dispatching.ProcessabilityChecker;
import org.opentcs.strategies.basic.dispatching.TransportOrderUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Creates parking orders for idle vehicles not already at a parking position.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
@SuppressWarnings("deprecation")
public class ParkIdleVehiclesPhase
    implements Phase {

  /**
   * This class's Logger.
   */
  private static final Logger LOG = LoggerFactory.getLogger(ParkIdleVehiclesPhase.class);
  /**
   * The transport order service.
   */
  private final InternalTransportOrderService orderService;
  /**
   * The strategy used for finding suitable parking positions.
   */
  private final org.opentcs.components.kernel.ParkingPositionSupplier parkingPosSupplier;
  /**
   * The Router instance calculating route costs.
   */
  private final Router router;
  /**
   * Checks processability of transport orders for vehicles.
   */
  private final ProcessabilityChecker processabilityChecker;

  private final TransportOrderUtil transportOrderUtil;
  /**
   * The dispatcher configuration.
   */
  private final DefaultDispatcherConfiguration configuration;
  /**
   * Indicates whether this component is initialized.
   */
  private boolean initialized;

  @Inject
  public ParkIdleVehiclesPhase(
      InternalTransportOrderService orderService,
      org.opentcs.components.kernel.ParkingPositionSupplier parkingPosSupplier,
      Router router,
      ProcessabilityChecker processabilityChecker,
      TransportOrderUtil transportOrderUtil,
      DefaultDispatcherConfiguration configuration) {
    this.router = requireNonNull(router, "router");
    this.orderService = requireNonNull(orderService, "orderService");
    this.parkingPosSupplier = requireNonNull(parkingPosSupplier, "parkingPosSupplier");
    this.processabilityChecker = requireNonNull(processabilityChecker, "processabilityChecker");
    this.transportOrderUtil = requireNonNull(transportOrderUtil, "transportOrderUtil");
    this.configuration = requireNonNull(configuration, "configuration");
  }

  @Override
  public void initialize() {
    if (isInitialized()) {
      return;
    }

    parkingPosSupplier.initialize();

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

    parkingPosSupplier.terminate();

    initialized = false;
  }

  @Override
  public void run() {
    if (!configuration.parkIdleVehicles()) {
      return;
    }

    for (Vehicle vehicle : orderService.fetchObjects(Vehicle.class, this::parkable)) {
      createParkingOrder(vehicle);
    }
  }

  private void createParkingOrder(Vehicle vehicle) {
    Point vehiclePosition = orderService.fetchObject(Point.class, vehicle.getCurrentPosition());
    if (vehiclePosition.isParkingPosition()) {
      return;
    }

    // Get a suitable parking position for the vehicle.
    Optional<Point> parkPos = parkingPosSupplier.findParkingPosition(vehicle);
    LOG.debug("Parking position for {}: {}", vehicle, parkPos);
    // If we could not find a suitable parking position at all, just leave the vehicle where it is.
    if (!parkPos.isPresent()) {
      LOG.info("{}: Did not find a suitable parking position.", vehicle.getName());
      return;
    }
    // Create a destination for the point.
    List<DestinationCreationTO> parkDests = Arrays.asList(
        new DestinationCreationTO(parkPos.get().getName(), DriveOrder.Destination.OP_PARK)
    );
    // Create a transport order for parking and verify its processability.
    TransportOrder parkOrder = orderService.createTransportOrder(
        new TransportOrderCreationTO("Park-" + UUID.randomUUID(), parkDests)
            .withDispensable(true)
            .withIntendedVehicleName(vehicle.getName())
    );
    Optional<List<DriveOrder>> driveOrders = router.getRoute(vehicle, vehiclePosition, parkOrder);
    if (processabilityChecker.checkProcessability(vehicle, parkOrder) && driveOrders.isPresent()) {
      transportOrderUtil.assignTransportOrder(vehicle, parkOrder, driveOrders.get());
    }
    else {
      // Mark the order as failed, since the vehicle cannot execute it.
      orderService.updateTransportOrderState(parkOrder.getReference(), TransportOrder.State.FAILED);
    }
  }

  private boolean parkable(Vehicle vehicle) {
    return vehicle.getIntegrationLevel() == Vehicle.IntegrationLevel.TO_BE_UTILIZED
        && vehicle.hasProcState(Vehicle.ProcState.IDLE)
        && vehicle.hasState(Vehicle.State.IDLE)
        && vehicle.getCurrentPosition() != null
        && vehicle.getOrderSequence() == null;
  }

}
