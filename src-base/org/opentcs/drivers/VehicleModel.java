/*
 * openTCS copyright information:
 * Copyright (c) 2007 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.drivers;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Observable;
import java.util.logging.Logger;
import org.opentcs.data.model.Triple;
import org.opentcs.data.model.Vehicle;
import org.opentcs.drivers.CommunicationAdapter.State;

/**
 * Mainly provides vehicle data to be displayed in the GUI.
 *
 * @author Iryna Felko (Fraunhofer IML)
 * @author Stefan Walter (Fraunhofer IML)
 */
public final class VehicleModel
    extends Observable
    implements CommunicationAdapterView, VelocityListener {

  /**
   * This class's Logger.
   */
  private static final Logger log =
      Logger.getLogger(VehicleModel.class.getName());
  /**
   * A copy of the kernel's Vehicle instance.
   */
  private final Vehicle vehicle;
  /**
   * A <code>VelocityHistory</code> instance that helps with visualizing the
   * vehicle's speed.
   */
  private final VelocityHistory velocityHistory = new VelocityHistory(100, 10);
  /**
   * The list of vehicle's messages.
   */
  private final List<Message> messages = new LinkedList<>();
  /**
   * This communication adapter's vehicle manager.
   */
  private volatile VehicleManager vehicleManager;
  /**
   * Communication adapter assigned to this vehicle.
   */
  private BasicCommunicationAdapter commAdapter;
  /**
   * Communication factory.
   */
  private CommunicationAdapterFactory commFactory;
  /**
   * The vehicle's current position.
   */
  private String vehiclePosition;
  /**
   * The last selected tab in the DetailPanel.
   */
  private int selectedTab;

  /**
   * Creates a new VehicleModel container. The new
   * instance of the VehicleModel contains data from
   * the attached vehicle object.
   *
   * @param attachedVehicle The vehicle attached to the new
   * VehicleModel container.
   */
  public VehicleModel(Vehicle attachedVehicle) {
    if (attachedVehicle == null) {
      throw new NullPointerException("vehicle is null");
    }
    vehicle = attachedVehicle;
  }

  /**
   * Returns a copy of the kernel's Vehicle instance.
   *
   * @return A copy of the kernel's Vehicle instance.
   */
  public Vehicle getVehicle() {
    return vehicle;
  }

  /**
   * Returns this vehicle's name.
   *
   * @return This vehicle's name.
   */
  public String getName() {
    return vehicle.getName();
  }

  /**
   * Returns the vehicle state.
   *
   * @return The state
   */
  public Vehicle.State getState() {
    synchronized (vehicle) {
      return vehicle.getState();
    }
  }

  /**
   * Returns whether this vehicle has a communication adapter.
   *
   * @return False if it hasn't,
   * <code>getCommunicationAdapter().isVehicleAlive()</code> otherwise
   */
  public boolean isVehicleAlive() {
    if (!hasCommunicationAdapter()) {
      return false;
    }
    else {
      return getCommunicationAdapter().isVehicleAlive();
    }
  }

  /**
   * Returns whether this' vehicle communication adapter is enabled.
   *
   * @return True if it is, false otherwise.
   */
  public boolean isCommunicationAdapterEnabled() {
    if (commAdapter != null) {
      return commAdapter.isEnabled();
    }
    else {
      return false;
    }
  }

  /**
   * Returns the currently selected tab.
   *
   * @return The index of the tab.
   */
  public int getSelectedTab() {
    return selectedTab;
  }

  /**
   * Sets the currently selected tab.
   * 
   * @param selectedTab The index of the tab.
   */
  public void setSelectedTab(int selectedTab) {
    this.selectedTab = selectedTab;
  }

  /**
   * Returns the position of the vehicle.
   * 
   * @return The positon
   */
  public String getPosition() {
    return vehiclePosition;
  }

  /**
   * Returns all messages sent.
   * 
   * @return The list of messages
   */
  public List<Message> getMessages() {
    return messages;
  }

  /**
   * Get the vehicle's precise position.
   * 
   * @return Precise position in mm. null if precise position not set. 
   */
  public Triple getVehiclePrecisePosition() {
    return vehicle.getPrecisePosition();
  }

  /**
   * Updates the vehicle position.
   * 
   * @param position The new position
   */
  public void setVehiclePosition(String position) {
    // If the new position is not different from the previous one, do nothing.
    if (Objects.equals(vehiclePosition, position)) {
      return;
    }
    // Otherwise update the position, notify listeners and let the kernel know.
    vehiclePosition = position;
    setChanged();
    notifyObservers();
    vehicleManager.setVehiclePosition(position);
  }

  /**
   * Updates the vehicle precise position.
   * 
   * @param position The new position.
   */
  public void setVehiclePrecisePosition(Triple position) {
    // If the new position is not different from the previous one, do nothing.
    if (Objects.equals(vehicle.getPrecisePosition(), position)) {
      return;
    }
    // Otherwise update the position, notify listeners and let the kernel know.
    synchronized (vehicle) {
      vehicle.setPrecisePosition(position);
    }
    setChanged();
    notifyObservers();
    vehicleManager.setVehiclePrecisePosition(position);
  }

  /**
   * Get the vehicle's current orientation angle.
   * 
   * @return Current orientation angle in deg [-360°,360°].
   *          Double.NaN if orientation angle not set.
   */
  public double getVehicleOrientationAngle() {
    return vehicle.getOrientationAngle();
  }

  /**
   * Updates the vehicle orientation angle.
   * 
   * @param angle The new angle
   */
  public void setVehicleOrientationAngle(double angle) {
    // If the new angle is not different from the previous one, do nothing.
    if (angle == vehicle.getOrientationAngle()) {
      return;
    }
    synchronized (vehicle) {
      vehicle.setOrientationAngle(angle);
    }
    setChanged();
    notifyObservers();
    vehicleManager.setVehicleOrientationAngle(angle);
  }

  /**
   * Updates the vehicle energy level.
   * 
   * @param newLevel The new level.
   */
  public void setVehicleEnergyLevel(int newLevel) {
    // If the new level is not different from the previous one, do nothing.
    if (newLevel == vehicle.getEnergyLevel()) {
      return;
    }
    synchronized (vehicle) {
      vehicle.setEnergyLevel(newLevel);
    }
    setChanged();
    notifyObservers();
    vehicleManager.setVehicleEnergyLevel(newLevel);
  }

  /**
   * Updates the vehicle's recharge operation.
   * 
   * @param rechargeOperation The new recharge operation.
   */
  public void setVehicleRechargeOperation(String rechargeOperation) {
    vehicleManager.setVehicleRechargeOperation(rechargeOperation);
  }

  /**
   * Updates this' vehicle list of LoadHandlingDevices.
   * 
   * @param devices The new devices
   */
  public void setVehicleLoadHandlingDevices(List<LoadHandlingDevice> devices) {
    // If the new devices are not different from the previous ones, do nothing.
    if (Objects.equals(vehicle.getLoadHandlingDevices(), devices)) {
      return;
    }
    List<LoadHandlingDevice> devs = new LinkedList<>();
    for (LoadHandlingDevice lhd : devices) {
      devs.add(new LoadHandlingDevice(lhd));
    }
    // Otherwise update the devices list, notify listeners and let the kernel
    // know.
    synchronized (vehicle) {
      vehicle.setLoadHandlingDevices(devs);
    }
    setChanged();
    notifyObservers();
    vehicleManager.setVehicleLoadHandlingDevices(devs);
  }

  /**
   * Updates the vehicle's maximum velocity.
   * 
   * @param newVelocity The new maximum velocity.
   */
  public void setVehicleMaxVelocity(int newVelocity) {
    // If the new level is not different from the previous one, do nothing.
    if (newVelocity == vehicle.getMaxVelocity()) {
      return;
    }
    synchronized (vehicle) {
      vehicle.setMaxVelocity(newVelocity);
    }
    setChanged();
    notifyObservers();
    vehicleManager.setVehicleMaxVelocity(newVelocity);
  }

  /**
   * Updates the vehicle's maximum reverse velocity.
   * 
   * @param newVelocity The new maximum reverse velocity.
   */
  public void setVehicleMaxReverseVelocity(int newVelocity) {
    // If the new level is not different from the previous one, do nothing.
    if (newVelocity == vehicle.getMaxReverseVelocity()) {
      return;
    }
    synchronized (vehicle) {
      vehicle.setMaxReverseVelocity(newVelocity);
    }
    setChanged();
    notifyObservers();
    vehicleManager.setVehicleMaxReverseVelocity(newVelocity);
  }

  /**
   * Sets a property of the vehicle.
   * If the given value is <code>null</code>, the property is removed from the
   * vehicle.
   *
   * @param key The property's key.
   * @param value The property's (new) value. If <code>null</code>, the property
   * is removed from the vehicle.
   */
  public void setVehicleProperty(String key, String value) {
    vehicleManager.setVehicleProperty(key, value);
  }

  /**
   * Updates the vehicle state.
   * 
   * @param newState The new state
   */
  public void setVehicleState(Vehicle.State newState) {
    if (newState == null) {
      throw new IllegalStateException("newState is null");
    }
    // If the new state is not different from the previous one, do nothing.
    if (newState.equals(vehicle.getState())) {
      return;
    }
    synchronized (vehicle) {
      vehicle.setState(newState);
    }
    setChanged();
    notifyObservers();
    vehicleManager.setVehicleState(newState);
  }

  /**
   * Sets a property of the transport order the vehicle is currently processing.
   * If the given value is <code>null</code>, the property is removed from the
   * order.
   * 
   * @param key The property's key.
   * @param value The property's (new) value. If <code>null</code>, the property
   * is removed from the order.
   */
  public void setOrderProperty(String key, String value) {
    vehicleManager.setOrderProperty(key, value);
  }

  /**
   * Confirms that a given command has been successfully executed by a
   * communication adapter/vehicle.
   *
   * @param executedCommand The command that has been successfully executed.
   */
  public void commandExecuted(AdapterCommand executedCommand) {
    if (executedCommand == null) {
      throw new IllegalStateException("executedCommand is null");
    }
    vehicleManager.commandExecuted(executedCommand);
  }

  // Class-specific methods start here.
  /**
   * Sets a new communication adapter for this vehicle.
   * 
   * @param adapter The new communication adapter for this vehicle.
   */
  public void setCommunicationAdapter(BasicCommunicationAdapter adapter) {
    if (adapter == null) {
      throw new NullPointerException("adapter is null");
    }
    commAdapter = adapter;
    commAdapter.addVelocityListener(this);
    commAdapter.addView(this);
    setChanged();
    notifyObservers();
  }

  /**
   * Returns this vehicle's current communication adapter.
   * If the vehicle is currently not connected the return value is null.
   * 
   * @return This vehicle's current communication adapter.
   */
  public BasicCommunicationAdapter getCommunicationAdapter() {
    return commAdapter;
  }

  /**
   * Sets the communication factory.
   * 
   * @param factory The new factory
   */
  public void setCommunicationFactory(CommunicationAdapterFactory factory) {
    commFactory = factory;
    setChanged();
    notifyObservers(factory);
  }

  /**
   * Returns this' vehicle CommunicationAdapterFactory.
   * 
   * @return The factory
   */
  public CommunicationAdapterFactory getCommunicationFactory() {
    return commFactory;
  }

  /**
   * Removes the communication adapter assigned to this vehicle.
   */
  public void removeCommunicationAdapter() {
    if (commAdapter != null) {
      commAdapter.removeVelocityListener(this);
      commAdapter.removeView(this);
      velocityHistory.clear();
      commAdapter = null;
      commFactory = null;
      setSelectedTab(0);
      setVehicleState(Vehicle.State.UNKNOWN);
      setChanged();
      notifyObservers();
    }
  }

  /**
   * Checks whether this model has a communication adapter.
   * 
   * @return <code>true</code> if, and only if, this model's communication
   * adapter is not <code>null</code>.
   */
  public boolean hasCommunicationAdapter() {
    return commAdapter != null;
  }

  /**
   * Checks whether this model has a communication factory.
   * 
   * @return <code>true</code> if, and only if, this model's communication
   * factory is not <code>null</code>.
   */
  public boolean hasCommunicationFactory() {
    return commFactory != null;
  }

  /**
   * Returns the velocity queue listener.
   *
   * @return The velocity queue listener.
   */
  VelocityHistory getVelocityHistory() {
    return velocityHistory;
  }

  /**
   * Returns the VehicleManager.
   * 
   * @return The VehicleManager
   */
  public VehicleManager getVehicleManager() {
    return this.vehicleManager;
  }

  /**
   * Sets this communication adapter's vehicle manager.
   * 
   * @param manager The new vehicle manager.
   */
  public void setVehicleManager(VehicleManager manager) {
    log.finer("method entry");
    vehicleManager = manager;
    if (vehicleManager != null) {
      // Update the adapter and vehicle state on the kernel side.
      synchronized (vehicle) {
        vehicleManager.setAdapterState(State.CONNECTED);
        vehicleManager.setVehicleState(vehicle.getState());
      }
    }
  }

  /**
   * Returns whether this vehicle has a vehicle manager.
   * 
   * @return True if it has, false otherwise.
   */
  public boolean hasVehicleManager() {
    return vehicleManager != null;
  }

  /**
   * Sets the adapter command queue capacity.
   * 
   * @param capacity The new capacity
   */
  public void setAdapterCommandQueueCapacity(int capacity) {
    vehicleManager.setAdapterCommandQueueCapacity(capacity);
  }

  /**
   * Logs a message.
   * 
   * @param message The message to be logged
   */
  public void logMessage(Message message) {
    if (message == null) {
      throw new NullPointerException("message is null");
    }
    messages.add(message);
    setChanged();
    notifyObservers(message);
  }

  @Override
  public void update() {
    setChanged();
    notifyObservers();
  }

  @Override
  public void addVelocityValue(int velocityValue) {
    // Store the new value in the history...
    velocityHistory.addVelocityValue(velocityValue);
    // ...and let all observers know about it.
    setChanged();
    notifyObservers(velocityHistory);
  }
}
