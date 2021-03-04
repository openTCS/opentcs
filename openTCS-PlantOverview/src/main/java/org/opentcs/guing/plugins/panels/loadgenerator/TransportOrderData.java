/*
 * openTCS copyright information:
 * Copyright (c) 2007 Fraunhofer IML
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
import java.util.TreeMap;
import org.opentcs.data.TCSObjectReference;
import org.opentcs.data.model.Vehicle;
import org.opentcs.data.order.DriveOrder;

/**
 * <code>TransportOrderData</code> implements functionalities for creating
 * and for local storing of xml telegrams.
 * The transport/drive oder data is stored in this class.
 *
 * @author Iryna Felko (Fraunhofer IML)
 */
public class TransportOrderData {

  /**
   * This transport order's name.
   */
  private String name = "";
  /**
   * The new transport order's deadline.
   */
  private Deadline deadline = Deadline.PLUS_ONE_HOUR;
  /**
   * The drive orders the transport order consists of.
   */
  private final List<DriveOrderStructure> driveOrders = new ArrayList<>();
  /**
   * A reference to the vehicle intended to process the transport order.
   */
  private TCSObjectReference<Vehicle> intendedVehicle;
  /**
   * Properties of the transport order data.
   */
  private final Map<String, String> properties = new TreeMap<>();

  /**
   * Creates a new instance of TransportOrderData, that performes
   * XML telegram generation. This class implements also functions for
   * management of data structures that contains the data of
   * gui-generated elements and data need for creating a new XML telegram and
   * for creating of a new transport order instances.
   */
  public TransportOrderData() {
  }

  /**
   * Adds a new DriveOrderStructure to this transport order.
   *
   * @param driveOrder The drive order that shall be added
   */
  public void addDriveOrder(DriveOrderStructure driveOrder) {
    Objects.requireNonNull(driveOrder, "driveOrder is null");
    driveOrders.add(driveOrder);
  }

  /**
   * Removes a <code>DriveOrderStructure</code> from the list of drive orders.
   *
   * @param index The index of the drive order in the list.
   */
  public void removeDriveOrder(int index) {
    driveOrders.remove(index);
  }

  /**
   * Returns a list of matched destinations that have to be travelled when
   * processing the new generated transport order.
   *
   * @return The list of destinations that have to be travelled when processing
   * the new generated transport order.
   */
  public List<DriveOrder.Destination> getDestinations() {
    List<DriveOrder.Destination> destinations = new LinkedList<>();
    for (DriveOrderStructure i : driveOrders) {
      destinations.add(new DriveOrder.Destination(i.getDriveOrderLocation())
          .withOperation(i.getDriveOrderVehicleOperation()));
    }
    return destinations;
  }

  /**
   * Returns the properties of this transport order data.
   *
   * @return The properties
   */
  public Map<String, String> getProperties() {
    return properties;
  }

  /**
   * Adds a property.
   *
   * @param key The key
   * @param value The value
   */
  public void addProperty(String key, String value) {
    if (key == null || value == null) {
      return;
    }
    properties.put(key, value);
  }

  /**
   * Returns a list of drive orders.
   *
   * @return The list of drive orders.
   */
  public List<DriveOrderStructure> getDriveOrders() {
    return driveOrders;
  }

  /**
   * Returns a transport order's name.
   *
   * @return This transport order's name.
   */
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
  public Deadline getDeadline() {
    return this.deadline;
  }

  /**
   * Sets the currect deadline for representing transport order.
   *
   * @param newDeadline The new deadline.
   */
  public void setDeadline(Deadline newDeadline) {
    this.deadline = newDeadline;
  }

  /**
   * Returns a reference to the vehicle intended to process the order.
   *
   * @return A reference to the vehicle intended to process the order.
   */
  public TCSObjectReference<Vehicle> getIntendedVehicle() {
    return intendedVehicle;
  }

  /**
   * Sets a reference to the vehicle intended to process the order.
   *
   * @param vehicle A reference to the vehicle intended to process the order.
   */
  public void setIntendedVehicle(TCSObjectReference<Vehicle> vehicle) {
    intendedVehicle = vehicle;
  }

  /**
   * The enumeration of possible default deadline values.
   */
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
    private final int millis;
    /**
     * The deadline label.
     */
    private final String label;

    /**
     * Creates a new Deadline.
     *
     * @param newLabel This deadline as a string
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

    @Override
    public String toString() {
      return this.label;
    }
  }
}
