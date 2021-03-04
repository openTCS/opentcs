/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.strategies.basic.dispatching.vehicleselection;

import java.util.List;
import static java.util.Objects.requireNonNull;
import org.opentcs.data.model.Vehicle;
import org.opentcs.data.order.DriveOrder;

/**
 * Contains information for a potential assignment of a vehicle to a transport order.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public class VehicleCandidate {

  /**
   * The vehicle.
   */
  private final Vehicle vehicle;
  /**
   * The costs for processing the order with the vehicle.
   */
  private final long costs;
  /**
   * The route/drive orders to be executed upon assignment.
   */
  private final List<DriveOrder> driveOrders;

  public VehicleCandidate(Vehicle vehicle, long costs, List<DriveOrder> driveOrders) {
    this.vehicle = requireNonNull(vehicle, "vehicle");
    this.costs = costs;
    this.driveOrders = requireNonNull(driveOrders, "driveOrders");
  }

  public Vehicle getVehicle() {
    return vehicle;
  }

  public long getCosts() {
    return costs;
  }

  public List<DriveOrder> getDriveOrders() {
    return driveOrders;
  }

}
