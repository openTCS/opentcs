/*
 * openTCS copyright information:
 * Copyright (c) 2012 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.plugins.panels.loadgenerator;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;

/**
 * A class to save <code>TransportOrderData</code> in an XML structure.
 *
 * @author Philipp Seifert (Fraunhofer IML)
 */
@XmlType(propOrder = {"properties", "driveOrders", "name", "deadline",
                      "intendedVehicle"})
@XmlSeeAlso(TransportOrderTableModel.class)
public class TransportOrderXMLStructure {

  /**
   * This transport order's name.
   */
  private String name;
  /**
   * The new transport order's deadline.
   */
  private Deadline deadline = Deadline.PLUS_ONE_HOUR;
  /**
   * The drive orders the transport order consists of.
   */
  private final List<DriveOrderXMLStructure> driveOrders = new ArrayList<>();
  /**
   * A reference to the vehicle intended to process the transport order.
   */
  private String intendedVehicle;
  /**
   * Properties of the transport order data.
   */
  private List<XMLMapEntry> properties = new LinkedList<>();

  /**
   * Creates a new instance of TransportOrderData, that performes
   * XML telegram generation. This class implements also functions for
   * management of data structures that contains the data of
   * gui-generated elements and data need for creating a new XML telegram and
   * for creating of a new transport order instances.
   *
   * @param name The name of the transport order
   * @param deadline The deadline of this transport order
   * @param driveOrders A list containing the drive orders
   * @param intendedVehicle The intended vehicle for this transport order
   * @param properties A map containing the properties of this transport order
   */
  public TransportOrderXMLStructure(String name,
                                    TransportOrderData.Deadline deadline,
                                    List<DriveOrderStructure> driveOrders,
                                    String intendedVehicle,
                                    Map<String, String> properties) {
    this.name = name;
    switch (deadline.toString()) {
      case "-5 min.":
        this.deadline = Deadline.MINUS_FIVE_MINUTES;
        break;
      case "5 min.":
        this.deadline = Deadline.PLUS_FIVE_MINUTES;
        break;
      case "-30 min.":
        this.deadline = Deadline.MINUS_HALF_HOUR;
        break;
      case "30 min.":
        this.deadline = Deadline.PLUS_HALF_HOUR;
        break;
      case " 1 h.":
        this.deadline = Deadline.PLUS_ONE_HOUR;
        break;
      case " 2 h.":
        this.deadline = Deadline.PLUS_TWO_HOURS;
        break;
      default:
        this.deadline = Deadline.PLUS_ONE_HOUR;
        break;
    }
    this.intendedVehicle = intendedVehicle;
    for (Map.Entry<String, String> curEntry : properties.entrySet()) {
      this.properties.add(
          new XMLMapEntry(curEntry.getKey(), curEntry.getValue()));
    }
    for (DriveOrderStructure curDOS : driveOrders) {
      this.driveOrders.add(
          new DriveOrderXMLStructure(curDOS.getDriveOrderLocation().getName(),
                                     curDOS.getDriveOrderVehicleOperation()));
    }
  }

  /**
   * Creates an empty <code>TransportOrderXMLStructure</code>.
   */
  public TransportOrderXMLStructure() {
    // Do nada.
  }

  /**
   * Adds a new <code>DriveOrderXMLStructure</code> to this transport order.
   *
   * @param driveOrder The drive order to be added
   */
  public void addDriveOrder(DriveOrderXMLStructure driveOrder) {
    Objects.requireNonNull(driveOrder, "driveOrder is null");
    driveOrders.add(driveOrder);
  }

  /**
   * Removes a <code>DriveOrderXMLStructure</code> from the list of drive orders.
   *
   * @param index The index of the drive order in the list.
   */
  public void removeDriveOrder(int index) {
    driveOrders.remove(index);
  }

