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
import java.util.LinkedList;
import java.util.List;
import static java.util.Objects.requireNonNull;
import java.util.Queue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.opentcs.data.model.Triple;
import org.opentcs.data.model.Vehicle;
import org.opentcs.data.notification.UserNotification;
import org.opentcs.data.order.TransportOrder;
import org.opentcs.util.annotations.ScheduledApiChange;

/**
 * An observable model of a vehicle's and its comm adapter's attributes.
 *
 * @author Iryna Felko (Fraunhofer IML)
 * @author Stefan Walter (Fraunhofer IML)
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
   * Used for implementing property change events.
   */
  private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
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
  private final Queue<UserNotification> notifications = new LinkedList<>();

  private Triple precisePosition;

  private double orientationAngle = Double.NaN;

  private int energyLevel = 100;

  private List<LoadHandlingDevice> loadHandlingDevices = new LinkedList<>();

  private int maxVelocity;

  private int maxReverseVelocity;

  private Vehicle.State state = Vehicle.State.UNKNOWN;

  @Deprecated
  @ScheduledApiChange(when = "5.0")
  private VehicleCommAdapter.State adapterState = VehicleCommAdapter.State.UNKNOWN;

  /**
   * Creates a new instance.
   *
   * @param attachedVehicle The vehicle attached to the new instance.
   */
  public VehicleProcessModel(@Nonnull Vehicle attachedVehicle) {
    vehicle = requireNonNull(attachedVehicle, "attachedVehicle");
  }

  public void addPropertyChangeListener(PropertyChangeListener listener) {
    pcs.addPropertyChangeListener(listener);
  }

  public void removePropertyChangeListener(PropertyChangeListener listener) {
    pcs.removePropertyChangeListener(listener);
  }

  /**
   * Returns a copy of the kernel's Vehicle instance.
   *
   * @return A copy of the kernel's Vehicle instance.
   * @deprecated Will be removed.
   */
  @Deprecated
  @ScheduledApiChange(when = "5.0")
  @Nonnull
  public Vehicle getVehicle() {
    return vehicle;
  }

  /**
   * Returns the vehicle's name.
   *
   * @return The vehicle's name.
   */
  @Nonnull
  public String getName() {
    return vehicle.getName();
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
   */
  @Nullable
  public String getVehiclePosition() {
    return vehiclePosition;
  }

  /**
   * Updates the vehicle's current position.
   *
   * @param position The new position
   */
  public void setVehiclePosition(@Nullable String position) {
    // Otherwise update the position, notify listeners and let the kernel know.
    String oldValue = this.vehiclePosition;
    vehiclePosition = position;

    getPropertyChangeSupport().firePropertyChange(Attribute.POSITION.name(),
                                                  oldValue,
                                                  position);
  }

  /**
   * Returns the vehicle's precise position.
   *
   * @return The vehicle's precise position.
   */
  @Nullable
  public Triple getVehiclePrecisePosition() {
    return precisePosition;
  }

  /**
   * Sets the vehicle's precise position.
   *
   * @param position The new position.
   */
  public void setVehiclePrecisePosition(@Nullable Triple position) {
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
   */
  public double getVehicleOrientationAngle() {
    return orientationAngle;
  }

  /**
   * Sets the vehicle's current orientation angle.
   *
   * @param angle The new angle
   * @see Vehicle#setOrientationAngle(double)
   */
  public void setVehicleOrientationAngle(double angle) {
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
   */
  public int getVehicleEnergyLevel() {
    return energyLevel;
  }

  /**
   * Sets the vehicle's current energy level.
   *
   * @param newLevel The new level.
   */
  public void setVehicleEnergyLevel(int newLevel) {
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
   */
  @Nonnull
  public List<LoadHandlingDevice> getVehicleLoadHandlingDevices() {
    return loadHandlingDevices;
  }

  /**
   * Sets the vehicle's load handling devices.
   *
   * @param devices The new devices
   */
  public void setVehicleLoadHandlingDevices(@Nonnull List<LoadHandlingDevice> devices) {
    List<LoadHandlingDevice> devs = new LinkedList<>();
    for (LoadHandlingDevice lhd : devices) {
      devs.add(new LoadHandlingDevice(lhd));
    }
    List<LoadHandlingDevice> oldValue = this.loadHandlingDevices;
    this.loadHandlingDevices = devs;

    getPropertyChangeSupport().firePropertyChange(Attribute.LOAD_HANDLING_DEVICES.name(),
                                                  oldValue,
                                                  devs);
  }

  /**
   * Returns the vehicle's maximum velocity.
   *
   * @return The vehicle's maximum velocity.
   * @deprecated The maximum velocity is not a dynamic vehicle attribute.
   */
  @Deprecated
  @ScheduledApiChange(when = "5.0")
  public int getVehicleMaxVelocity() {
    return maxVelocity;
  }

  /**
   * Sets the vehicle's maximum velocity.
   *
   * @param newVelocity The new maximum velocity.
   * @deprecated The maximum velocity is not a dynamic vehicle attribute.
   */
  @Deprecated
  @ScheduledApiChange(when = "5.0")
  public void setVehicleMaxVelocity(int newVelocity) {
    int oldValue = this.maxVelocity;
    this.maxVelocity = newVelocity;

    getPropertyChangeSupport().firePropertyChange(Attribute.MAX_VELOCITY.name(),
                                                  oldValue,
                                                  newVelocity);
  }

  /**
   * Returns the vehicle's maximum reverse velocity.
   *
   * @return The vehicle's maximum reverse velocity.
   * @deprecated The maximum reverse velocity is not a dynamic vehicle attribute.
   */
  @Deprecated
  @ScheduledApiChange(when = "5.0")
  public int getVehicleMaxReverseVelocity() {
    return maxReverseVelocity;
  }

  /**
   * Sets the vehicle's maximum reverse velocity.
   *
   * @param newVelocity The new maximum reverse velocity.
   * @deprecated The maximum reverse velocity is not a dynamic vehicle attribute.
   */
  @Deprecated
  @ScheduledApiChange(when = "5.0")
  public void setVehicleMaxReverseVelocity(int newVelocity) {
    int oldValue = this.maxReverseVelocity;
    this.maxReverseVelocity = newVelocity;

    getPropertyChangeSupport().firePropertyChange(Attribute.MAX_REVERSE_VELOCITY.name(),
                                                  oldValue,
                                                  newVelocity);
  }

  /**
   * Sets a property of the vehicle.
   *
   * @param key The property's key.
   * @param value The property's new value.
   * @see Vehicle#setProperty(java.lang.String, java.lang.String)
   */
  public void setVehicleProperty(@Nonnull String key, @Nullable String value) {
    getPropertyChangeSupport().firePropertyChange(Attribute.VEHICLE_PROPERTY.name(),
                                                  null,
                                                  new VehiclePropertyUpdate(key, value));
  }

  /**
   * Returns the vehicle's current state.
   *
   * @return The state
   */
  @Nonnull
  public Vehicle.State getVehicleState() {
    return state;
  }

  /**
   * Sets the vehicle's current state.
   *
   * @param newState The new state
   */
  public void setVehicleState(@Nonnull Vehicle.State newState) {
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
   * Returns the comm adapter's current state.
   *
   * @return The comm adapter's current state.
   * @deprecated VehicleCommAdapter.State is deprecated.
   */
  @Deprecated
  @ScheduledApiChange(when = "5.0")
  @Nonnull
  public VehicleCommAdapter.State getVehicleAdapterState() {
    return adapterState;
  }

  /**
   * Sets the comm adapter's current state.
   *
   * @param newState The new state.
   * @deprecated VehicleCommAdapter.State is deprecated.
   */
  @Deprecated
  @ScheduledApiChange(when = "5.0")
  public void setVehicleAdapterState(@Nonnull VehicleCommAdapter.State newState) {
    VehicleCommAdapter.State oldValue = this.adapterState;
    this.adapterState = newState;

    getPropertyChangeSupport().firePropertyChange(Attribute.COMM_ADAPTER_STATE.name(),
                                                  oldValue,
                                                  newState);
  }

  /**
   * Sets a property of the transport order the vehicle is currently processing.
   *
   * @param key The property's key.
   * @param value The property's new value.
   * @see TransportOrder#setProperty(java.lang.String, java.lang.String)
   */
  public void setTransportOrderProperty(@Nonnull String key, @Nullable String value) {
    // XXX Should check if property already has the new value.
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
   * Notifies observers that the given command has been sent to the associated vehicle
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

  protected PropertyChangeSupport getPropertyChangeSupport() {
    return pcs;
  }

  /**
   * A notification object sent to observers to indicate a change of a property.
   */
  public static abstract class PropertyUpdate {

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
     * Indicates a change of the vehicle's maximum velocity.
     *
     * @deprecated The maximum velocity is not a dynamic vehicle attribute.
     */
    @Deprecated
    @ScheduledApiChange(when = "5.0")
    MAX_VELOCITY,
    /**
     * Indicates a change of the vehicle's maximum reverse velocity.
     *
     * @deprecated The maximum velocity is not a dynamic vehicle attribute.
     */
    @Deprecated
    @ScheduledApiChange(when = "5.0")
    MAX_REVERSE_VELOCITY,
    /**
     * Indicates a change of the vehicle's state.
     */
    STATE,
    /**
     * Indicates a change of the comm adapter's state.
     *
     * @deprecated VehicleCommAdapter.State is deprecated.
     */
    @Deprecated
    @ScheduledApiChange(when = "5.0")
    COMM_ADAPTER_STATE,
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
    TRANSPORT_ORDER_PROPERTY;
  }
}
