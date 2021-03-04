/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.strategies.basic.dispatching;

import java.util.List;
import javax.annotation.Nullable;
import org.opentcs.data.model.Vehicle;
import org.opentcs.data.order.DriveOrder;
import org.opentcs.data.order.TransportOrder;

/**
 * Represents selection of a vehicle to a transport order, or vice versa.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public class VehicleOrderSelection {

  /**
   * The transport order to be processed.
   */
  private final TransportOrder transportOrder;
  /**
   * The vehicle supposed to process the transport order.
   */
  private final Vehicle vehicle;
  /**
   * The drive orders describing the route the vehicle would need to take for processing the order.
   */
  private final List<DriveOrder> driveOrders;

  /**
   * Creates a new instance.
   *
   * @param transportOrder The transport order to be processed.
   * May be {@code null} to mark this selection as not assignable.
   * @param vehicle The transport order to be processed.
   * May be {@code null} to mark this selection as not assignable.
   * @param driveOrders The drive orders describing the route the vehicle would need to take for
   * processing the order.
   * May be {@code null} to mark this selection as not assignable.
   */
  public VehicleOrderSelection(@Nullable TransportOrder transportOrder,
                               @Nullable Vehicle vehicle,
                               @Nullable List<DriveOrder> driveOrders) {
    this.transportOrder = transportOrder;
    this.vehicle = vehicle;
    this.driveOrders = driveOrders;
  }

  @Nullable
  public TransportOrder getTransportOrder() {
    return transportOrder;
  }

  @Nullable
  public Vehicle getVehicle() {
    return vehicle;
  }

  @Nullable
  public List<DriveOrder> getDriveOrders() {
    return driveOrders;
  }
  
  /**
   * Checks whether this instance represents a selection that is actually assignable.
   *
   * @return {@code true} if, and only if, this instance represents a selection that is actually
   * assignable.
   */
  public boolean isAssignable() {
    return transportOrder != null && vehicle != null && driveOrders != null;
  }
}
