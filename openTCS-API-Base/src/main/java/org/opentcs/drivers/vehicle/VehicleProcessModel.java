/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.drivers.vehicle;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import static java.util.Objects.requireNonNull;
import java.util.Queue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.opentcs.data.TCSObjectReference;
import org.opentcs.data.model.Triple;
import org.opentcs.data.model.Vehicle;
import org.opentcs.data.notification.UserNotification;
import org.opentcs.util.annotations.ScheduledApiChange;

/**
 * An observable model of a vehicle's and its comm adapter's attributes.
 */
public class VehicleProcessModel {

  /**
   * The maximum number of notifications we want to keep.
   */
  private static final int MAX_NOTIFICATION_COUNT = 100;
  /**
   * A copy of the kernel's Vehicle instance.
   */
  private final Vehicle vehicle;
  /**
   * A reference to the vehicle.
   */
  private final TCSObjectReference<Vehicle> vehicleReference;
  /**
   * Used for implementing property change events.
   */
  private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
  /**
   * The vehicle properties set by the driver.
   * (I.e. this map does <em>not</em> contain properties/values set by any other components!)
   */
  private final Map<String, String> vehicleProperties = new HashMap<>();
  /**
   * The transport order properties set by the driver.
   * (I.e. this map does <em>not</em> contain properties/values set by any other components!)
   */
  private final Map<String, String> transportOrderProperties = new HashMap<>();
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
  private String position;
  /**
   * User notifications published by the comm adapter.
   */
  private final Queue<UserNotification> notifications = new ArrayDeque<>();
  /**
   * The vehicle's precise position.
   */
  private Triple precisePosition;
  /**
   * The vehicle's orientation angle.
   */
  private double orientationAngle = Double.NaN;
  /**
   * The vehicle's energy level.
   */
  private int energyLevel = 100;
  /**
   * The vehicle's load handling devices (state).
   */
  private List<LoadHandlingDevice> loadHandlingDevices = new ArrayList<>();
  /**
   * The vehicle's current state.
   */
  private Vehicle.State state = Vehicle.State.UNKNOWN;
  /**
   * The vehicle's current length.
   */
  private int length;

  /**
   * Creates a new instance.
   *
   * @param attachedVehicle The vehicle attached to the new instance.
   */
  public VehicleProcessModel(@Nonnull Vehicle attachedVehicle) {
    this.vehicle = requireNonNull(attachedVehicle, "attachedVehicle");
    this.vehicleReference = vehicle.getReference();
    this.length = vehicle.getLength();
  }

  /**
   * Registers a new property change listener with this model.
   *
   * @param listener The listener to be registered.
   */
  public void addPropertyChangeListener(PropertyChangeListener listener) {
    pcs.addPropertyChangeListener(listener);
  }

  /**
   * Unregisters a property change listener from this model.
   *
   * @param listener The listener to be unregistered.
   */
  public void removePropertyChangeListener(PropertyChangeListener listener) {
    pcs.removePropertyChangeListener(listener);
  }

  /**
   * Returns a reference to the vehicle.
   *
   * @return A reference to the vehicle.
   * @deprecated Use {@link #getReference()} instead.
   */
  @Deprecated()
  @ScheduledApiChange(when = "6.0", details = "Will be removed.")
  @Nonnull
  public TCSObjectReference<Vehicle> getVehicleReference() {
    return getReference();
  }

  /**
   * Returns a reference to the vehicle.
   *
   * @return A reference to the vehicle.
   */
  @Nonnull
  public TCSObjectReference<Vehicle> getReference() {
    return vehicleReference;
  }

  /**
   * Returns the vehicle's name.
   *
   * @return The vehicle's name.
   */
  @Nonnull
  public String getName() {
    return vehicleReference.getName();
  }

  /**
   * Returns user notifications published by the comm adapter.
   *
   * @return The notifications.
   */
  @Nonnull
  public Queue<UserNotification> getNotifications() {
    return notifications;
  }

