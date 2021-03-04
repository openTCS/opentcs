/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.strategies.basic.dispatching.orderselection;

import java.util.LinkedList;
import java.util.List;
import static java.util.Objects.requireNonNull;
import java.util.Optional;
import java.util.UUID;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;
import org.opentcs.access.LocalKernel;
import org.opentcs.access.to.order.DestinationCreationTO;
import org.opentcs.access.to.order.TransportOrderCreationTO;
import org.opentcs.components.Lifecycle;
import org.opentcs.components.kernel.Router;
import org.opentcs.data.model.Point;
import org.opentcs.data.model.Vehicle;
import org.opentcs.data.order.DriveOrder;
import org.opentcs.data.order.TransportOrder;
import org.opentcs.strategies.basic.dispatching.DefaultDispatcherConfiguration;
import org.opentcs.strategies.basic.dispatching.ProcessabilityChecker;
import org.opentcs.strategies.basic.dispatching.VehicleOrderSelection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
@SuppressWarnings("deprecation")
public class ParkingOrderSelectionStrategy
    implements VehicleOrderSelectionStrategy,
               Lifecycle {

  /**
   * This class's Logger.
   */
  private static final Logger LOG = LoggerFactory.getLogger(ParkingOrderSelectionStrategy.class);
  /**
   * The local kernel instance.
   */
  private final LocalKernel kernel;
  /**
   * The Router instance calculating route costs.
   */
  private final Router router;
  /**
   * Checks processability of transport orders for vehicles.
   */
  private final ProcessabilityChecker processabilityChecker;
  /**
   * The dispatcher configuration.
   */
  private final DefaultDispatcherConfiguration configuration;
  /**
   * The strategy used for finding suitable parking positions.
   */
  private final org.opentcs.components.kernel.ParkingPositionSupplier parkingPosSupplier;
  /**
   * Indicates whether this component is initialized.
   */
  private boolean initialized;

  @Inject
  public ParkingOrderSelectionStrategy(
      LocalKernel kernel,
      Router router,
      org.opentcs.components.kernel.ParkingPositionSupplier parkingPosSupplier,
      ProcessabilityChecker processabilityChecker,
      DefaultDispatcherConfiguration configuration) {
    this.router = requireNonNull(router, "router");
    this.kernel = requireNonNull(kernel, "kernel");
    this.processabilityChecker = requireNonNull(processabilityChecker, "processabilityChecker");
    this.parkingPosSupplier = requireNonNull(parkingPosSupplier, "parkingPosSupplier");
    this.configuration = requireNonNull(configuration, "configuration");
  }

  @Override
  public void initialize() {
    if (initialized) {
      return;
    }
    parkingPosSupplier.initialize();
    
    initialized = true;
  }

  @Override
  public void terminate() {
    if (!initialized) {
      return;
    }
    parkingPosSupplier.terminate();
    
    initialized = false;
  }

  @Override
  public boolean isInitialized() {
    return initialized;
  }

  @Nullable
  @Override
  public VehicleOrderSelection selectOrder(@Nonnull Vehicle vehicle) {
    requireNonNull(vehicle, "vehicle");
    if (!configuration.parkIdleVehicles()) {
      return null;
    }
    Point vehiclePosition = kernel.getTCSObject(Point.class, vehicle.getCurrentPosition());
    if (vehiclePosition.isParkingPosition()) {
      return new VehicleOrderSelection(null, vehicle, null);
    }

    LOG.debug("{}: Looking for a parking position...", vehicle.getName());
    // Get a suitable parking position for the vehicle.
    Optional<Point> parkPos = parkingPosSupplier.findParkingPosition(vehicle);
    LOG.debug("Parking position for {}: {}", vehicle, parkPos);
    // If we could not find a suitable parking position at all, just leave the
    // vehicle where it is.
    if (!parkPos.isPresent()) {
      LOG.warn("{}: Did not find a suitable parking position.", vehicle.getName());
      return null;
    }
    // Create a destination for the point.
    List<DestinationCreationTO> parkDests = new LinkedList<>();
    parkDests.add(new DestinationCreationTO(parkPos.get().getName(),
                                            DriveOrder.Destination.OP_PARK));
    // Create a transport order for parking and verify its processability.
    TransportOrder parkOrder = kernel.createTransportOrder(
        new TransportOrderCreationTO("Park-" + UUID.randomUUID(), parkDests)
            .setDispensable(true)
            .setIntendedVehicleName(vehicle.getName())
    );
    Optional<List<DriveOrder>> driveOrders = router.getRoute(vehicle, vehiclePosition, parkOrder);
    if (processabilityChecker.checkProcessability(vehicle, parkOrder) && driveOrders.isPresent()) {
      return new VehicleOrderSelection(parkOrder, vehicle, driveOrders.get());
    }
    else {
      // Mark the order as failed, since the vehicle does not want to execute it.
      kernel.setTransportOrderState(parkOrder.getReference(), TransportOrder.State.FAILED);
      return null;
    }
  }

}
