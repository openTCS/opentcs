// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.drivers.vehicle;

import static java.util.Objects.requireNonNull;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;
import org.opentcs.data.TCSObjectReference;
import org.opentcs.data.model.BoundingBox;
import org.opentcs.data.model.Pose;
import org.opentcs.data.model.Vehicle;
import org.opentcs.data.notification.UserNotification;

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
  @SuppressWarnings("this-escape")
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
   * The vehicle's pose.
   */
  private Pose pose = new Pose(null, Double.NaN);
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
   * The vehicle's current bounding box.
   */
  private BoundingBox boundingBox;

  /**
   * Creates a new instance.
   *
   * @param attachedVehicle The vehicle attached to the new instance.
   */
  public VehicleProcessModel(
      @Nonnull
      Vehicle attachedVehicle
  ) {
    this.vehicle = requireNonNull(attachedVehicle, "attachedVehicle");
    this.vehicleReference = vehicle.getReference();
    this.boundingBox = vehicle.getBoundingBox();
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
  public void publishUserNotification(
      @Nonnull
      UserNotification notification
  ) {
    requireNonNull(notification, "notification");

    notifications.add(notification);
    while (notifications.size() > MAX_NOTIFICATION_COUNT) {
      notifications.remove();
    }

    getPropertyChangeSupport().firePropertyChange(
        Attribute.USER_NOTIFICATION.name(),
        null,
        notification
    );
  }

  /**
   * Publishes an event via the kernel's event mechanism.
   *
   * @param event The event to be published.
   */
  public void publishEvent(
      @Nonnull
      VehicleCommAdapterEvent event
  ) {
    requireNonNull(event, "event");

    getPropertyChangeSupport().firePropertyChange(
        Attribute.COMM_ADAPTER_EVENT.name(),
        null,
        event
    );
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

    getPropertyChangeSupport().firePropertyChange(
        Attribute.COMM_ADAPTER_ENABLED.name(),
        oldValue,
        commAdapterEnabled
    );
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

    getPropertyChangeSupport().firePropertyChange(
        Attribute.COMM_ADAPTER_CONNECTED.name(),
        oldValue,
        commAdapterConnected
    );
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
   */
  public void setPosition(
      @Nullable
      String position
  ) {
    // Otherwise update the position, notify listeners and let the kernel know.
    String oldValue = this.position;
    this.position = position;

    getPropertyChangeSupport().firePropertyChange(
        Attribute.POSITION.name(),
        oldValue,
        position
    );
  }

  /**
   * Returns the vehicle's pose.
   *
   * @return The vehicle's pose.
   */
  @Nonnull
  public Pose getPose() {
    return pose;
  }

  /**
   * Sets the vehicle's pose.
   *
   * @param pose The new pose
   */
  public void setPose(
      @Nonnull
      Pose pose
  ) {
    requireNonNull(pose, "pose");

    Pose oldPose = this.pose;
    this.pose = pose;
    getPropertyChangeSupport().firePropertyChange(
        Attribute.POSE.name(),
        oldPose,
        pose
    );
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
   */
  public void setEnergyLevel(int newLevel) {
    int oldValue = this.energyLevel;
    this.energyLevel = newLevel;

    getPropertyChangeSupport().firePropertyChange(
        Attribute.ENERGY_LEVEL.name(),
        oldValue,
        newLevel
    );
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
   */
  public void setLoadHandlingDevices(
      @Nonnull
      List<LoadHandlingDevice> devices
  ) {
    List<LoadHandlingDevice> devs = new ArrayList<>(devices);

    List<LoadHandlingDevice> oldValue = this.loadHandlingDevices;
    this.loadHandlingDevices = devs;

    getPropertyChangeSupport().firePropertyChange(
        Attribute.LOAD_HANDLING_DEVICES.name(),
        oldValue,
        devs
    );
  }

  /**
   * Sets a property of the vehicle.
   *
   * @param key The property's key.
   * @param value The property's new value.
   */
  public void setProperty(
      @Nonnull
      String key,
      @Nullable
      String value
  ) {
    requireNonNull(key, "key");

    // Check whether the new value is the same as the last one we set. If yes, ignore the update,
    // as it would cause unnecessary churn in the kernel.
    // Note that this assumes that other components do not modify properties set by this driver.
    String oldValue = vehicleProperties.get(key);
    if (Objects.equals(value, oldValue)) {
      return;
    }
    vehicleProperties.put(key, value);

    getPropertyChangeSupport().firePropertyChange(
        Attribute.VEHICLE_PROPERTY.name(),
        null,
        new VehiclePropertyUpdate(key, value)
    );
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
   */
  public void setState(
      @Nonnull
      Vehicle.State newState
  ) {
    Vehicle.State oldState = this.state;
    this.state = newState;

    getPropertyChangeSupport().firePropertyChange(Attribute.STATE.name(), oldState, newState);

    if (oldState != Vehicle.State.ERROR && newState == Vehicle.State.ERROR) {
      publishUserNotification(
          new UserNotification(
              getName(),
              "Vehicle state changed to ERROR",
              UserNotification.Level.NOTEWORTHY
          )
      );
    }
    else if (oldState == Vehicle.State.ERROR && newState != Vehicle.State.ERROR) {
      publishUserNotification(
          new UserNotification(
              getName(),
              "Vehicle state is no longer ERROR",
              UserNotification.Level.NOTEWORTHY
          )
      );
    }
  }

  /**
   * Returns the vehicle's current bounding box.
   *
   * @return The vehicle's current bounding box.
   */
  @Nonnull
  public BoundingBox getBoundingBox() {
    return boundingBox;
  }

  /**
   * Sets the vehicle's current bounding box.
   *
   * @param boundingBox The new bounding box.
   */
  public void setBoundingBox(
      @Nonnull
      BoundingBox boundingBox
  ) {
    requireNonNull(boundingBox, "boundingBox");

    BoundingBox oldValue = this.boundingBox;
    this.boundingBox = boundingBox;

    getPropertyChangeSupport().firePropertyChange(
        Attribute.BOUNDING_BOX.name(),
        oldValue,
        boundingBox
    );
  }

  /**
   * Sets a property of the transport order the vehicle is currently processing.
   *
   * @param key The property's key.
   * @param value The property's new value.
   */
  public void setTransportOrderProperty(
      @Nonnull
      String key,
      @Nullable
      String value
  ) {
    requireNonNull(key, "key");

    // Check whether the new value is the same as the last one we set. If yes, ignore the update,
    // as it would cause unnecessary churn in the kernel.
    // Note that this assumes that other components do not modify properties set by this driver.
    String oldValue = transportOrderProperties.get(key);
    if (Objects.equals(value, oldValue)) {
      return;
    }
    transportOrderProperties.put(key, value);

    getPropertyChangeSupport().firePropertyChange(
        Attribute.TRANSPORT_ORDER_PROPERTY.name(),
        null,
        new TransportOrderPropertyUpdate(key, value)
    );
  }

  /**
   * Notifies observers that the given command has been added to the comm adapter's command queue.
   *
   * @param enqueuedCommand The command that has been added to the queue.
   */
  public void commandEnqueued(
      @Nonnull
      MovementCommand enqueuedCommand
  ) {
    getPropertyChangeSupport().firePropertyChange(
        Attribute.COMMAND_ENQUEUED.name(),
        null,
        enqueuedCommand
    );
  }

  /**
   * Notifies observers that the given command has been sent to the associated vehicle.
   *
   * @param sentCommand The command that has been sent to the vehicle.
   */
  public void commandSent(
      @Nonnull
      MovementCommand sentCommand
  ) {
    getPropertyChangeSupport().firePropertyChange(
        Attribute.COMMAND_SENT.name(),
        null,
        sentCommand
    );
  }

  /**
   * Notifies observers that the given command has been executed by the comm adapter/vehicle.
   *
   * @param executedCommand The command that has been executed.
   */
  public void commandExecuted(
      @Nonnull
      MovementCommand executedCommand
  ) {
    getPropertyChangeSupport().firePropertyChange(
        Attribute.COMMAND_EXECUTED.name(),
        null,
        executedCommand
    );
  }

  /**
   * Notifies observers that the given command could not be executed by the comm adapter/vehicle.
   *
   * @param failedCommand The command that could not be executed.
   */
  public void commandFailed(
      @Nonnull
      MovementCommand failedCommand
  ) {
    getPropertyChangeSupport().firePropertyChange(
        Attribute.COMMAND_FAILED.name(),
        null,
        failedCommand
    );
  }

  /**
   * Notifies observers that the vehicle would like to have its integration level changed.
   *
   * @param level The integration level to change to.
   */
  public void integrationLevelChangeRequested(
      @Nonnull
      Vehicle.IntegrationLevel level
  ) {
    getPropertyChangeSupport().firePropertyChange(
        Attribute.INTEGRATION_LEVEL_CHANGE_REQUESTED.name(),
        null,
        level
    );
  }

  /**
   * Notifies observers that the vehicle would like to have its (logical) position
   * determined based on a precise position.
   *
   * @param precisePosition The precise position.
   */
  public void positionResolutionRequested(Pose precisePosition) {
    getPropertyChangeSupport().firePropertyChange(
        Attribute.POSITION_RESOLUTION_REQUESTED.name(),
        position,
        precisePosition
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
      extends
        PropertyUpdate {

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
      extends
        PropertyUpdate {

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
     * Indicates a change of the vehicle's pose.
     */
    POSE,
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
     * Indicates a change of the vehicle's bounding box.
     */
    BOUNDING_BOX,
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
    TRANSPORT_ORDER_WITHDRAWAL_REQUESTED,
    /**
     * Indicates a request to determine the vehicle's (logical) position based on a precise
     * position.
     */
    POSITION_RESOLUTION_REQUESTED;
  }
}