  /**
   * Publishes an user notification.
   *
   * @param notification The notification to be published.
   */
  public void publishUserNotification(@Nonnull UserNotification notification) {
    requireNonNull(notification, "notification");

    notifications.add(notification);
    while (notifications.size() > MAX_NOTIFICATION_COUNT) {
      notifications.remove();
    }

    getPropertyChangeSupport().firePropertyChange(Attribute.USER_NOTIFICATION.name(),
                                                  null,
                                                  notification);
  }

  /**
   * Publishes an event via the kernel's event mechanism.
   *
   * @param event The event to be published.
   */
  public void publishEvent(@Nonnull VehicleCommAdapterEvent event) {
    requireNonNull(event, "event");

    getPropertyChangeSupport().firePropertyChange(Attribute.COMM_ADAPTER_EVENT.name(),
                                                  null,
                                                  event);
  }

  /**
   * Indicates whether the comm adapter is currently enabled or not.
   *
   * @return <code>true</code> if, and only if, the comm adapter is currently enabled.
   */
  public boolean isCommAdapterEnabled() {
    return commAdapterEnabled;
  }

  /**
   * Sets the comm adapter's <em>enabled</em> flag.
   *
   * @param commAdapterEnabled The new value.
   */
  public void setCommAdapterEnabled(boolean commAdapterEnabled) {
    boolean oldValue = this.commAdapterEnabled;
    this.commAdapterEnabled = commAdapterEnabled;

    getPropertyChangeSupport().firePropertyChange(Attribute.COMM_ADAPTER_ENABLED.name(),
                                                  oldValue,
                                                  commAdapterEnabled);
  }

  /**
   * Indicates whether the comm adapter is currently connected or not.
   *
   * @return <code>true</code> if, and only if, the comm adapter is currently connected.
   */
  public boolean isCommAdapterConnected() {
    return commAdapterConnected;
  }

  /**
   * Sets the comm adapter's <em>connected</em> flag.
   *
   * @param commAdapterConnected The new value.
   */
  public void setCommAdapterConnected(boolean commAdapterConnected) {
    boolean oldValue = this.commAdapterConnected;
    this.commAdapterConnected = commAdapterConnected;

    getPropertyChangeSupport().firePropertyChange(Attribute.COMM_ADAPTER_CONNECTED.name(),
                                                  oldValue,
                                                  commAdapterConnected);
  }

  /**
   * Returns the vehicle's current position.
   *
   * @return The position.
   * @deprecated Use {@link #getPosition()} instead.
   */
  @Deprecated()
  @ScheduledApiChange(when = "6.0", details = "Will be removed.")
  @Nullable
  public String getVehiclePosition() {
    return getPosition();
  }

  /**
   * Returns the vehicle's current position.
   *
   * @return The position.
   */
  @Nullable
  public String getPosition() {
    return position;
  }

  /**
   * Updates the vehicle's current position.
   *
   * @param position The new position
   * @deprecated Use {@link #setPosition(java.lang.String)} instead.
   */
  @Deprecated()
  @ScheduledApiChange(when = "6.0", details = "Will be removed.")
  public void setVehiclePosition(@Nullable String position) {
    setPosition(position);
  }

  /**
   * Updates the vehicle's current position.
   *
   * @param position The new position
   */
  public void setPosition(@Nullable String position) {
    // Otherwise update the position, notify listeners and let the kernel know.
    String oldValue = this.position;
    this.position = position;

    getPropertyChangeSupport().firePropertyChange(Attribute.POSITION.name(),
                                                  oldValue,
                                                  position);
  }

  /**
   * Returns the vehicle's precise position.
   *
   * @return The vehicle's precise position.
   * @deprecated Use {@link #getPrecisePosition()} instead.
   */
  @Deprecated()
  @ScheduledApiChange(when = "6.0", details = "Will be removed.")
  @Nullable
  public Triple getVehiclePrecisePosition() {
    return getPrecisePosition();
  }

  /**
   * Returns the vehicle's precise position.
   *
   * @return The vehicle's precise position.
   */
  @Nullable
  public Triple getPrecisePosition() {
    return precisePosition;
  }

