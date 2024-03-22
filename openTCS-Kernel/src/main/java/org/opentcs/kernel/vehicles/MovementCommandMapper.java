/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.kernel.vehicles;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import static java.util.Objects.requireNonNull;
import javax.inject.Inject;
import org.opentcs.components.kernel.services.TCSObjectService;
import org.opentcs.data.model.Location;
import org.opentcs.data.model.Point;
import org.opentcs.data.order.DriveOrder;
import org.opentcs.data.order.Route;
import org.opentcs.data.order.TransportOrder;
import org.opentcs.drivers.vehicle.MovementCommand;

/**
 * Provides methods for mapping {@link DriveOrder}s to a list of {@link MovementCommand}s.
 */
public class MovementCommandMapper {

  private final TCSObjectService objectService;

  /**
   * Creates a new instance.
   *
   * @param objectService The object service to use.
   */
  @Inject
  public MovementCommandMapper(TCSObjectService objectService) {
    this.objectService = requireNonNull(objectService, "objectService");
  }

  /**
   * Maps the given {@link DriveOrder} to a corresponding list of {@link MovementCommand}s.
   *
   * @param driveOrder The {@link DriveOrder} to map.
   * @param transportOrder The {@link TransportOrder} the drive order belongs to.
   * @return A list of {@link MovementCommand}s.
   */
  public List<MovementCommand> toMovementCommands(DriveOrder driveOrder,
                                                  TransportOrder transportOrder) {
    requireNonNull(driveOrder, "driveOrder");
    requireNonNull(transportOrder, "transportOrder");

    // Gather information from the drive order.
    String op = driveOrder.getDestination().getOperation();
    Route orderRoute = driveOrder.getRoute();
    Point finalDestination = orderRoute.getFinalDestinationPoint();
    Location finalDestinationLocation
        = objectService.fetchObject(Location.class,
                                    driveOrder.getDestination().getDestination().getName());
    Map<String, String> destProperties = driveOrder.getDestination().getProperties();

    List<MovementCommand> result = new ArrayList<>(orderRoute.getSteps().size());
    // Create a movement command for every step in the drive order's route.
    Iterator<Route.Step> stepIter = orderRoute.getSteps().iterator();
    while (stepIter.hasNext()) {
      Route.Step curStep = stepIter.next();
      // Ignore report positions on the route.
      if (curStep.getDestinationPoint().isHaltingPosition()) {
        boolean isFinalMovement = !stepIter.hasNext();

        String operation = isFinalMovement ? op : MovementCommand.NO_OPERATION;
        Location location = isFinalMovement ? finalDestinationLocation : null;

        result.add(
            new MovementCommandImpl(transportOrder,
                                    driveOrder,
                                    curStep,
                                    operation,
                                    location,
                                    isFinalMovement,
                                    finalDestinationLocation,
                                    finalDestination,
                                    op,
                                    mergeProperties(transportOrder.getProperties(), destProperties))
        );
      }
    }

    return result;
  }

  /**
   * Merges the properties of a transport order and those of a drive order.
   *
   * @param orderProps The properties of a transport order.
   * @param destProps The properties of a drive order destination.
   * @return The merged properties.
   */
  private Map<String, String> mergeProperties(Map<String, String> orderProps,
                                              Map<String, String> destProps) {
    requireNonNull(orderProps, "orderProps");
    requireNonNull(destProps, "destProps");

    Map<String, String> result = new HashMap<>();
    result.putAll(orderProps);
    result.putAll(destProps);
    return result;
  }
}
