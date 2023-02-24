/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.kernel.extensions.servicewebapi.v1.binding.shared;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import static java.util.Objects.requireNonNull;
import javax.annotation.Nonnull;
import org.opentcs.data.order.DriveOrder;

/**
 * A {@link org.opentcs.data.order.DriveOrder DriveOrder}'s destination.
 */
public class DestinationState {

  private String locationName = "";

  private String operation = "";

  private State state = State.PRISTINE;

  private List<Property> properties = new ArrayList<>();

  /**
   * Creates a new instance.
   */
  public DestinationState() {
  }

  @Nonnull
  public String getLocationName() {
    return locationName;
  }

  public DestinationState setLocationName(@Nonnull String name) {
    this.locationName = requireNonNull(name, "name");
    return this;
  }

  @Nonnull
  public String getOperation() {
    return operation;
  }

  public DestinationState setOperation(@Nonnull String operation) {
    this.operation = requireNonNull(operation, "operation");
    return this;
  }

  @Nonnull
  public State getState() {
    return state;
  }

  public DestinationState setState(@Nonnull State state) {
    this.state = requireNonNull(state, "state");
    return this;
  }

  @Nonnull
  public List<Property> getProperties() {
    return properties;
  }

  public DestinationState setProperties(@Nonnull List<Property> properties) {
    this.properties = requireNonNull(properties, "properties");
    return this;
  }

  public static DestinationState fromDriveOrder(DriveOrder driveOrder) {
    if (driveOrder == null) {
      return null;
    }
    DestinationState destination = new DestinationState();
    destination.setLocationName(driveOrder.getDestination().getDestination().getName());
    destination.setOperation(driveOrder.getDestination().getOperation());
    destination.setState(mapDriveOrderState(driveOrder.getState()));

    for (Map.Entry<String, String> mapEntry
             : driveOrder.getDestination().getProperties().entrySet()) {
      destination.getProperties().add(new Property(mapEntry.getKey(), mapEntry.getValue()));
    }
    return destination;
  }

  private static DestinationState.State mapDriveOrderState(DriveOrder.State driveOrderState) {
    switch (driveOrderState) {
      case PRISTINE:
        return DestinationState.State.PRISTINE;
      case TRAVELLING:
        return DestinationState.State.TRAVELLING;
      case OPERATING:
        return DestinationState.State.OPERATING;
      case FINISHED:
        return DestinationState.State.FINISHED;
      case FAILED:
        return DestinationState.State.FAILED;
      default:
        throw new IllegalArgumentException("Unhandled drive order state: " + driveOrderState);
    }
  }

  /**
   * This enumeration defines the various states a DriveOrder may be in.
   */
  public enum State {

    /**
     * A DriveOrder's initial state, indicating it being still untouched/not
     * being processed.
     */
    PRISTINE,
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
    FAILED;
  }
}
