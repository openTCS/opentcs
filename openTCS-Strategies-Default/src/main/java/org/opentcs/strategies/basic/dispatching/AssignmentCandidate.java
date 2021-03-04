/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.strategies.basic.dispatching;

import java.util.List;
import static java.util.Objects.requireNonNull;
import org.opentcs.data.model.Vehicle;
import org.opentcs.data.order.DriveOrder;
import org.opentcs.data.order.TransportOrder;
import static org.opentcs.util.Assertions.checkArgument;

/**
 * Contains information for a potential assignment of a transport order to a vehicle.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public class AssignmentCandidate {

  /**
   * The vehicle.
   */
  private final Vehicle vehicle;
  /**
   * The transport order.
   */
  private final TransportOrder transportOrder;
  /**
   * The route/drive orders to be executed upon assignment.
   */
  private final List<DriveOrder> driveOrders;
  /**
   * The completeRoutingCosts for processing the whole order with the vehicle.
   */
  private final long completeRoutingCosts;

  /**
   * Creates a new instance.
   *
   * @param vehicle The vehicle that would be assigned to the transport order.
   * @param transportOrder The transport order that would be assigned to the vehicle.
   * @param driveOrders The drive orders containing the computed route the vehicle would take. May
   * not be empty.
   */
  public AssignmentCandidate(Vehicle vehicle,
                             TransportOrder transportOrder,
                             List<DriveOrder> driveOrders) {
    this.vehicle = requireNonNull(vehicle, "vehicle");
    this.transportOrder = requireNonNull(transportOrder, "transportOrder");
    this.driveOrders = requireNonNull(driveOrders, "driveOrders");
    checkArgument(!driveOrders.isEmpty(), "driveOrders is empty");
    this.completeRoutingCosts = cumulatedCosts(driveOrders);
  }

  public Vehicle getVehicle() {
    return vehicle;
  }

  public TransportOrder getTransportOrder() {
    return transportOrder;
  }

  public List<DriveOrder> getDriveOrders() {
    return driveOrders;
  }

  /**
   * Returns the costs for travelling only the first drive order/reaching the first destination.
   *
   * @return The costs for travelling only the first drive order.
   */
  public long getInitialRoutingCosts() {
    return driveOrders.get(0).getRoute().getCosts();
  }

  /**
   * Returns the costs for travelling all drive orders.
   *
   * @return The costs for travelling all drive orders.
   */
  public long getCompleteRoutingCosts() {
    return completeRoutingCosts;
  }

  private static long cumulatedCosts(List<DriveOrder> driveOrders) {
    return driveOrders.stream().mapToLong(driveOrder -> driveOrder.getRoute().getCosts()).sum();
  }
}