  /**
   * Sets the vehicle's precise position.
   *
   * @param position The new position.
   * @deprecated Use {@link #setPrecisePosition(org.opentcs.data.model.Triple)} instead.
   */
  @Deprecated()
  @ScheduledApiChange(when = "6.0", details = "Will be removed.")
  public void setVehiclePrecisePosition(@Nullable Triple position) {
    setPrecisePosition(position);
  }

  /**
   * Sets the vehicle's precise position.
   *
   * @param position The new position.
   */
  public void setPrecisePosition(@Nullable Triple position) {
    // Otherwise update the position, notify listeners and let the kernel know.
    Triple oldValue = this.precisePosition;
    this.precisePosition = position;

    getPropertyChangeSupport().firePropertyChange(Attribute.PRECISE_POSITION.name(),
                                                  oldValue,
                                                  position);
  }

  /**
   * Returns the vehicle's current orientation angle.
   *
   * @return The vehicle's current orientation angle.
   * @see Vehicle#getOrientationAngle()
   * @deprecated Use {@link #getOrientationAngle()} instead.
   */
  @Deprecated()
  @ScheduledApiChange(when = "6.0", details = "Will be removed.")
  public double getVehicleOrientationAngle() {
    return getOrientationAngle();
  }

  /**
   * Returns the vehicle's current orientation angle.
   *
   * @return The vehicle's current orientation angle.
   * @see Vehicle#getOrientationAngle()
   */
  public double getOrientationAngle() {
    return orientationAngle;
  }

  /**
   * Sets the vehicle's current orientation angle.
   *
   * @param angle The new angle
   * @deprecated Use {@link #setOrientationAngle(double)} instead.
   */
  @Deprecated()
  @ScheduledApiChange(when = "6.0", details = "Will be removed.")
  public void setVehicleOrientationAngle(double angle) {
    setOrientationAngle(angle);
  }

  /**
   * Sets the vehicle's current orientation angle.
   *
   * @param angle The new angle
   */
  public void setOrientationAngle(double angle) {
    double oldValue = this.orientationAngle;
    this.orientationAngle = angle;

    getPropertyChangeSupport().firePropertyChange(Attribute.ORIENTATION_ANGLE.name(),
                                                  oldValue,
                                                  angle);
  }

  /**
   * Returns the vehicle's current energy level.
   *
   * @return The vehicle's current energy level.
   * @deprecated Use {@link #getEnergyLevel()} instead.
   */
  @Deprecated()
  @ScheduledApiChange(when = "6.0", details = "Will be removed.")
  public int getVehicleEnergyLevel() {
    return getEnergyLevel();
  }

  /**
   * Returns the vehicle's current energy level.
   *
   * @return The vehicle's current energy level.
   */
  public int getEnergyLevel() {
    return energyLevel;
  }

  /**
   * Sets the vehicle's current energy level.
   *
   * @param newLevel The new level.
   * @deprecated Use {@link #setEnergyLevel(int)} instead.
   */
  @Deprecated()
  @ScheduledApiChange(when = "6.0", details = "Will be removed.")
  public void setVehicleEnergyLevel(int newLevel) {
    setEnergyLevel(newLevel);
  }

  /**
   * Sets the vehicle's current energy level.
   *
   * @param newLevel The new level.
   */
  public void setEnergyLevel(int newLevel) {
    int oldValue = this.energyLevel;
    this.energyLevel = newLevel;

    getPropertyChangeSupport().firePropertyChange(Attribute.ENERGY_LEVEL.name(),
                                                  oldValue,
                                                  newLevel);
  }

  /**
   * Returns the vehicle's load handling devices.
   *
   * @return The vehicle's load handling devices.
   * @deprecated Use {@link #getLoadHandlingDevices()} instead.
   */
  @Deprecated()
  @ScheduledApiChange(when = "6.0", details = "Will be removed.")
  @Nonnull
  public List<LoadHandlingDevice> getVehicleLoadHandlingDevices() {
    return getLoadHandlingDevices();
  }

