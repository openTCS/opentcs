/*
 * openTCS copyright information:
 * Copyright (c) 2006 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.data.order;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import static java.util.Objects.requireNonNull;
import javax.annotation.Nonnull;
import javax.xml.bind.annotation.XmlType;
import org.opentcs.data.TCSObjectReference;
import org.opentcs.data.model.Location;

/**
 * Represents a list of movement steps plus an optional operation at the end of
 * this list that a vehicle is supposed to execute.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public class DriveOrder
    implements Serializable,
               Cloneable {

  /**
   * This drive order's destination.
   */
  private Destination destination;
  /**
   * A back-reference to the transport order this drive order belongs to.
   */
  private TCSObjectReference<TransportOrder> transportOrder;
  /**
   * This drive order's route.
   */
  private Route route;
  /**
   * This drive order's current state.
   */
  private State state = State.PRISTINE;

  /**
   * Creates a new DriveOrder.
   *
   * @param orderDestination This drive order's destination.
   */
  public DriveOrder(Destination orderDestination) {
    destination = requireNonNull(orderDestination, "orderDestination");
  }

  /**
   * Returns this drive order's destination.
   *
   * @return This drive order's destination.
   */
  public Destination getDestination() {
    return destination;
  }

  /**
   * Returns a reference to the transport order this drive order belongs to.
   *
   * @return A reference to the transport order this drive order belongs to.
   */
  public TCSObjectReference<TransportOrder> getTransportOrder() {
    return transportOrder;
  }

  /**
   * Sets a reference to the transport order this drive order belongs to.
   *
   * @param transportOrder A reference to the transport order.
   */
  public void setTransportOrder(TCSObjectReference<TransportOrder> transportOrder) {
    this.transportOrder = transportOrder;
  }

  /**
   * Returns this drive order's route.
   *
   * @return This drive order's route. May be <code>null</code> if this drive
   * order's route hasn't been calculated, yet.
   */
  public Route getRoute() {
    return route;
  }

  /**
   * Sets this drive order's route.
   *
   * @param newRoute This drive order's new route.
   */
  public void setRoute(Route newRoute) {
    route = newRoute;
  }

  /**
   * Returns this drive order's state.
   *
   * @return This drive order's state.
   */
  public State getState() {
    return state;
  }

  /**
   * Sets this drive order's state.
   *
   * @param newState This drive order's new state.
   */
  public void setState(State newState) {
    state = requireNonNull(newState, "newState");
  }

  @Override
  public String toString() {
    return route + " -> " + destination;
  }

  @Override
  public DriveOrder clone() {
    DriveOrder clone;
    try {
      clone = (DriveOrder) super.clone();
    }
    catch (CloneNotSupportedException exc) {
      throw new IllegalStateException("Unexpected exception", exc);
    }
    clone.destination = destination.clone();
    return clone;
  }

  /**
   * A pair consisting of a location and an operation to be performed at that
   * location.
   */
  public static class Destination
      implements Serializable,
                 Cloneable {

    /**
     * An operation constant for doing nothing.
     */
    public static final String OP_NOP = "NOP";
    /**
     * An operation constant for parking the vehicle.
     */
    public static final String OP_PARK = "PARK";
    /**
     * An operation constant for sending the vehicle to a point without a
     * location associated to it.
     */
    public static final String OP_MOVE = "MOVE";
    /**
     * The destination location.
     */
    private TCSObjectReference<Location> location;
    /**
     * The operation to be performed at the destination location.
     */
    private String operation;
    /**
     * Properties of this destination.
     * May contain parameters for the operation, for instance.
     */
    private Map<String, String> properties;

    /**
     * Creates a new Destination.
     *
     * @param destLocation The destination location.
     * @param destOperation The operation to be performed at the destination
     * location.
     * @param destProperties A set of destProperties. May contain parameters for
     * the operation, for instance, or anything else that might be interesting
     * for the executing vehicle driver.
     */
    public Destination(@Nonnull TCSObjectReference<Location> destLocation,
                       @Nonnull String destOperation,
                       @Nonnull Map<String, String> destProperties) {
      location = requireNonNull(destLocation, "destLocation");
      operation = requireNonNull(destOperation, "destOperation");
      properties = requireNonNull(destProperties, "destProperties");
    }

    /**
     * Creates a new Destination.
     *
     * @param destLocation The destination location.
     * @param destOperation The operation to be performed at the destination
     * location.
     */
    public Destination(TCSObjectReference<Location> destLocation,
                       String destOperation) {
      this(destLocation, destOperation, new HashMap<>());
    }

    /**
     * Returns the destination location.
     *
     * @return The destination location.
     */
    public TCSObjectReference<Location> getLocation() {
      return location;
    }

    /**
     * Returns the operation to be performed at the destination location.
     *
     * @return The operation to be performed at the destination location.
     */
    public String getOperation() {
      return operation;
    }

    /**
     * Returns the properties of this destination.
     *
     * @return The properties of this destination.
     */
    public Map<String, String> getProperties() {
      return properties;
    }

    @Override
    public boolean equals(Object o) {
      if (o instanceof Destination) {
        Destination other = (Destination) o;
        return location.equals(other.location)
            && operation.equals(other.operation)
            && properties.equals(other.properties);
      }
      else {
        return false;
      }
    }

    @Override
    public int hashCode() {
      return location.hashCode() ^ operation.hashCode();
    }

    @Override
    public Destination clone() {
      Destination clone;
      try {
        clone = (Destination) super.clone();
      }
      catch (CloneNotSupportedException exc) {
        throw new IllegalStateException("Unexpected exception", exc);
      }
      clone.location = location.clone();
      clone.properties = new HashMap<>(properties);
      return clone;
    }

    @Override
    public String toString() {
      return location.getName() + ":" + operation;
    }
  }

  /**
   * This enumeration defines the various states a DriveOrder may be in.
   */
  @XmlType(name = "driveOrderState")
  public enum State {

    /**
     * A DriveOrder's initial state, indicating it being still untouched/not
     * being processed.
     */
    PRISTINE,
    /**
     * Indicates a DriveOrder is part of a TransportOrder.
     */
    ACTIVE,
    /**
     * Indicates this drive order being processed at the moment.
     */
    TRAVELLING,
    /**
     * Indicates the vehicle processing an order is currently executing an
     * operation.
     */
    OPERATING,
    /**
     * Marks a DriveOrder as successfully completed.
     */
    FINISHED,
    /**
     * Marks a DriveOrder as failed.
     */
    FAILED
  }
}
