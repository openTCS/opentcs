/*
 * openTCS copyright information:
 * Copyright (c) 2007 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.drivers.vehicle;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import static java.util.Objects.requireNonNull;
import java.util.Queue;
import org.opentcs.data.model.Vehicle;
import static org.opentcs.util.Assertions.checkInRange;
import org.opentcs.util.CyclicTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A base class for communication adapters mainly providing command queue processing.
 *
 * <p>
 * Implementation notes:
 * </p>
 * <ul>
 * <li>Accessing the command queue/sent queue from outside should always be
 * protected by synchronization on the BasicVehicleCommunicationAdapter instance.</li>
 * </ul>
 *
 * @author Iryna Felko (Fraunhofer IML)
 * @author Stefan Walter (Fraunhofer IML)
 */
public abstract class BasicVehicleCommAdapter
    implements VehicleCommAdapter,
               PropertyChangeListener {

  /**
   * This class's Logger.
   */
  private static final Logger LOG = LoggerFactory.getLogger(BasicVehicleCommAdapter.class);
  /**
   * An observable model of the vehicle's and its comm adapter's attributes.
   */
  private final VehicleProcessModel vehicleModel;
  /**
   * The number of commands this adapter's command queue accepts.
   */
  private final int commandQueueCapacity;
  /**
   * The maximum number of orders to be sent to a vehicle.
   */
  private final int sentQueueCapacity;
  /**
   * The string to recognize as a recharge operation.
   */
  private final String rechargeOperation;
  /**
   * This adapter's panels.
   */
  private final List<VehicleCommAdapterPanel> adapterPanels = new LinkedList<>();
  /**
   * Indicates whether this adapter is initialized.
   */
  private boolean initialized;
  /**
   * This adapter's <em>enabled</em> flag.
   */
  private boolean enabled;
  /**
   * This adapter's current command dispatcher task.
   */
  private CyclicTask commandDispatcherTask;
  /**
   * This adapter's command queue.
   */
  private final Queue<MovementCommand> commandQueue = new LinkedList<>();
  /**
   * Contains the orders which have been sent to the vehicle but which haven't
   * been executed by it, yet.
   */
  private final Queue<MovementCommand> sentQueue = new LinkedList<>();

  /**
   * Creates a new instance.
   *
   * @param vehicleModel An observable model of the vehicle's and its comm adapter's attributes.
   * @param commandQueueCapacity The number of commands this comm adapter's command queue accepts.
   * Must be at least 1.
   * @param sentQueueCapacity The maximum number of orders to be sent to a vehicle.
   * @param rechargeOperation The string to recognize as a recharge operation.
   */
  public BasicVehicleCommAdapter(VehicleProcessModel vehicleModel,
                                 int commandQueueCapacity,
                                 int sentQueueCapacity,
                                 String rechargeOperation) {
    this.vehicleModel = requireNonNull(vehicleModel, "vehicleModel");
    this.commandQueueCapacity = checkInRange(commandQueueCapacity,
                                             0,
                                             Integer.MAX_VALUE,
                                             "commandQueueCapacity");
    this.sentQueueCapacity = checkInRange(sentQueueCapacity,
                                          0,
                                          Integer.MAX_VALUE,
                                          "sentQueueCapacity");
    this.rechargeOperation = requireNonNull(rechargeOperation, "rechargeOperation");
  }

  /**
   * {@inheritDoc}
   * <p>
   * <em>Overriding methods are expected to call this implementation, too.</em>
   * </p>
   */
  @Override
  public void initialize() {
    if (initialized) {
      LOG.debug("{}: Already initialized.", getName());
      return;
    }
    getProcessModel().addPropertyChangeListener(this);
    for (VehicleCommAdapterPanel panel : createAdapterPanels()) {
      adapterPanels.add(panel);
      getProcessModel().addPropertyChangeListener(panel);
    }
    this.initialized = true;
  }

  /**
   * {@inheritDoc}
   * <p>
   * <em>Overriding methods are expected to call this implementation, too.</em>
   * </p>
   */
  @Override
  public void terminate() {
    if (!initialized) {
      LOG.debug("{}: Not initialized.", getName());
      return;
    }
    for (VehicleCommAdapterPanel panel : adapterPanels) {
      getProcessModel().removePropertyChangeListener(panel);
    }
    adapterPanels.clear();
    getProcessModel().removePropertyChangeListener(this);
    this.initialized = false;
  }

  @Override
  public boolean isInitialized() {
    return initialized;
  }

  /**
   * {@inheritDoc}
   * <p>
   * <em>Overriding methods are expected to call this implementation, too.</em>
   * </p>
   */
  @Override
  public synchronized void enable() {
    if (enabled) {
      LOG.debug("{}: Already enabled, doing nothing.", getName());
      return;
    }
    connectVehicle();
    commandDispatcherTask = new CommandDispatcherTask();
    Thread commandDispatcherThread = new Thread(commandDispatcherTask,
                                                getName() + "-commandDispatcher");
    commandDispatcherThread.start();
    enabled = true;
    getProcessModel().setCommAdapterEnabled(true);
  }

  /**
   * {@inheritDoc}
   * <p>
   * <em>Overriding methods are expected to call this implementation, too.</em>
   * </p>
   */
  @Override
  public synchronized void disable() {
    if (!enabled) {
      LOG.debug("{}: Not enabled, doing nothing.", getName());
      return;
    }
    disconnectVehicle();
    commandDispatcherTask.terminate();
    commandDispatcherTask = null;
    enabled = false;
    // Update the vehicle's state for the rest of the system.
    getProcessModel().setCommAdapterEnabled(false);
    getProcessModel().setVehicleState(Vehicle.State.UNKNOWN);
  }

  @Override
  public synchronized boolean isEnabled() {
    return enabled;
  }

  @Override
  public VehicleProcessModel getProcessModel() {
    return vehicleModel;
  }

  @Override
  public int getCommandQueueCapacity() {
    return commandQueueCapacity;
  }

  @Override
  public synchronized Queue<MovementCommand> getCommandQueue() {
    return commandQueue;
  }

  @Override
  public int getSentQueueCapacity() {
    return sentQueueCapacity;
  }

  @Override
  public synchronized Queue<MovementCommand> getSentQueue() {
    return sentQueue;
  }

  @Override
  public String getRechargeOperation() {
    return rechargeOperation;
  }

  @Override
  public List<VehicleCommAdapterPanel> getAdapterPanels() {
    return adapterPanels;
  }

  @Override
  public synchronized boolean enqueueCommand(MovementCommand newCommand) {
    requireNonNull(newCommand, "newCommand");

    boolean commandAdded = false;
    if (commandQueue.size() < getCommandQueueCapacity()) {
      LOG.debug("{}: Adding command: {}", getName(), newCommand);
      commandQueue.add(newCommand);
      commandAdded = true;
    }
    if (commandAdded) {
      getProcessModel().commandEnqueued(newCommand);
      triggerCommandDispatcherTask();
    }
    return commandAdded;
  }

  @Override
  public synchronized void clearCommandQueue() {
    if (!commandQueue.isEmpty()) {
      commandQueue.clear();
      triggerCommandDispatcherTask();
    }
    sentQueue.clear();
  }

  /**
   * Processes updates of the {@link VehicleProcessModel}.
   *
   * <p>
   * <em>Overriding methods should also call this.</em>
   * </p>
   *
   * @param evt The property change event published by the model.
   */
  @Override
  public void propertyChange(PropertyChangeEvent evt) {
    // If a command was executed by this comm adapter, wake up the command dispatcher task to see if
    // we should send another command to the vehicle.
    if (Objects.equals(evt.getPropertyName(),
                       VehicleProcessModel.Attribute.COMMAND_EXECUTED.name())) {
      triggerCommandDispatcherTask();
    }
  }

  /**
   * Returns this communication adapter's name.
   *
   * @return This communication adapter's name.
   */
  public String getName() {
    return getProcessModel().getName();
  }

  /**
   * Converts the given command to something the vehicle can understand and
   * sends the resulting data to the vehicle.
   *
   * @param cmd The command to be sent.
   * @throws IllegalArgumentException If there was a problem with interpreting the command or
   * communicating it to the vehicle.
   */
  public abstract void sendCommand(MovementCommand cmd)
      throws IllegalArgumentException;

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
    return (getSentQueue().size() < sentQueueCapacity) && !getCommandQueue().isEmpty();
  }

  // Abstract methods start here.
  /**
   * Creates and returns a list of panels this comm adapter provides for interacting with it.
   * <p>
   * The panels are implicitly registered as observers of this comm adapter's {@link VehicleProcessModel}
   * in {@link #initialize()} and unregistered in {@link #terminate()}.
   * </p>
   *
   * @return The list of panels.
   */
  protected abstract List<VehicleCommAdapterPanel> createAdapterPanels();

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
   * vehicle is considered to be working/responding correctly.
   * </p>
   *
   * @return <code>true</code> if, and only if, the communication channel to the
   * vehicle is open.
   */
  protected abstract boolean isVehicleConnected();

  private synchronized void triggerCommandDispatcherTask() {
    this.notifyAll();
  }

  /**
   * The task processing the command queue.
   */
  private class CommandDispatcherTask
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
      synchronized (BasicVehicleCommAdapter.this) {
        // Wait until we're terminated or we can send the next command.
        while (!isTerminated() && !canSendNextCommand()) {
          try {
            // Wait at most one second so we can still periodically check if this task has been
            // terminated.
            BasicVehicleCommAdapter.this.wait(1000);
          }
          catch (InterruptedException exc) {
            LOG.warn("{}: Unexpectedly interrupted", getName(), exc);
          }
        }
        if (!isTerminated()) {
          curCmd = getCommandQueue().poll();
          if (curCmd != null) {
            try {
              sendCommand(curCmd);
              // Remember that we sent this command to the vehicle.
              sentQueue.add(curCmd);
              // Notify listeners that this command was sent.
              getProcessModel().commandSent(curCmd);
            }
            catch (IllegalArgumentException exc) {
              // Notify listeners that this command failed.
              LOG.warn("{}: Failed sending command {}", getName(), curCmd, exc);
              getProcessModel().commandFailed(curCmd);
            }
          }
        }
      }
    }
  }
}
