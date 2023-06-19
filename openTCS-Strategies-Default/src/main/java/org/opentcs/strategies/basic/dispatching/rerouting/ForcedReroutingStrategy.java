/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.strategies.basic.dispatching.rerouting;

import static java.util.Objects.requireNonNull;
import java.util.Optional;
import java.util.Set;
import javax.inject.Inject;
import org.opentcs.components.kernel.Router;
import org.opentcs.components.kernel.services.InternalTransportOrderService;
import org.opentcs.data.model.Point;
import org.opentcs.data.model.Vehicle;
import org.opentcs.data.order.ReroutingType;
import org.opentcs.drivers.vehicle.VehicleController;
import org.opentcs.drivers.vehicle.VehicleControllerPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link ReroutingStrategy} implementation for {@link ReroutingType#FORCED}.
 * <p>
 * Reroutes a {@link Vehicle} from its current position, but only if the vehicle is allowed to
 * allocated the resources for that position.
 */
public class ForcedReroutingStrategy
    extends AbstractReroutingStrategy {

  private static final Logger LOG = LoggerFactory.getLogger(ForcedReroutingStrategy.class);
  private final VehicleControllerPool vehicleControllerPool;
  private final InternalTransportOrderService transportOrderService;

  @Inject
  public ForcedReroutingStrategy(Router router,
                                 InternalTransportOrderService transportOrderService,
                                 VehicleControllerPool vehicleControllerPool,
                                 ForcedDriveOrderMerger driveOrderMerger) {
    super(router, transportOrderService, driveOrderMerger);
    this.transportOrderService = requireNonNull(transportOrderService, "transportOrderService");
    this.vehicleControllerPool = requireNonNull(vehicleControllerPool, "vehicleControllerPool");
  }

  @Override
  protected Optional<Point> determineRerouteSource(Vehicle vehicle) {
    Point currentVehiclePosition = transportOrderService.fetchObject(Point.class,
                                                                     vehicle.getCurrentPosition());

    if (currentVehiclePosition == null) {
      return Optional.empty();
    }

    VehicleController vehicleController
        = vehicleControllerPool.getVehicleController(vehicle.getName());
    if (!vehicleController.mayAllocateNow(Set.of(currentVehiclePosition))) {
      LOG.warn("{}: The resources for the current position are unavailable. "
          + "Unable to determine the reroute source.",
               vehicle.getName());
      return Optional.empty();
    }

    return Optional.of(currentVehiclePosition);
  }
}