  /**
   * Returns the properties of this transport order data.
   *
   * @return The properties
   */
  @XmlElement(name = "properties", required = true)
  //@XmlJavaTypeAdapter(MapAdapter.class)
  public List<XMLMapEntry> getProperties() {
    return properties;
  }

  /**
   * Returns a list of drive orders.
   *
   * @return The list of drive orders.
   */
  @XmlElement(name = "driveOrders", required = true)
  public List<DriveOrderXMLStructure> getDriveOrders() {
    return driveOrders;
  }

  /**
   * Returns a transport order's name.
   *
   * @return This transport order's name.
   */
  @XmlAttribute(name = "name", required = true)
  public String getName() {
    return this.name;
  }

  /**
   * Sets a new transport order name.
   *
   * @param newName The new unique name for transport order associated with its
   * TransportOrderData instance.
   */
  public void setName(String newName) {
    Objects.requireNonNull(newName, "newName is null");
    if (newName.isEmpty()) {
      throw new IllegalArgumentException("newName is the empty string");
    }
    this.name = newName;
  }

  /**
   * Returns a deadline.
   *
   * @return The deadline.
   */
  @XmlElement(name = "deadline", required = true)
  public Deadline getDeadline() {
    return this.deadline;
  }

  /**
   * Sets the currect deadline for representing transport order.
   *
   * @param newDeadline The new deadline.
   */
  public void setDeadline(Deadline newDeadline) {
    this.deadline = Objects.requireNonNull(newDeadline, "newDeadline is null");
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
  @XmlType(propOrder = {"time", "toString"})
  public enum Deadline {

    /**
     * The deadline value is five minutes in the past.
     */
    MINUS_FIVE_MINUTES("-5 min.", -60 * 5 * 1000),
    /**
     * The deadline value is five minutes in the future.
     */
    PLUS_FIVE_MINUTES("5 min.", 60 * 5 * 1000),
    /**
     * The deadline value is a half hour in the past.
     */
    MINUS_HALF_HOUR("-30 min.", -60 * 30 * 1000),
    /**
     * The deadline value is a half hour in the future.
     */
    PLUS_HALF_HOUR("30 min.", 60 * 30 * 1000),
    /**
     * The deadline value is a one hour in the future.
     */
    PLUS_ONE_HOUR(" 1 h.", 60 * 60 * 1000),
    /**
     * The deadline value is two hours in the future.
     */
    PLUS_TWO_HOURS(" 2 h.", 2 * 60 * 60 * 1000);
    /**
     * The deadline value (in ms).
     */
    private int millis;
    /**
     * The deadline label.
     */
    private final String label;

    /**
     * Creates a new Deadline.
     *
     * @param newLabel The deadline in a string
     * @param deadline The value
     */
    private Deadline(String newLabel, int deadline) {
      this.millis = deadline;
      this.label = newLabel;
    }

    /**
     * Returns a deadline value (in ms relative to the current time).
     *
     * @return The deadline value (in ms relative to the current time).
     */
    @XmlAttribute(name = "time", required = true)
    public int getTime() {
      return this.millis;
    }

    /**
     * Returns an absolute time.
     *
     * @return The absolute time.
     */
    public Date getAbsoluteTime() {
      Calendar calendar = Calendar.getInstance();
      calendar.add(Calendar.MILLISECOND, this.getTime());
      return calendar.getTime();
    }

    @XmlAttribute(name = "toString", required = true)
    @Override
    public String toString() {
      return this.label;
    }
  }

  /**
   * A class to marshal the map of properties.
   */
  @XmlType
  public static class XMLMapEntry {

    /**
     * The key.
     */
    @XmlAttribute
    private String key;
    /**
     * The value.
     */
    @XmlAttribute
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

    public String getKey() {
      return key;
    }

    public void setKey(String key) {
      this.key = key;
    }

    public String getValue() {
      return value;
    }

    public void setValue(String value) {
      this.value = value;
    }
  }
}
