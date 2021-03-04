/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.kernel.extensions.xmlhost.status.binding;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import org.opentcs.data.model.Triple;
import org.opentcs.data.model.Vehicle;

/**
 * A status message containing information about a vehicle.
 *
 * @author Tobias Marquardt (Fraunhofer IML)
 */
public class VehicleStatusMessage
    extends StatusMessage {

  /**
   * The vehicle's name.
   */
  private String vehicleName = "";
  /**
   * The vehicle's current transport order's name (or empty string).
   */
  private String transportOrderName = "";
  /**
   * Precise position of the vehicle.
   */
  private PrecisePosition precisePosition;
  /**
   * Position of the vehicle.
   */
  private String position;
  /**
   * State of the vehicle.
   */
  private Vehicle.State state;
  /**
   * Processing state of the vehicle.
   */
  private Vehicle.ProcState procState;

  /**
   * Creates a new instance.
   */
  public VehicleStatusMessage() {
  }

  /**
   * Returns the vehicle's name.
   *
   * @return The vehicle's name.
   */
  @XmlAttribute(name = "vehicleName", required = true)
  public String getVehicleName() {
    return vehicleName;
  }

  /**
   * Sets the vehicle's name.
   *
   * @param vehicleName The vehicle's name.
   */
  public void setVehicleName(String vehicleName) {
    this.vehicleName = vehicleName;
  }

  /**
   * Returns the vehicle's current transport order's name.
   *
   * @return The vehicle's current transport order's name (or null if not assigned to a
   * transport order).
   */
  @XmlAttribute(name = "transportOrderName", required = false)
  public String getTransportOrderName() {
    return transportOrderName;
  }

  /**
   * Sets the vehicle's current transport order's name.
   *
   * @param transportOrderName The transport order's name.
   */
  public void setTransportOrderName(String transportOrderName) {
    this.transportOrderName = transportOrderName;
  }

  /**
   * Returns the position of the vehicle.
   *
   * @return The vehicle's position.
   */
  @XmlAttribute(name = "position", required = false)
  public String getPosition() {
    return position;
  }

  /**
   * Sets the position of the vehicle.
   *
   * @param position The vehicle's position.
   */
  public void setPosition(String position) {
    this.position = position;
  }

  /**
   * Returns the precise position of the vehicle.
   *
   * @return The precise position.
   */
  @XmlElement(name = "precisePosition", required = false)
  public PrecisePosition getPrecisePosition() {
    return precisePosition;
  }

  /**
   * Sets the precise position.
   *
   * @param precisePosition The precise position.
   */
  public void setPrecisePosition(PrecisePosition precisePosition) {
    this.precisePosition = precisePosition;
  }

  /**
   * Returns the vehicle's state.
   *
   * @return The current vehicle state.
   */
  @XmlAttribute(name = "state", required = true)
  public Vehicle.State getState() {
    return state;
  }

  /**
   * Sets the vehicle's state.
   *
   * @param state The vehicle state.
   */
  public void setState(Vehicle.State state) {
    this.state = state;
  }

  /**
   * Returns the vehicle's processing state.
   *
   * @return The current vehicle processing state.
   */
  @XmlAttribute(name = "processingState", required = true)
  public Vehicle.ProcState getProcState() {
    return procState;
  }

  /**
   * Sets the vehicle's processing state.
   *
   * @param procState The vehicle processing state.
   */
  public void setProcState(Vehicle.ProcState procState) {
    this.procState = procState;
  }

  public static VehicleStatusMessage fromVehicle(Vehicle vehicle) {
    VehicleStatusMessage vehicleMessage = new VehicleStatusMessage();
    vehicleMessage.setVehicleName(vehicle.getName());
    vehicleMessage.setTransportOrderName(
        vehicle.getTransportOrder() == null ? null : vehicle.getTransportOrder().getName());
    vehicleMessage.setPosition(
        vehicle.getCurrentPosition() == null ? null : vehicle.getCurrentPosition().getName());
    vehicleMessage.setState(vehicle.getState());
    vehicleMessage.setProcState(vehicle.getProcState());
    Triple precisePos = vehicle.getPrecisePosition();
    if (precisePos != null) {
      VehicleStatusMessage.PrecisePosition precisePosElement;
      precisePosElement = new VehicleStatusMessage.PrecisePosition(
          precisePos.getX(), precisePos.getY(), precisePos.getZ());
      vehicleMessage.setPrecisePosition(precisePosElement);
    }
    return vehicleMessage;
  }

  /**
   * A precise position of a vehicle.
   */
  public static class PrecisePosition {

    /**
     * X coordinate of the position.
     */
    private long x;
    /**
     * Y coordinate of the position.
     */
    private long y;
    /**
     * Z coordinate of the position.
     */
    private long z;

    /**
     * Creates a new instance.
     */
    public PrecisePosition() {
      // Do nada.
    }

    /**
     * Creates a new instance.
     *
     * @param x x value
     * @param y y value
     * @param z z value
     */
    public PrecisePosition(long x, long y, long z) {
      this.x = x;
      this.y = y;
      this.z = z;
    }

    /**
     * Get the x coordinate value of the precise position.
     *
     * @return x value
     */
    @XmlAttribute(name = "x", required = true)
    public long getX() {
      return x;
    }

    /**
     * Set the x coordinate value of the precise position.
     *
     * @param x x value
     */
    public void setX(long x) {
      this.x = x;
    }

    /**
     * Get the y coordinate value of the precise position.
     *
     * @return y value
     */
    @XmlAttribute(name = "y", required = true)
    public long getY() {
      return y;
    }

    /**
     * Set the y coordinate value of the precise position.
     *
     * @param y y value
     */
    public void setY(long y) {
      this.y = y;
    }

    /**
     * Get the z coordinate value of the precise position.
     *
     * @return z value
     */
    @XmlAttribute(name = "z", required = true)
    public long getZ() {
      return z;
    }

    /**
     * Set the z coordinate value of the precise position.
     *
     * @param z z value
     */
    public void setZ(long z) {
      this.z = z;
    }
  }
}
