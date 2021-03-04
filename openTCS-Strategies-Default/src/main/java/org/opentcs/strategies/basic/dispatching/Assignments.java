/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.strategies.basic.dispatching;

import static java.util.Objects.requireNonNull;
import javax.inject.Inject;
import org.opentcs.data.model.Point;
import org.opentcs.data.model.Vehicle;
import org.opentcs.data.order.DriveOrder;

/**
 * Provides helper methods for the dispatcher.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public class Assignments {

  /**
   * This class's configuration.
   */
  private final DefaultDispatcherConfiguration configuration;

  @Inject
  public Assignments(DefaultDispatcherConfiguration configuration) {
    this.configuration = requireNonNull(configuration, "configuration");
  }

  /**
   * Checks if the given drive order must be processed or could/should be left out.
   * Orders that should be left out are those with destinations at which the
   * vehicle is already present and which require no destination operation.
   *
   * @param driveOrder The drive order to be processed.
   * @param vehicle The vehicle that would process the order.
   * @return <code>true</code> if, and only if, the given drive order must be
   * processed; <code>false</code> if the order should/must be left out.
   */
  public boolean mustAssign(DriveOrder driveOrder, Vehicle vehicle) {
    requireNonNull(vehicle, "vehicle");
    // Removing a vehicle's drive order is always allowed.
    if (driveOrder == null) {
      return true;
    }
    // Check if all orders are to be assigned.
    if (configuration.assignRedundantOrders()) {
      return true;
    }
    Point destPoint = driveOrder.getRoute().getFinalDestinationPoint();
    String destOp = driveOrder.getDestination().getOperation();
    // We use startsWith(OP_NOP) here because that makes it possible to have
    // multiple different operations ("NOP.*") that all do nothing.
    if (destPoint.getReference().equals(vehicle.getCurrentPosition())
        && (destOp.startsWith(DriveOrder.Destination.OP_NOP)
            || destOp.equals(DriveOrder.Destination.OP_MOVE))) {
      return false;
    }
    return true;
  }


}
