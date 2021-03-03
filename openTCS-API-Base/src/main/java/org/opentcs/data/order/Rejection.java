/*
 * openTCS copyright information:
 * Copyright (c) 2012 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.data.order;

import java.io.Serializable;
import org.opentcs.data.TCSObjectReference;
import org.opentcs.data.model.Vehicle;

/**
 * Describes the rejection of a transport order by a vehicle, and the reason
 * given for the vehicle rejecting the order.
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
  public Rejection(TCSObjectReference<Vehicle> vehicle, String reason) {
    if (vehicle == null) {
      throw new NullPointerException("vehicle is null");
    }
    if (reason == null) {
      throw new NullPointerException("reason is null");
    }
    this.vehicle = vehicle;
    this.reason = reason;
    this.timestamp = System.currentTimeMillis();
  }

  /**
   * Returns the reason given for rejecting the transport order.
   *
   * @return The reason given for rejecting the transport order.
   */
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
  public TCSObjectReference<Vehicle> getVehicle() {
    return vehicle;
  }
}