  /**
   * Returns the vehicle's load handling devices.
   *
   * @return The vehicle's load handling devices.
   */
  @Nonnull
  public List<LoadHandlingDevice> getLoadHandlingDevices() {
    return loadHandlingDevices;
  }

  /**
   * Sets the vehicle's load handling devices.
   *
   * @param devices The new devices
   * @deprecated Use {@link #setLoadHandlingDevices(java.util.List)} instead.
   */
  @Deprecated()
  @ScheduledApiChange(when = "6.0", details = "Will be removed.")
  public void setVehicleLoadHandlingDevices(@Nonnull List<LoadHandlingDevice> devices) {
    setLoadHandlingDevices(devices);
  }

  /**
   * Sets the vehicle's load handling devices.
   *
   * @param devices The new devices
   */
  public void setLoadHandlingDevices(@Nonnull List<LoadHandlingDevice> devices) {
    List<LoadHandlingDevice> devs = new ArrayList<>(devices);

    List<LoadHandlingDevice> oldValue = this.loadHandlingDevices;
    this.loadHandlingDevices = devs;

    getPropertyChangeSupport().firePropertyChange(Attribute.LOAD_HANDLING_DEVICES.name(),
                                                  oldValue,
                                                  devs);
  }

  /**
   * Sets a property of the vehicle.
   *
   * @param key The property's key.
   * @param value The property's new value.
   * @deprecated Use {@link #setProperty(java.lang.String, java.lang.String)} instead.
   */
  @Deprecated()
  @ScheduledApiChange(when = "6.0", details = "Will be removed.")
  public void setVehicleProperty(@Nonnull String key, @Nullable String value) {
    setProperty(key, value);
  }

  /**
   * Sets a property of the vehicle.
   *
   * @param key The property's key.
   * @param value The property's new value.
   */
  public void setProperty(@Nonnull String key, @Nullable String value) {
    requireNonNull(key, "key");

    // Check whether the new value is the same as the last one we set. If yes, ignore the update,
    // as it would cause unnecessary churn in the kernel.
    // Note that this assumes that other components do not modify properties set by this driver.
    String oldValue = vehicleProperties.get(key);
    if (Objects.equals(value, oldValue)) {
      return;
    }
    vehicleProperties.put(key, value);

    getPropertyChangeSupport().firePropertyChange(Attribute.VEHICLE_PROPERTY.name(),
                                                  null,
                                                  new VehiclePropertyUpdate(key, value));
  }

  /**
   * Returns the vehicle's current state.
   *
   * @return The state
   * @deprecated Use {@link #getState()} instead.
   */
  @Deprecated()
  @ScheduledApiChange(when = "6.0", details = "Will be removed.")
  @Nonnull
  public Vehicle.State getVehicleState() {
    return getState();
  }

  /**
   * Returns the vehicle's current state.
   *
   * @return The state
   */
  @Nonnull
  public Vehicle.State getState() {
    return state;
  }

  /**
   * Sets the vehicle's current state.
   *
   * @param newState The new state
   * @deprecated Use {@link #setState(org.opentcs.data.model.Vehicle.State)} instead.
   */
  @Deprecated()
  @ScheduledApiChange(when = "6.0", details = "Will be removed.")
  public void setVehicleState(@Nonnull Vehicle.State newState) {
    setState(newState);
  }

  /**
   * Sets the vehicle's current state.
   *
   * @param newState The new state
   */
  public void setState(@Nonnull Vehicle.State newState) {
    Vehicle.State oldState = this.state;
    this.state = newState;

    getPropertyChangeSupport().firePropertyChange(Attribute.STATE.name(), oldState, newState);

    if (oldState != Vehicle.State.ERROR && newState == Vehicle.State.ERROR) {
      publishUserNotification(new UserNotification(getName(),
                                                   "Vehicle state changed to ERROR",
                                                   UserNotification.Level.NOTEWORTHY));
    }
    else if (oldState == Vehicle.State.ERROR && newState != Vehicle.State.ERROR) {
      publishUserNotification(new UserNotification(getName(),
                                                   "Vehicle state is no longer ERROR",
                                                   UserNotification.Level.NOTEWORTHY));
    }
  }

