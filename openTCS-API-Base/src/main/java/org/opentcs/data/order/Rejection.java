/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.data.order;

import java.io.Serializable;
import static java.util.Objects.requireNonNull;
import javax.annotation.Nonnull;
import org.opentcs.data.TCSObjectReference;
import org.opentcs.data.model.Vehicle;

/**
 * Describes the rejection of a transport order by a vehicle, and the reason given for the vehicle
 * rejecting the order.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public class Rejection
    implements Serializable {

  /**
   * The vehicle that rejected the transport order.
   * May not be <code>null</code>.
   */
  private final TCSObjectReference<Vehicle> vehicle;
  /**
   * The reason given for rejecting the transport order.
   * May not be <code>null</code>.
   */
  private final String reason;
  /**
   * The point of time at which the transport order was rejected/this Rejection
   * was created.
   */
  private final long timestamp;

  /**
   * Creates a new Rejection.
   *
   * @param vehicle The vehicle that rejected the transport order.
   * @param reason The reason given for rejecting the transport order.
   */
  public Rejection(@Nonnull TCSObjectReference<Vehicle> vehicle, @Nonnull String reason) {
    this.vehicle = requireNonNull(vehicle, "vehicle");
    this.reason = requireNonNull(reason, "reason");
    this.timestamp = System.currentTimeMillis();
  }

  /**
   * Returns the reason given for rejecting the transport order.
   *
   * @return The reason given for rejecting the transport order.
   */
  @Nonnull
  public String getReason() {
    return reason;
  }

  /**
   * Returns the point of time at which the transport order was rejected/this
   * Rejection was created.
   *
   * @return The point of time at which the transport order was rejected.
   */
  public long getTimestamp() {
    return timestamp;
  }

  /**
   * Returns the vehicle that rejected the transport order.
   *
   * @return The vehicle that rejected the transport order.
   */
  @Nonnull
  public TCSObjectReference<Vehicle> getVehicle() {
    return vehicle;
  }

  @Override
  public String toString() {
    return "Rejection{"
        + "vehicle=" + vehicle
        + ", reason=" + reason
        + ", timestamp=" + timestamp
        + '}';
  }
}
