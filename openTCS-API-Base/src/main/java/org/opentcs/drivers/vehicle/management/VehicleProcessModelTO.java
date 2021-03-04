/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.drivers.vehicle.management;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import static java.util.Objects.requireNonNull;
import java.util.Queue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.opentcs.data.model.Triple;
import org.opentcs.data.model.Vehicle;
import org.opentcs.data.notification.UserNotification;
import org.opentcs.drivers.vehicle.LoadHandlingDevice;
import org.opentcs.drivers.vehicle.VehicleProcessModel;

/**
 * A serializable representation of a {@link VehicleProcessModel}.
 *
 * @author Martin Grzenia (Fraunhofer IML)
 */
public class VehicleProcessModelTO
    implements Serializable {

  /**
   * The vehicle name to this process model.
   */
  private String vehicleName;
  /**
   * Whether the comm adapter is currently enabled.
   */
  private boolean commAdapterEnabled;
  /**
   * Whether the comm adapter is currently connected to the vehicle.
   */
  private boolean commAdapterConnected;
  /**
   * The name of the vehicle's current position.
   */
  private String vehiclePosition;
  /**
   * User notifications published by the comm adapter.
   */
  private Queue<UserNotification> notifications = new LinkedList<>();
  /**
   * The percise position of the vehicle.
   */
  private Triple precisePosition;
  /**
   * The vehicle's orentation angle.
   */
  private double orientationAngle = Double.NaN;
  /**
   * The vehicle's energy level.
   */
  private int energyLevel;
  /**
   * A list of load handling devices attached to the vehicle.
   */
  private List<LoadHandlingDevice> loadHandlingDevices = new LinkedList<>();
  /**
   * The vehicle's state.
   */
  private Vehicle.State vehicleState = Vehicle.State.UNKNOWN;

  public String getVehicleName() {
    return vehicleName;
  }

  public VehicleProcessModelTO setVehicleName(@Nonnull String vehicleName) {
    this.vehicleName = requireNonNull(vehicleName);
    return this;
  }

  public boolean isCommAdapterEnabled() {
    return commAdapterEnabled;
  }

  public VehicleProcessModelTO setCommAdapterEnabled(boolean commAdapterEnabled) {
    this.commAdapterEnabled = commAdapterEnabled;
    return this;
  }

  public boolean isCommAdapterConnected() {
    return commAdapterConnected;
  }

  public VehicleProcessModelTO setCommAdapterConnected(boolean commAdapterConnected) {
    this.commAdapterConnected = commAdapterConnected;
    return this;
  }

  @Nullable
  public String getVehiclePosition() {
    return vehiclePosition;
  }

  public VehicleProcessModelTO setVehiclePosition(@Nullable String vehiclePosition) {
    this.vehiclePosition = vehiclePosition;
    return this;
  }

  @Nonnull
  public Queue<UserNotification> getNotifications() {
    return notifications;
  }

  public VehicleProcessModelTO setNotifications(@Nonnull Queue<UserNotification> notifications) {
    this.notifications = requireNonNull(notifications, "notifications");
    return this;
  }

  @Nullable
  public Triple getPrecisePosition() {
    return precisePosition;
  }

  public VehicleProcessModelTO setPrecisePosition(@Nullable Triple precisePosition) {
    this.precisePosition = precisePosition;
    return this;
  }

  public double getOrientationAngle() {
    return orientationAngle;
  }

  public VehicleProcessModelTO setOrientationAngle(double orientationAngle) {
    this.orientationAngle = orientationAngle;
    return this;
  }

  public int getEnergyLevel() {
    return energyLevel;
  }

  public VehicleProcessModelTO setEnergyLevel(int energyLevel) {
    this.energyLevel = energyLevel;
    return this;
  }

  @Nonnull
  public List<LoadHandlingDevice> getLoadHandlingDevices() {
    return loadHandlingDevices;
  }

  public VehicleProcessModelTO setLoadHandlingDevices(
      @Nonnull List<LoadHandlingDevice> loadHandlingDevices) {
    this.loadHandlingDevices = requireNonNull(loadHandlingDevices, "loadHandlingDevices");
    return this;
  }

  @Nonnull
  public Vehicle.State getVehicleState() {
    return vehicleState;
  }

  public VehicleProcessModelTO setVehicleState(@Nonnull Vehicle.State state) {
    this.vehicleState = requireNonNull(state, "state");
    return this;
  }
}