  /**
   * Returns the vehicle's current length.
   *
   * @return The vehicle's current length.
   * @deprecated Use {@link #getLength()} instead.
   */
  @Deprecated()
  @ScheduledApiChange(when = "6.0", details = "Will be removed.")
  public int getVehicleLength() {
    return getLength();
  }

  /**
   * Returns the vehicle's current length.
   *
   * @return The vehicle's current length.
   */
  public int getLength() {
    return length;
  }

  /**
   * Sets the vehicle's current length.
   *
   * @param length The new length.
   * @deprecated Use {@link #setLength(int)} instead.
   */
  @Deprecated()
  @ScheduledApiChange(when = "6.0", details = "Will be removed.")
  public void setVehicleLength(int length) {
    setLength(length);
  }

  /**
   * Sets the vehicle's current length.
   *
   * @param length The new length.
   */
  public void setLength(int length) {
    int oldValue = this.length;
    this.length = length;

    getPropertyChangeSupport().firePropertyChange(Attribute.LENGTH.name(),
                                                  oldValue,
                                                  length);
  }

  /**
   * Sets a property of the transport order the vehicle is currently processing.
   *
   * @param key The property's key.
   * @param value The property's new value.
   */
  public void setTransportOrderProperty(@Nonnull String key, @Nullable String value) {
    requireNonNull(key, "key");

    // Check whether the new value is the same as the last one we set. If yes, ignore the update,
    // as it would cause unnecessary churn in the kernel.
    // Note that this assumes that other components do not modify properties set by this driver.
    String oldValue = transportOrderProperties.get(key);
    if (Objects.equals(value, oldValue)) {
      return;
    }
    transportOrderProperties.put(key, value);

    getPropertyChangeSupport().firePropertyChange(Attribute.TRANSPORT_ORDER_PROPERTY.name(),
                                                  null,
                                                  new TransportOrderPropertyUpdate(key, value));
  }

  /**
   * Notifies observers that the given command has been added to the comm adapter's command queue.
   *
   * @param enqueuedCommand The command that has been added to the queue.
   */
  public void commandEnqueued(@Nonnull MovementCommand enqueuedCommand) {
    getPropertyChangeSupport().firePropertyChange(Attribute.COMMAND_ENQUEUED.name(),
                                                  null,
                                                  enqueuedCommand);
  }

  /**
   * Notifies observers that the given command has been sent to the associated vehicle.
   *
   * @param sentCommand The command that has been sent to the vehicle.
   */
  public void commandSent(@Nonnull MovementCommand sentCommand) {
    getPropertyChangeSupport().firePropertyChange(Attribute.COMMAND_SENT.name(),
                                                  null,
                                                  sentCommand);
  }

  /**
   * Notifies observers that the given command has been executed by the comm adapter/vehicle.
   *
   * @param executedCommand The command that has been executed.
   */
  public void commandExecuted(@Nonnull MovementCommand executedCommand) {
    getPropertyChangeSupport().firePropertyChange(Attribute.COMMAND_EXECUTED.name(),
                                                  null,
                                                  executedCommand);
  }

  /**
   * Notifies observers that the given command could not be executed by the comm adapter/vehicle.
   *
   * @param failedCommand The command that could not be executed.
   */
  public void commandFailed(@Nonnull MovementCommand failedCommand) {
    getPropertyChangeSupport().firePropertyChange(Attribute.COMMAND_FAILED.name(),
                                                  null,
                                                  failedCommand);
  }

  /**
   * Notifies observers that the vehicle would like to have its integration level changed.
   *
   * @param level The integration level to change to.
   */
  public void integrationLevelChangeRequested(@Nonnull Vehicle.IntegrationLevel level) {
    getPropertyChangeSupport().firePropertyChange(
        Attribute.INTEGRATION_LEVEL_CHANGE_REQUESTED.name(),
        null,
        level
    );
  }

