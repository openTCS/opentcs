/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.kernel.extensions.xmlhost.status.binding;

import java.util.LinkedList;
import java.util.List;
import static java.util.Objects.requireNonNull;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import org.opentcs.util.annotations.ScheduledApiChange;

/**
 * A {@link org.opentcs.data.order.DriveOrder DriveOrder}'s destination.
 */
public class Destination {

  /**
   * The name of the destination location.
   */
  private String locationName = "";
  /**
   * The operation to be executed at the destination location.
   */
  private String operation = "";
  /**
   * The <code>DriveOrder</code>'s state.
   */
  private State state;
  /**
   * The <code>DriveOrder</code>'s properties.
   */
  private List<Property> properties = new LinkedList<>();

  /**
   * Creates a new instance.
   */
  public Destination() {
  }

  /**
   * Returns the name of the destination location.
   *
   * @return The name of the destination location.
   */
  @XmlAttribute(name = "locationName", required = true)
  public String getLocationName() {
    return locationName;
  }

  /**
   * Sets the name of the destination location.
   *
   * @param name The name of the destination location.
   */
  public void setLocationName(String name) {
    this.locationName = requireNonNull(name, "name");
  }

  /**
   * Returns the operation to be executed at the destination location.
   *
   * @return The operation to be executed at the destination location.
   */
  @XmlAttribute(name = "operation", required = true)
  public String getOperation() {
    return operation;
  }

  /**
   * Sets the operation to be executed at the destination location.
   *
   * @param operation The operation to be executed at the destination
   * location.
   */
  public void setOperation(String operation) {
    this.operation = requireNonNull(operation, "operation");
  }

  /**
   * Returns the <code>DriveOrder</code>'s state.
   *
   * @return The <code>DriveOrder</code>'s state.
   */
  @XmlAttribute(name = "state", required = true)
  public State getState() {
    return state;
  }

  /**
   * Sets the <code>DriveOrder</code>'s state.
   *
   * @param state The <code>DriveOrder</code>'s state.
   */
  public void setState(State state) {
    this.state = requireNonNull(state, "state");
  }

  @XmlElement(name = "property", required = false)
  public List<Property> getProperties() {
    return properties;
  }

  public void setProperties(List<Property> properties) {
    this.properties = properties;
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
