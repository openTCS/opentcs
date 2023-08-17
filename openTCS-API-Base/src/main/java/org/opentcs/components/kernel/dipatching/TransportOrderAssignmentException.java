/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.components.kernel.dipatching;

import static java.util.Objects.requireNonNull;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.opentcs.access.KernelRuntimeException;
import org.opentcs.data.TCSObjectReference;
import org.opentcs.data.model.Vehicle;
import org.opentcs.data.order.TransportOrder;
import static org.opentcs.util.Assertions.checkArgument;

/**
 * Thrown when a {@link TransportOrder} could not be assigned to a {@link Vehicle}.
 */
public class TransportOrderAssignmentException
    extends KernelRuntimeException {

  private final TCSObjectReference<TransportOrder> transportOrder;
  private final TCSObjectReference<Vehicle> vehicle;
  private final TransportOrderAssignmentVeto transportOrderAssignmentVeto;

  /**
   * Creates a new instance.
   *
   * @param transportOrder The transport order.
   * @param vehicle The vehicle.
   * @param transportOrderAssignmentVeto The reason why the transport order could not be assigned
   * to the vehicle.
   */
  public TransportOrderAssignmentException(
      @Nonnull TCSObjectReference<TransportOrder> transportOrder,
      @Nullable TCSObjectReference<Vehicle> vehicle,
      @Nonnull TransportOrderAssignmentVeto transportOrderAssignmentVeto) {
    super("Could not assign transport order '" + transportOrder.getName() + "' to vehicle '"
        + (vehicle != null ? vehicle.getName() : "null") + "': "
        + transportOrderAssignmentVeto.name());
    // This exception is reasonable only for actual assignment vetos.
    checkArgument(transportOrderAssignmentVeto != TransportOrderAssignmentVeto.NO_VETO,
                  "Invalid assignment veto for exception: " + transportOrderAssignmentVeto);
    this.transportOrder = requireNonNull(transportOrder, "transportOrder");
    this.vehicle = vehicle;
    this.transportOrderAssignmentVeto = requireNonNull(transportOrderAssignmentVeto,
                                                       "transportOrderAssignmentVeto");
  }

  /**
   * Returns the transport order.
   *
   * @return The transport order.
   */
  @Nonnull
  public TCSObjectReference<TransportOrder> getTransportOrder() {
    return transportOrder;
  }

  /**
   * Returns the vehicle.
   *
   * @return The vehicle.
   */
  @Nullable
  public TCSObjectReference<Vehicle> getVehicle() {
    return vehicle;
  }

  /**
   * Returns the reason why a transport order assignment was not possible.
   *
   * @return The reason why a transport order assignment was not possible.
   */
  @Nonnull
  public TransportOrderAssignmentVeto getTransportOrderAssignmentVeto() {
    return transportOrderAssignmentVeto;
  }
}
