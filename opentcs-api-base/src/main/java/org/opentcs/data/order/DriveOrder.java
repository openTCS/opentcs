// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.data.order;

import static java.util.Objects.requireNonNull;
import static org.opentcs.util.Assertions.checkArgument;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import java.io.Serializable;
import java.util.Map;
import java.util.Objects;
import org.opentcs.data.TCSObjectReference;
import org.opentcs.data.model.Location;
import org.opentcs.data.model.Point;
import org.opentcs.data.model.Vehicle;
import org.opentcs.util.annotations.ScheduledApiChange;

/**
 * Describes a sequence of movements and an optional operation at the end that a {@link Vehicle} is
 * supposed to execute.
 *
 * @see TransportOrder
 */
public class DriveOrder
    implements
      Serializable {

  /**
   * This drive order's name.
   */
  private final String name;
  /**
   * This drive order's destination.
   */
  private final Destination destination;
  /**
   * A back-reference to the transport order this drive order belongs to.
   */
  private final TCSObjectReference<TransportOrder> transportOrder;
  /**
   * This drive order's route.
   */
  private final Route route;
  /**
   * This drive order's current state.
   */
  private final State state;

  /**
   * Creates a new DriveOrder.
   *
   * @param destination This drive order's destination.
   * @deprecated Use {@link #DriveOrder(String, Destination)} instead.
   */
  @Deprecated
  @ScheduledApiChange(when = "7.0", details = "Will be removed")
  public DriveOrder(
      @Nonnull
      Destination destination
  ) {
    this("", destination);
  }

  /**
   * Creates a new DriveOrder.
   *
   * @param name This drive order's name.
   * @param destination This drive order's destination.
   */
  public DriveOrder(
      @Nonnull
      String name,
      @Nonnull
      Destination destination
  ) {
    this(name, destination, null, null, State.PRISTINE);
  }

  private DriveOrder(
      @Nonnull
      String name,
      @Nonnull
      Destination destination,
      @Nullable
      TCSObjectReference<TransportOrder> transportOrder,
      @Nullable
      Route route,
      @Nonnull
      State state
  ) {
    this.name = requireNonNull(name, "name");
    this.destination = requireNonNull(destination, "destination");
    this.transportOrder = transportOrder;
    this.route = route;
    this.state = requireNonNull(state, "state");
  }

  /**
   * Returns this drive order's name.
   *
   * @return This drive order's name.
   */
  @Nonnull
  public String getName() {
    return name;
  }

  /**
   * Returns this drive order's destination.
   *
   * @return This drive order's destination.
   */
  @Nonnull
  public Destination getDestination() {
    return destination;
  }

  /**
   * Returns a reference to the transport order this drive order belongs to.
   *
   * @return A reference to the transport order this drive order belongs to.
   */
  @Nullable
  public TCSObjectReference<TransportOrder> getTransportOrder() {
    return transportOrder;
  }

  /**
   * Creates a copy of this object, with the given transport order.
   *
   * @param transportOrder The value to be set in the copy.
   * @return A copy of this object, differing in the given value.
   */
  public DriveOrder withTransportOrder(
      @Nullable
      TCSObjectReference<TransportOrder> transportOrder
  ) {
    return new DriveOrder(name, destination, transportOrder, route, state);
  }

  /**
   * Returns this drive order's route.
   *
   * @return This drive order's route. May be <code>null</code> if this drive
   * order's route hasn't been calculated, yet.
   */
  @Nullable
  public Route getRoute() {
    return route;
  }

  /**
   * Creates a copy of this object, with the given route.
   *
   * @param route The value to be set in the copy.
   * @return A copy of this object, differing in the given value.
   */
  public DriveOrder withRoute(
      @Nullable
      Route route
  ) {
    return new DriveOrder(name, destination, transportOrder, route, state);
  }

  /**
   * Returns this drive order's state.
   *
   * @return This drive order's state.
   */
  @Nonnull
  public State getState() {
    return state;
  }

  /**
   * Creates a copy of this object, with the given state.
   *
   * @param state The value to be set in the copy.
   * @return A copy of this object, differing in the given value.
   */
  public DriveOrder withState(
      @Nonnull
      State state
  ) {
    return new DriveOrder(name, destination, transportOrder, route, state);
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof DriveOrder other)) {
      return false;
    }

    return Objects.equals(name, other.name)
        && Objects.equals(destination, other.destination)
        && Objects.equals(transportOrder, other.transportOrder);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, destination, transportOrder);
  }

  @Override
  public String toString() {
    return "DriveOrder{" +
        "name=" + name +
        ", destination=" + destination +
        ", transportOrder=" + transportOrder +
        ", state=" + state +
        ", route=" + route +
        '}';
  }

  /**
   * Describes the destination of a drive order.
   */
  public static class Destination
      implements
        Serializable {

    /**
     * An operation constant for doing nothing.
     */
    public static final String OP_NOP = "NOP";
    /**
     * An operation constant for parking the vehicle.
     */
    public static final String OP_PARK = "PARK";
    /**
     * An operation constant for sending the vehicle to a point without a location associated to it.
     */
    public static final String OP_MOVE = "MOVE";
    /**
     * The actual destination (point or location).
     */
    private final TCSObjectReference<?> destination;
    /**
     * The operation to be performed at the destination location.
     */
    private final String operation;
    /**
     * Properties of this destination.
     * May contain parameters for the operation, for instance.
     */
    private final Map<String, String> properties;

    /**
     * Creates a new instance.
     *
     * @param destination The actual destination (must be a reference to a location or point).
     */
    @SuppressWarnings("unchecked")
    public Destination(
        @Nonnull
        TCSObjectReference<?> destination
    ) {
      checkArgument(
          destination.getReferentClass() == Location.class
              || destination.getReferentClass() == Point.class,
          "Not a reference on a location or point: %s",
          destination
      );

      this.destination = requireNonNull(destination, "destination");
      this.operation = OP_NOP;
      this.properties = Map.of();
    }

    private Destination(
        @Nonnull
        TCSObjectReference<?> destination,
        @Nonnull
        Map<String, String> properties,
        @Nonnull
        String operation
    ) {
      this.destination = requireNonNull(destination, "destination");
      this.operation = requireNonNull(operation, "operation");
      this.properties = requireNonNull(properties, "properties");
    }

    /**
     * Returns the actual destination (a location or point).
     *
     * @return The actual destination (a location or point).
     */
    @Nonnull
    public TCSObjectReference<?> getDestination() {
      return destination;
    }

    /**
     * Returns the operation to be performed at the destination location.
     *
     * @return The operation to be performed at the destination location.
     */
    @Nonnull
    public String getOperation() {
      return operation;
    }

    /**
     * Creates a copy of this object, with the given operation.
     *
     * @param operation The value to be set in the copy.
     * @return A copy of this object, differing in the given value.
     */
    public Destination withOperation(
        @Nonnull
        String operation
    ) {
      return new Destination(destination, properties, operation);
    }

    /**
     * Returns the properties of this destination.
     *
     * @return The properties of this destination.
     */
    @Nonnull
    public Map<String, String> getProperties() {
      return properties;
    }

    /**
     * Creates a copy of this object, with the given properties.
     *
     * @param properties The value to be set in the copy.
     * @return A copy of this object, differing in the given value.
     */
    public Destination withProperties(Map<String, String> properties) {
      return new Destination(destination, Map.copyOf(properties), operation);
    }

    @Override
    public boolean equals(Object o) {
      if (o instanceof Destination other) {
        return destination.equals(other.destination)
            && operation.equals(other.operation)
            && properties.equals(other.properties);
      }
      else {
        return false;
      }
    }

    @Override
    public int hashCode() {
      return Objects.hash(destination, operation);
    }

    @Override
    public String toString() {
      return destination.getName() + ":" + operation;
    }
  }

  /**
   * Defines the various potential states of a drive order.
   */
  public enum State {

    /**
     * A drive order's initial state, indicating it being still untouched/not being processed.
     */
    PRISTINE,
    /**
     * Indicates the vehicle processing the order is currently moving to its destination.
     */
    TRAVELLING,
    /**
     * Indicates the vehicle processing the order is currently executing an operation.
     */
    OPERATING,
    /**
     * Marks a drive order as successfully completed.
     */
    FINISHED,
    /**
     * Marks a drive order as failed.
     */
    FAILED
  }
}
