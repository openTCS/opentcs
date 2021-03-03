/*
 * openTCS copyright information:
 * Copyright (c) 2007 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.drivers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Queue;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JPanel;
import org.opentcs.data.model.Triple;
import org.opentcs.data.model.Vehicle;
import org.opentcs.util.CyclicTask;

/**
 * The base class that all communication adapters must extend.
 *
 * <h4>Synchronization:</h4>
 * <p>
 * <ul>
 * <li>Accessing the command queue/sent queue from outside should always be
 * protected by synchronization on the BasicCommunicationAdapter instance.</li>
 * </ul>
 * </p>
 *
 * @author Iryna Felko (Fraunhofer IML)
 * @author Stefan Walter (Fraunhofer IML)
 */
public abstract class BasicCommunicationAdapter
    implements CommunicationAdapter, VelocityListener {

  /**
   * This class's Logger.
   */
  private static final Logger log =
      Logger.getLogger(BasicCommunicationAdapter.class.getName());
  /**
   * This communication adapter's name.
   */
  private final String name;
  /**
   * This communication adapter's velocity listeners.
   */
  private final Set<VelocityListener> velocityListeners = new HashSet<>();
  /**
   * A set of views observing this communication adapter.
   */
  private final Set<CommunicationAdapterView> adapterViews = new HashSet<>();
  /**
   * This adapter's <em>enabled</em> flag.
   */
  private boolean enabled;
  /**
   * This adapter's current command dispatcher task.
   */
  private CyclicTask commandDispatcherTask;
  /**
   * Energy level of the vehicle controlled by this communication adapter.
   */
  private int vehicleEnergyLevel = 100;
  /**
   * Load handling devices of the vehicle controlled by this driver.
   */
  private List<LoadHandlingDevice> loadHandlingDevices = new LinkedList<>();
  /**
   * State of the vehicle controlled by this communication adapter.
   */
  private Vehicle.State vehicleState = Vehicle.State.UNKNOWN;
  /**
   * This communication adapter's <code>VehicleModel</code>.
   */
  private VehicleModel vehicleModel;
  /**
   * This adapter's command queue.
   */
  private final Queue<MovementCommand> commandQueue = new LinkedList<>();
  /**
   * This adapter's command queue's capacity.
   */
  private int commandQueueCapacity = 1;
  /**
   * Contains the orders which have been sent to the vehicle but which haven't
   * been executed by it, yet.
   */
  private final Queue<MovementCommand> sentQueue = new LinkedList<>();
  /**
   * The maximum number of orders to be sent to a vehicle.
   */
  private int sentQueueCapacity = 1;
  /**
   * The operation this adapter interprets as a command to recharge the vehicle.
   */
  private String rechargeOperation = "CHARGE";

  /**
   * Creates a new BasicCommunicationAdapter.
   *
   * @param adapterName The new communication adapter's name. Mainly used for
   * identifying the adapter in log messages.
   */
  public BasicCommunicationAdapter(final String adapterName) {
    log.finer("method entry");
    name = Objects.requireNonNull(adapterName, "adapterName is null");
  }

  // Implementation of interface CommunicationAdapter starts here.
  @Override
  public final synchronized boolean addCommand(AdapterCommand newCommand) {
    log.finer("method entry");
    Objects.requireNonNull(newCommand, "newCommand is null");

    boolean commandAdded = false;
    if (newCommand instanceof MovementCommand) {
      if (commandQueue.size() < commandQueueCapacity) {
        log.fine("Adding command: " + newCommand);
        commandQueue.add((MovementCommand) newCommand);
        commandAdded = true;
      }
    }
    else {
      log.warning("Unknown AdapterCommand implementation: "
          + newCommand.getClass());
    }
    if (commandAdded) {
      updateViews();
      this.notifyAll();
    }
    return commandAdded;
  }

  @Override
  public synchronized void clearCommandQueue() {
    log.finer("method entry");
    if (!commandQueue.isEmpty()) {
      commandQueue.clear();
      this.notifyAll();
    }
    sentQueue.clear();
  }

  @Override
  public final synchronized int getVehicleEnergyLevel() {
    return vehicleEnergyLevel;
  }

  @Override
  public synchronized List<LoadHandlingDevice> getVehicleLoadHandlingDevices() {
    return loadHandlingDevices;
  }

  @Override
  public synchronized Vehicle.State getVehicleState() {
    return vehicleState;
  }

  @Override
  public void processMessage(Object message) {
    // Do nada.
  }

  /**
   * Returns the current vehicle position.
   *
   * @return Current position
   */
  public synchronized String getVehiclePosition() {
    return vehicleModel.getPosition();
  }

  // Implementation of interface VelocityListener starts here.
  /**
   * Convenience implementation that delegates to all registered
   * <code>VelocityListener</code>s.
   *
   * @param velocityValue The new velocity value.
   */
  @Override
  public final synchronized void addVelocityValue(int velocityValue) {
    for (VelocityListener curListener : velocityListeners) {
      curListener.addVelocityValue(velocityValue);
    }
  }

  // Implementation of class-specific methods starts here.
  /**
   * Starts this communication adapter after it has been initialized.
   * Note that overriding methods should always call this implementation as it
   * does some pretty important stuff.
   */
  public synchronized void enable() {
    log.finer("method entry");
    if (!enabled) {
      connectVehicle();
      commandDispatcherTask = new CommandDispatcherTask();
      Thread commandDispatcherThread =
          new Thread(commandDispatcherTask, getName() + "-commandDispatcher");
      commandDispatcherThread.start();
      enabled = true;
      setCommandQueueCapacity(commandQueueCapacity);
      // Re-send the vehicle state.
      setVehicleRechargeOperation(rechargeOperation);
      setVehiclePrecisePosition(null);
      setVehicleOrientationAngle(Double.NaN);
      setVehicleEnergyLevel(vehicleEnergyLevel);
      setVehicleLoadHandlingDevices(loadHandlingDevices);
      setVehicleState(getVehicleState());
      updateViews();
    }
    else {
      log.warning("Already enabled, doing nothing.");
    }
  }

  /**
   * Disables this communication adapter.
   * Note that overriding methods should always call this implementation as it
   * does some pretty important stuff.
   */
  public synchronized void disable() {
    log.finer("method entry");
    if (enabled) {
      disconnectVehicle();
      commandDispatcherTask.terminate();
      commandDispatcherTask = null;
      enabled = false;
      // Update the vehicle's state for the rest of the system.
      setVehicleState(Vehicle.State.UNKNOWN);
      updateViews();
    }
    else {
      log.fine("Not enabled, doing nothing.");
    }
  }

  /**
   * Checks whether this communication adapter is enabled.
   *
   * @return <code>true</code> if, and only if, this communication adapter is
   * enabled.
   */
  public synchronized boolean isEnabled() {
    return enabled;
  }

  /**
   * Returns this communication adapter's name.
   *
   * @return This communication adapter's name.
   */
  public final String getName() {
    return name;
  }

  /**
   * Sets this communication adapter's <code>VehicleModel</code>.
   *
   * @param newModel The new <code>VehicleStatusUpdater</code>.
   */
  public final synchronized void setVehicleModel(VehicleModel newModel) {
    log.finer("method entry");
    this.vehicleModel = newModel;
  }

  /**
   * Returns a list of <code>JPanel</code>s that this communication adapter
   * offers for displaying or manipulating its custom properties.
   * <p>
   * <em>Implementation note</em>: No guarantee is being made that this method
   * will ever be called during the lifetime of a communication adapter; the
   * correct functionality of a communication adapter should thus not rely on
   * this method being called.
   * </p>
   *
   * @return A list of <code>JPanel</code>s that this communication
   * adapter offers for displaying or manipulating its custom properties. The
   * returned list may be empty if the communication adapter does not have/need
   * any custom panels; the returned value may never be <code>null</code>,
   * though.
   */
  public List<JPanel> getCustomDisplayPanels() {
    log.finer("method entry");
    return new ArrayList<>(0);
  }

  /**
   * Informs the communication adapter that the given panels are not needed any
   * more by the GUI and may be disposed.
   * @param panels The panels to be disposed.
   */
  public final synchronized void detachCustomDisplayPanels(
      Collection<JPanel> panels) {
    log.finer("method entry");
    Objects.requireNonNull(panels, "panels is null");
    // Forget the panels given so their update() methods won't be called any
    // more.
    adapterViews.removeAll(panels);
  }

  /**
   * Registers a new velocity listener with this communication adapter.
   *
   * @param l The listener to be added.
   */
  public final synchronized void addVelocityListener(VelocityListener l) {
    log.finer("method entry");
    Objects.requireNonNull(l, "l is null");
    velocityListeners.add(l);
  }

  /**
   * Unregisters a velocity listener from this communication adapter.
   *
   * @param l The listener to be removed.
   */
  public final synchronized void removeVelocityListener(VelocityListener l) {
    log.finer("method entry");
    Objects.requireNonNull(l, "l is null");
    velocityListeners.remove(l);
  }

  /**
   * Notifies the <code>VehicleStatusUpdater</code> about the vehicle's new
   * position.
   *
   * @param position The vehicle's new position.
   */
  public final synchronized void setVehiclePosition(String position) {
    log.finer("method entry");
    if (vehicleModel != null) {
      vehicleModel.setVehiclePosition(position);
    }
    else {
      log.fine("vehicleModel is null, not called");
    }
  }

  /**
   * Get the vehicle's precise position.
   * 
   * @return Precise position in mm. null if precise position not set. 
   */
  public final Triple getVehiclePrecisePosition() {
    log.finer("method entry");
    if (vehicleModel != null) {
      return vehicleModel.getVehiclePrecisePosition();
    }
    else {
      log.fine("vehicleModel is null, not called");
      return null;
    }
  }

  /**
   * Sets the vehicle's precise position in mm. (May be <code>null</code> to
   * indicate that the vehicle does not provide coordinates.)
   *
   * @param position The position.
   */
  public final synchronized void setVehiclePrecisePosition(Triple position) {
    log.finer("method entry");
    if (vehicleModel != null) {
      vehicleModel.setVehiclePrecisePosition(position);
    }
    else {
      log.fine("vehicleModel is null, not called");
    }
  }

  /**
   * Get the vehicle's current orientation angle.
   * 
   * @return Current orientation angle in deg [-360°,360°].
   *          Double.NaN if orientation angle not set.
   */
  public double getVehicleOrientationAngle() {
    return vehicleModel.getVehicleOrientationAngle();
  }

  /**
   * Sets the vehicle's orientation angle (-360..360°). May be Double.NaN if the
   * vehicle doesn't provide an angle.
   *
   * @param angle The angle.
   */
  public final synchronized void setVehicleOrientationAngle(double angle) {
    log.finer("method entry");
    final double validAngle;
    if (!Double.isNaN(angle) && (angle > 360.0 || angle < -360.0)) {
      validAngle = angle % 360.0;
      log.warning("Angle not with [-360..360]: " + angle + ", normalized to "
          + validAngle);
    }
    else {
      validAngle = angle;
    }
    if (vehicleModel != null) {
      vehicleModel.setVehicleOrientationAngle(validAngle);
    }
    else {
      log.fine("vehicleModel is null, not called");
    }
  }

  /**
   * Notifies the <code>VehicleStatusUpdater</code> about the vehicle's new
   * energy level.
   *
   * @param newLevel The vehicle's new energy level.
   */
  public final synchronized void setVehicleEnergyLevel(int newLevel) {
    log.finer("method entry");
    final int validLevel;
    // If the reported value is out of [0..100], limit it to 0 or 100.
    if (newLevel < 0 || newLevel > 100) {
      validLevel = Math.min(100, Math.max(0, newLevel));
      log.warning("Energy level not within [0..100]: " + newLevel
          + ", limited to " + validLevel);
    }
    else {
      validLevel = newLevel;
    }
    if (vehicleModel != null) {
      vehicleModel.setVehicleEnergyLevel(validLevel);
      vehicleEnergyLevel = validLevel;
    }
    else {
      log.fine("vehicleModel is null, not called");
    }
  }

  /**
   * Notifies the <code>VehicleStatusUpdater</code> about the vehicle's new
   * recharge operation.
   *
   * @param newOperation The vehicle's new recharge operation.
   */
  public final synchronized void setVehicleRechargeOperation(
      String newOperation) {
    log.finer("method entry");
    if (vehicleModel != null) {
      vehicleModel.setVehicleRechargeOperation(rechargeOperation);
    }
    else {
      log.fine("vehicleModel is null, not called");
    }
    rechargeOperation = newOperation;
  }

  /**
   * Notifies the <code>VehicleModel</code> about the vehicle's load handling
   * devices.
   *
   * @param devices The vehicle's load handling devices.
   */
  public final synchronized void setVehicleLoadHandlingDevices(
      List<LoadHandlingDevice> devices) {
    log.finer("method entry");
    if (vehicleModel != null) {
      vehicleModel.setVehicleLoadHandlingDevices(devices);
      loadHandlingDevices = devices;
    }
    else {
      log.fine("vehicleModel is null, not called");
    }
  }

  /**
   * Notifies the <code>VehicleStatusUpdater</code> about the vehicle's new
   * maximum velocity.
   *
   * @param newVelocity The vehicle's new maximum velocity.
   */
  public final synchronized void setVehicleMaxVelocity(int newVelocity) {
    log.finer("method entry");
    if (vehicleModel != null) {
      vehicleModel.setVehicleMaxVelocity(newVelocity);
    }
    else {
      log.fine("vehicleModel is null, not called");
    }
  }

  /**
   * Notifies the <code>VehicleStatusUpdater</code> about the vehicle's new
   * maximum reverse velocity.
   *
   * @param newVelocity The vehicle's new maximum reverse velocity.
   */
  public final synchronized void setVehicleMaxReverseVelocity(int newVelocity) {
    log.finer("method entry");
    if (vehicleModel != null) {
      vehicleModel.setVehicleMaxReverseVelocity(newVelocity);
    }
    else {
      log.fine("vehicleModel is null, not called");
    }
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
  public final synchronized void setVehicleProperty(String key, String value) {
    log.finer("method entry");
    vehicleModel.setVehicleProperty(key, value);
  }

  /**
   * Notifies the <code>VehicleStatusUpdater</code> about the vehicle's new
   * state.
   *
   * @param newState The vehicle's new state.
   */
  public final synchronized void setVehicleState(Vehicle.State newState) {
    log.finer("method entry");
    if (!vehicleState.equals(newState)) {
      if (!Vehicle.State.ERROR.equals(vehicleState)
          && Vehicle.State.ERROR.equals(newState)) {
        logMessage("Vehicle state changed to ERROR", Message.Type.INFO);
      }
      else if (Vehicle.State.ERROR.equals(vehicleState)
          && !Vehicle.State.ERROR.equals(newState)) {
        logMessage("Vehicle state is no longer ERROR", Message.Type.INFO);
      }
      vehicleState = newState;
      if (vehicleModel != null) {
        vehicleModel.setVehicleState(newState);
      }
      else {
        log.fine("vehicleModel is null, not called");
      }
    }
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
  public final synchronized void setOrderProperty(String key, String value) {
    log.finer("method entry");
    vehicleModel.setOrderProperty(key, value);
  }

  /**
   * Confirms that a given command has been successfully executed by the
   * communication adapter/vehicle.
   *
   * @param executedCommand The command that has been successfully executed.
   */
  protected final synchronized void commandExecuted(
      AdapterCommand executedCommand) {
    log.finer("method entry");
    if (vehicleModel != null) {
      vehicleModel.commandExecuted(executedCommand);
    }
    else {
      log.warning("vehicleModel is null, not called");
    }
    // Notify the command dispatcher task so it can send the next order if one
    // is available.
    this.notify();
  }

  /**
   * Returns this adapter's command queue.
   *
   * @return This adapter's command queue.
   */
  public final synchronized Queue<MovementCommand> getCommandQueue() {
    return commandQueue;
  }

  /**
   * Returns this adapter's command queue's capacity.
   *
   * @return This adapter's command queue's capacity.
   */
  public final synchronized int getCommandQueueCapacity() {
    return commandQueueCapacity;
  }

  /**
   * Informs the vehicle manager about the capacity of the communication
   * adapter's command queue.
   *
   * @param capacity The communication adapter's command queue capacity.
   */
  public final synchronized void setCommandQueueCapacity(int capacity) {
    log.finer("method entry");
    commandQueueCapacity = capacity;
    if (vehicleModel != null) {
      vehicleModel.setAdapterCommandQueueCapacity(capacity);
    }
    else {
      log.fine("vehicleModel is null, not called");
    }
  }

  /**
   * Returns a queue containing the commands that this adapter has sent to the
   * vehicle already but which have not yet been processed by it.
   *
   * @return A queue containing the commands that this adapter has sent to the
   * vehicle already but which have not yet been processed by it.
   */
  public final synchronized Queue<MovementCommand> getSentQueue() {
    return sentQueue;
  }

  /**
   * Returns the capacity of this adapter's <em>sent queue</em>.
   *
   * @return The capacity of this adapter's <em>sent queue</em>.
   */
  public final synchronized int getSentQueueCapacity() {
    return sentQueueCapacity;
  }

  /**
   * Sets the capacity of this adapter's <em>sent queue</em>.
   *
   * @param newCapacity The new capacity of this adapter's <em>sent
   * queue</em>.
   */
  public final synchronized void setSentQueueCapacity(int newCapacity) {
    sentQueueCapacity = newCapacity;
  }

  /**
   * Logs a message from the communication adapter.
   *
   * @param message The message to be logged.
   */
  protected final synchronized void logMessage(Message message) {
    log.finer("method entry");
    if (vehicleModel != null) {
      vehicleModel.logMessage(message);
    }
    else {
      log.fine("vehicleModel is null, not called");
    }
  }

  /**
   * Logs a message from the communication adapter.
   *
   * @param message The message to be logged.
   * @param msgType The type of the message.
   */
  protected final synchronized void logMessage(String message,
                                               Message.Type msgType) {
    log.finer("method entry");
    if (vehicleModel != null) {
      vehicleModel.logMessage(new Message(message, msgType));
    }
    else {
      log.fine("vehicleModel is null, not called");
    }
  }

  /**
   * Adds a view to this communication adapter.
   * The view will be updated on all subsequent calls of
   * <code>updateViews()</code>.
   *
   * @param newView The view to be added.
   */
  public final synchronized void addView(CommunicationAdapterView newView) {
    Objects.requireNonNull(newView, "newView is null");
    adapterViews.add(newView);
  }

  /**
   * Removes a view from this communication adapter.
   * After calling this method, the given view will not be updated on calls of
   * <code>updateViews()</code> any more.
   *
   * @param rmView The view to be removed.
   */
  public final synchronized void removeView(CommunicationAdapterView rmView) {
    Objects.requireNonNull(rmView, "rmView is null");
    adapterViews.remove(rmView);
  }

  /**
   * Updates all registered views.
   */
  protected final synchronized void updateViews() {
    for (CommunicationAdapterView curView : adapterViews) {
      curView.update();
    }
  }

  /**
   * Checks whether a new command can be sent to the vehicle.
   * The default implementation of this method returns <code>true</code> only if
   * the number of commands sent already is less than the vehicle's capacity and
   * there is at least one command in the queue that is waiting to be sent.
   *
   * @return <code>true</code> if, and only if, a new command can be sent to the
   * vehicle.
   */
  protected synchronized boolean canSendNextCommand() {
    return (getSentQueue().size() < sentQueueCapacity)
        && !getCommandQueue().isEmpty();
  }

  // Abstract methods start here.
  /**
   * Initiates a communication channel to the vehicle.
   * This method should not block, i.e. it should not wait for the actual
   * connection to be established, as the vehicle could be temporarily absent
   * or not responding at all. If that's the case, the communication adapter
   * should continue trying to establish a connection until successful or until
   * <code>disconnectVehicle</code> is called.
   */
  protected abstract void connectVehicle();

  /**
   * Closes the communication channel to the vehicle.
   */
  protected abstract void disconnectVehicle();

  /**
   * Checks whether the communication channel to the vehicle is open.
   * <p>
   * Note that the return value of this method does <em>not</em> indicate
   * whether communication with the vehicle is currently alive and/or if the
   * vehicle is considered to be working/responding correctly. For that
   * information, see <code>isVehicleAlive()</code>.
   * </p>
   *
   * @return <code>true</code> if, and only if, the communication channel to the
   * vehicle is open.
   */
  protected abstract boolean isVehicleConnected();

  /**
   * Checks whether communication with the vehicle is considered to still be
   * alive/working.
   *
   * @return <code>true</code> if, and only if, the implementing communication
   * adapter considers communication with the vehicle to still be alive.
   */
  public abstract boolean isVehicleAlive();

  /**
   * Converts the given command to something the vehicle can understand and
   * sends the resulting data to the vehicle.
   *
   * @param cmd The command to be sent.
   */
  public abstract void sendCommand(MovementCommand cmd);

  /**
   * The task processing the command queue.
   */
  private final class CommandDispatcherTask
      extends CyclicTask {

    /**
     * Creates a new CommandDispatcherTask.
     */
    private CommandDispatcherTask() {
      super(0);
    }

    @Override
    protected void runActualTask() {
      MovementCommand curCmd;
      synchronized (BasicCommunicationAdapter.this) {
        // Wait until we're terminated or we can send the next command.
        while (!isTerminated() && !canSendNextCommand()) {
          try {
            // Wait at most one second so we can still regularly check if this
            // task has been terminated.
            BasicCommunicationAdapter.this.wait(1000);
          }
          catch (InterruptedException exc) {
            log.log(Level.WARNING, "Unexpectedly interrupted", exc);
          }
        }
        if (!isTerminated()) {
          curCmd = getCommandQueue().poll();
          if (curCmd != null) {
            sendCommand(curCmd);
            // Remember that we sent this command to the vehicle.
            sentQueue.add(curCmd);
            // Update all views on this communication adapter.
            updateViews();
          }
        }
      }
    }
  }
}