  /**
   * Notifies observers that the vehicle would like to have its current transport order withdrawn.
   *
   * @param forced Whether a forced withdrawal is requested.
   */
  public void transportOrderWithdrawalRequested(boolean forced) {
    getPropertyChangeSupport().firePropertyChange(
        Attribute.TRANSPORT_ORDER_WITHDRAWAL_REQUESTED.name(),
        null,
        forced
    );
  }

  protected PropertyChangeSupport getPropertyChangeSupport() {
    return pcs;
  }

  /**
   * A notification object sent to observers to indicate a change of a property.
   */
  public static class PropertyUpdate {

    /**
     * The property's key.
     */
    private final String key;
    /**
     * The property's new value.
     */
    private final String value;

    /**
     * Creates a new instance.
     *
     * @param key The key.
     * @param value The new value.
     */
    public PropertyUpdate(String key, String value) {
      this.key = requireNonNull(key, "key");
      this.value = value;
    }

    /**
     * Returns the property's key.
     *
     * @return The property's key.
     */
    public String getKey() {
      return key;
    }

    /**
     * Returns the property's new value.
     *
     * @return The property's new value.
     */
    public String getValue() {
      return value;
    }
  }

  /**
   * A notification object sent to observers to indicate a change of a vehicle's property.
   */
  public static class VehiclePropertyUpdate
      extends PropertyUpdate {

    /**
     * Creates a new instance.
     *
     * @param key The property's key.
     * @param value The new value.
     */
    public VehiclePropertyUpdate(String key, String value) {
      super(key, value);
    }
  }

  /**
   * A notification object sent to observers to indicate a change of a transport order's property.
   */
  public static class TransportOrderPropertyUpdate
      extends PropertyUpdate {

    /**
     * Creates a new instance.
     *
     * @param key The property's key.
     * @param value The new value.
     */
    public TransportOrderPropertyUpdate(String key, String value) {
      super(key, value);
    }
  }

  /**
   * Notification arguments to indicate some change.
   */
  public enum Attribute {
    /**
     * Indicates a change of the comm adapter's <em>enabled</em> setting.
     */
    COMM_ADAPTER_ENABLED,
    /**
     * Indicates a change of the comm adapter's <em>connected</em> setting.
     */
    COMM_ADAPTER_CONNECTED,
    /**
     * Indicates a change of the vehicle's position.
     */
    POSITION,
    /**
     * Indicates a change of the vehicle's precise position.
     */
    PRECISE_POSITION,
    /**
     * Indicates a change of the vehicle's orientation angle.
     */
    ORIENTATION_ANGLE,
    /**
     * Indicates a change of the vehicle's energy level.
     */
    ENERGY_LEVEL,
    /**
     * Indicates a change of the vehicle's load handling devices.
     */
    LOAD_HANDLING_DEVICES,
    /**
     * Indicates a change of the vehicle's state.
     */
    STATE,
    /**
     * Indicates a change of the vehicle's length.
     */
    LENGTH,
    /**
     * Indicates a new user notification was published.
     */
    USER_NOTIFICATION,
    /**
     * Indicates a new comm adapter event was published.
     */
    COMM_ADAPTER_EVENT,
    /**
     * Indicates a command was enqueued.
     */
    COMMAND_ENQUEUED,
    /**
     * Indicates a command was sent.
     */
    COMMAND_SENT,
    /**
     * Indicates a command was executed successfully.
     */
    COMMAND_EXECUTED,
    /**
     * Indicates a command failed.
     */
    COMMAND_FAILED,
    /**
     * Indicates a change of a vehicle property.
     */
    VEHICLE_PROPERTY,
    /**
     * Indicates a change of a transport order property.
     */
    TRANSPORT_ORDER_PROPERTY,
    /**
     * Indicates a request to change the integration level of the vehicle.
     */
    INTEGRATION_LEVEL_CHANGE_REQUESTED,
    /**
     * Indicates a request to withdraw the vehicles current transport order.
     */
    TRANSPORT_ORDER_WITHDRAWAL_REQUESTED;
  }
}
