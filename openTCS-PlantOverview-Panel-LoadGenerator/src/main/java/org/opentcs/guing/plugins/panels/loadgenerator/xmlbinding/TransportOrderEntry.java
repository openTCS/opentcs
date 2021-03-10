/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.plugins.panels.loadgenerator.xmlbinding;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import static java.util.Objects.requireNonNull;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import org.opentcs.guing.plugins.panels.loadgenerator.DriveOrderStructure;
import org.opentcs.guing.plugins.panels.loadgenerator.TransportOrderData;

/**
 * Stores a transport order definition for XML marshalling/unmarshalling.
 *
 * @author Philipp Seifert (Fraunhofer IML)
 * @author Stefan Walter (Fraunhofer IML)
 */
@XmlType(propOrder = {"properties", "driveOrders", "deadline", "intendedVehicle"})
public class TransportOrderEntry {

  /**
   * The transport order's deadline.
   */
  private Deadline deadline = Deadline.PLUS_TWO_HOURS;
  /**
   * The drive orders the transport order consists of.
   */
  private final List<DriveOrderEntry> driveOrders = new ArrayList<>();
  /**
   * The name of the vehicle intended to process the transport order.
   */
  private String intendedVehicle;
  /**
   * Properties of the transport order data.
   */
  private List<XMLMapEntry> properties = new LinkedList<>();

  /**
   * Creates a new instance.
   *
   * @param deadline The deadline of this transport order
   * @param driveOrders A list containing the drive orders
   * @param intendedVehicle The intended vehicle for this transport order
   * @param properties A map containing the properties of this transport order
   */
  public TransportOrderEntry(TransportOrderData.Deadline deadline,
                                    List<DriveOrderStructure> driveOrders,
                                    String intendedVehicle,
                                    Map<String, String> properties) {
    switch (deadline) {
      case MINUS_FIVE_MINUTES:
        this.deadline = Deadline.MINUS_FIVE_MINUTES;
        break;
      case PLUS_FIVE_MINUTES:
        this.deadline = Deadline.PLUS_FIVE_MINUTES;
        break;
      case MINUS_HALF_HOUR:
        this.deadline = Deadline.MINUS_HALF_HOUR;
        break;
      case PLUS_HALF_HOUR:
        this.deadline = Deadline.PLUS_HALF_HOUR;
        break;
      case PLUS_ONE_HOUR:
        this.deadline = Deadline.PLUS_ONE_HOUR;
        break;
      case PLUS_TWO_HOURS:
      default:
        this.deadline = Deadline.PLUS_TWO_HOURS;
        break;
    }
    this.intendedVehicle = intendedVehicle;
    for (Map.Entry<String, String> curEntry : properties.entrySet()) {
      this.properties.add(
          new XMLMapEntry(curEntry.getKey(), curEntry.getValue()));
    }
    for (DriveOrderStructure curDOS : driveOrders) {
      this.driveOrders.add(
          new DriveOrderEntry(curDOS.getDriveOrderLocation().getName(),
                                     curDOS.getDriveOrderVehicleOperation()));
    }
  }

  /**
   * Creates a new instance.
   */
  public TransportOrderEntry() {
  }

  /**
   * Returns the properties of this transport order.
   *
   * @return The properties
   */
  @XmlElement(name = "property", required = true)
  //@XmlJavaTypeAdapter(MapAdapter.class)
  public List<XMLMapEntry> getProperties() {
    return properties;
  }

  /**
   * Returns the list of drive orders.
   *
   * @return The list of drive orders.
   */
  @XmlElement(name = "driveOrder", required = true)
  public List<DriveOrderEntry> getDriveOrders() {
    return driveOrders;
  }

  /**
   * Returns a deadline.
   *
   * @return The deadline.
   */
  @XmlAttribute(name = "deadline", required = true)
  public Deadline getDeadline() {
    return this.deadline;
  }

  /**
   * Sets the transport order's deadline.
   *
   * @param deadline The new deadline.
   */
  public void setDeadline(Deadline deadline) {
    this.deadline = requireNonNull(deadline, "deadline");
  }

  /**
   * Returns a reference to the vehicle intended to process the order.
   *
   * @return A reference to the vehicle intended to process the order.
   */
  @XmlAttribute(name = "intendedVehicle", required = true)
  public String getIntendedVehicle() {
    return intendedVehicle;
  }

  /**
   * Sets a reference to the vehicle intended to process the order.
   *
   * @param vehicle A reference to the vehicle intended to process the order.
   */
  public void setIntendedVehicle(String vehicle) {
    intendedVehicle = vehicle;
  }

  /**
   * The enumeration of possible default deadline values.
   */
  public static enum Deadline {

    /**
     * The deadline value is five minutes in the past.
     */
    MINUS_FIVE_MINUTES,
    /**
     * The deadline value is five minutes in the future.
     */
    PLUS_FIVE_MINUTES,
    /**
     * The deadline value is a half hour in the past.
     */
    MINUS_HALF_HOUR,
    /**
     * The deadline value is a half hour in the future.
     */
    PLUS_HALF_HOUR,
    /**
     * The deadline value is a one hour in the future.
     */
    PLUS_ONE_HOUR,
    /**
     * The deadline value is two hours in the future.
     */
    PLUS_TWO_HOURS;
  }

  /**
   * A class to marshal the map of properties.
   */
  @XmlType
  public static class XMLMapEntry {

    /**
     * The key.
     */
    private String key;
    /**
     * The value.
     */
    private String value;

    /**
     * Creates an empty XMLMalEntry.
     */
    public XMLMapEntry() {
    }

    /**
     * Creates a new XMLMapEntry.
     *
     * @param key The key
     * @param value The value
     */
    public XMLMapEntry(String key, String value) {
      this.key = key;
      this.value = value;
    }

    @XmlAttribute
    public String getKey() {
      return key;
    }

    public void setKey(String key) {
      this.key = key;
    }

    @XmlAttribute
    public String getValue() {
      return value;
    }

    public void setValue(String value) {
      this.value = value;
    }
  }
}
