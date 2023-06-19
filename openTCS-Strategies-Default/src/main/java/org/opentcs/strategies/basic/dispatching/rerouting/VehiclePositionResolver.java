/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.strategies.basic.dispatching.rerouting;

import java.util.ArrayList;
import java.util.List;
import static java.util.Objects.requireNonNull;
import javax.inject.Inject;
import org.opentcs.components.kernel.services.TCSObjectService;
import org.opentcs.data.model.Point;
import org.opentcs.data.model.Vehicle;
import org.opentcs.drivers.vehicle.MovementCommand;
import org.opentcs.drivers.vehicle.VehicleCommAdapter;
import org.opentcs.drivers.vehicle.VehicleController;
import org.opentcs.drivers.vehicle.VehicleControllerPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides methods to resolve the position of a {@link Vehicle}.
 */
public class VehiclePositionResolver {

  private static final Logger LOG = LoggerFactory.getLogger(VehiclePositionResolver.class);
  private final VehicleControllerPool vehicleControllerPool;
  private final TCSObjectService objectService;

  /**
   * Creates a new instance.
   *
   * @param vehicleControllerPool The pool of {@link VehicleController}s.
   * @param objectService The object service to use.
   */
  @Inject
  public VehiclePositionResolver(VehicleControllerPool vehicleControllerPool,
                                 TCSObjectService objectService) {
    this.vehicleControllerPool = requireNonNull(vehicleControllerPool, "vehicleControllerPool");
    this.objectService = requireNonNull(objectService, "objectService");
  }

  /**
   * Returns the position the given {@link Vehicle} will be at after processing all commands that
   * have been or are to be sent to its {@link VehicleCommAdapter}, or its current position, if
   * there are no such commands.
   *
   * @param vehicle The vehicle to get the position for.
   * @return The position as a {@link Point}.
   */
  public Point getFutureOrCurrentPosition(Vehicle vehicle) {
    VehicleController controller = vehicleControllerPool.getVehicleController(vehicle.getName());
    if (controller.getCommandsSent().isEmpty()
        && controller.getInteractionsPendingCommand().isEmpty()) {
      LOG.debug("{}: No commands expected to be executed. Using current position: {}",
                vehicle.getName(),
                vehicle.getCurrentPosition());
      return objectService.fetchObject(Point.class, vehicle.getCurrentPosition());
    }

    if (controller.getInteractionsPendingCommand().isPresent()) {
      LOG.debug(
          "{}: Command with pending peripheral operations present. Using its destination point: {}",
          vehicle.getName(),
          controller.getInteractionsPendingCommand().get().getStep().getDestinationPoint()
      );
      return controller.getInteractionsPendingCommand().get().getStep().getDestinationPoint();
    }

    List<MovementCommand> commandsSent = new ArrayList<>(controller.getCommandsSent());
    MovementCommand lastCommandSent = commandsSent.get(commandsSent.size() - 1);
    LOG.debug("{}: Using the last command sent to the communication adapter: {}",
              vehicle.getName(),
              lastCommandSent);
    return lastCommandSent.getStep().getDestinationPoint();
  }
}
