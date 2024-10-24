// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.drivers.vehicle.management;

import static java.util.Objects.requireNonNull;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import java.io.Serializable;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import org.opentcs.data.model.BoundingBox;
import org.opentcs.data.model.Triple;
import org.opentcs.data.model.Vehicle;
import org.opentcs.data.notification.UserNotification;
import org.opentcs.drivers.vehicle.LoadHandlingDevice;
import org.opentcs.drivers.vehicle.VehicleProcessModel;
import org.opentcs.util.annotations.ScheduledApiChange;

/**
 * A serializable representation of a {@link VehicleProcessModel}.
 * <p>
 * For documentation of methods in this class, see the API documentation of their corresponding
 * counterparts in {@link VehicleProcessModel}.
 * </p>
 */
public class VehicleProcessModelTO
    implements
      Serializable {

  private String name;
  private boolean commAdapterEnabled;
  private boolean commAdapterConnected;
  private String position;
  private Queue<UserNotification> notifications = new ArrayDeque<>();
  private Triple precisePosition;
  private double orientationAngle = Double.NaN;
  private int energyLevel;
  private List<LoadHandlingDevice> loadHandlingDevices = new ArrayList<>();
  private Vehicle.State state = Vehicle.State.UNKNOWN;
  private BoundingBox boundingBox = new BoundingBox(1000, 1000, 1000);

  /**
   * Creates a new instance.
   */
  public VehicleProcessModelTO() {
  }

  public String getName() {
    return name;
  }

  public VehicleProcessModelTO setName(
      @Nonnull
      String name
  ) {
    this.name = requireNonNull(name);
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
  public String getPosition() {
    return position;
  }

  public VehicleProcessModelTO setPosition(
      @Nullable
      String position
  ) {
    this.position = position;
    return this;
  }

  @Nonnull
  public Queue<UserNotification> getNotifications() {
    return notifications;
  }

  public VehicleProcessModelTO setNotifications(
      @Nonnull
      Queue<UserNotification> notifications
  ) {
    this.notifications = requireNonNull(notifications, "notifications");
    return this;
  }

  @Nullable
  public Triple getPrecisePosition() {
    return precisePosition;
  }

  public VehicleProcessModelTO setPrecisePosition(
      @Nullable
      Triple precisePosition
  ) {
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
      @Nonnull
      List<LoadHandlingDevice> loadHandlingDevices
  ) {
    this.loadHandlingDevices = requireNonNull(loadHandlingDevices, "loadHandlingDevices");
    return this;
  }

  @Nonnull
  public Vehicle.State getState() {
    return state;
  }

  public VehicleProcessModelTO setState(
      @Nonnull
      Vehicle.State state
  ) {
    this.state = requireNonNull(state, "state");
    return this;
  }

  @Deprecated
  @ScheduledApiChange(when = "7.0", details = "Will be removed.")
  public int getLength() {
    return (int) boundingBox.getLength();
  }

  @Deprecated
  @ScheduledApiChange(when = "7.0", details = "Will be removed.")
  public VehicleProcessModelTO setLength(int length) {
    setBoundingBox(getBoundingBox().withLength(length));
    return this;
  }

  public BoundingBox getBoundingBox() {
    return boundingBox;
  }

  public VehicleProcessModelTO setBoundingBox(
      @Nonnull
      BoundingBox boundingBox
  ) {
    this.boundingBox = requireNonNull(boundingBox, "boundingBox");
    return this;
  }
}
