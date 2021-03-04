/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.kernel.extensions.servicewebapi.v1.status.binding;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import static java.util.Objects.requireNonNull;
import javax.validation.constraints.Size;
import org.opentcs.data.order.DriveOrder;
import org.opentcs.util.annotations.ScheduledApiChange;

/**
 * A {@link org.opentcs.data.order.DriveOrder DriveOrder}'s destination.
 */
public class Destination {

  @JsonProperty(required = true)
  @JsonPropertyDescription("The name of the destination location")
  private String locationName = "";

  @JsonProperty(required = true)
  @JsonPropertyDescription("The destination operation")
  private String operation = "";

  @JsonProperty(required = true)
  @JsonPropertyDescription("The drive order's state")
  private State state;

  @JsonPropertyDescription("The drive order's properties")
  @Size(min = 0)
  private List<Property> properties = new LinkedList<>();

  /**
   * Creates a new instance.
   */
  public Destination() {
  }

  public String getLocationName() {
    return locationName;
  }

  public void setLocationName(String name) {
    this.locationName = requireNonNull(name, "name");
  }

  public String getOperation() {
    return operation;
  }

  public void setOperation(String operation) {
    this.operation = requireNonNull(operation, "operation");
  }

  public State getState() {
    return state;
  }

  public void setState(State state) {
    this.state = requireNonNull(state, "state");
  }

  public List<Property> getProperties() {
    return properties;
  }

  public void setProperties(List<Property> properties) {
    this.properties = properties;
  }

  public static Destination fromDriveOrder(DriveOrder driveOrder){
    if(driveOrder == null){
      return null;
    }
    Destination destination = new Destination();
    destination.setLocationName(driveOrder.getDestination().getDestination().getName());
    destination.setOperation(driveOrder.getDestination().getOperation());
    destination.setState(mapDriveOrderState(driveOrder.getState()));
    
    for (Map.Entry<String, String> mapEntry
               : driveOrder.getDestination().getProperties().entrySet()) {
        Property prop = new Property();
        prop.setKey(mapEntry.getKey());
        prop.setValue(mapEntry.getValue());
        destination.getProperties().add(prop);
      }
    return destination;
  }
  
    @SuppressWarnings("deprecation")
  private static Destination.State mapDriveOrderState(DriveOrder.State driveOrderState) {
    switch (driveOrderState) {
      case PRISTINE:
        return Destination.State.PRISTINE;
      case ACTIVE:
        return Destination.State.ACTIVE;
      case TRAVELLING:
        return Destination.State.TRAVELLING;
      case OPERATING:
        return Destination.State.OPERATING;
      case FINISHED:
        return Destination.State.FINISHED;
      case FAILED:
        return Destination.State.FAILED;
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
     * Indicates a DriveOrder is part of a TransportOrder.
     *
     * @deprecated Unused. Will be removed.
     */
    @Deprecated
    @ScheduledApiChange(when = "5.0")
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
    FAILED;
  }
}
