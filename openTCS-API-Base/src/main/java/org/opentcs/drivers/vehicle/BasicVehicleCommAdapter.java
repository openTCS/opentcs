/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.drivers.vehicle;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Objects;
import static java.util.Objects.requireNonNull;
import java.util.Queue;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import org.opentcs.data.model.Vehicle;
import static org.opentcs.drivers.vehicle.VehicleProcessModel.Attribute.COMMAND_ENQUEUED;
import static org.opentcs.drivers.vehicle.VehicleProcessModel.Attribute.COMMAND_EXECUTED;
import org.opentcs.drivers.vehicle.management.VehicleProcessModelTO;
import static org.opentcs.util.Assertions.checkInRange;
import org.opentcs.util.annotations.ScheduledApiChange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A base class for communication adapters mainly providing command queue processing.
 *
 * <p>
 * Implementation notes:
 * </p>
 * <ul>
 * <li>Accessing the queues of {@link #getUnsentCommands() unsent} and
 * {@link #getSentCommands() sent} commands from outside should always be protected by
 * synchronization on the {@link BasicVehicleCommAdapter} instance.</li>
 * </ul>
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
   * The executor to run tasks on.
   */
  private final Executor executor;
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
  private final Runnable commandDispatcherTask = new CommandDispatcherTask();
  /**
   * This adapter's command queue.
   */
  private final Queue<MovementCommand> commandQueue = new LinkedBlockingQueue<>();
  /**
   * Contains the orders which have been sent to the vehicle but which haven't
   * been executed by it, yet.
   */
  private final Queue<MovementCommand> sentQueue = new LinkedBlockingQueue<>();

  /**
   * Creates a new instance.
   *
   * @param vehicleModel An observable model of the vehicle's and its comm adapter's attributes.
   * @param commandQueueCapacity The number of commands this comm adapter's command queue accepts.
   * Must be at least 1.
   * @param sentQueueCapacity The maximum number of orders to be sent to a vehicle.
   * Must be at least 1.
   * @param rechargeOperation The string to recognize as a recharge operation.
   * @param executor The executor to run tasks on.
   * @deprecated Use more specific constructor instead.
   */
  @Deprecated
  @ScheduledApiChange(when = "6.0", details = "Will be removed")
  public BasicVehicleCommAdapter(VehicleProcessModel vehicleModel,
                                 int commandQueueCapacity,
                                 int sentQueueCapacity,
                                 String rechargeOperation,
                                 Executor executor) {
    this.vehicleModel = requireNonNull(vehicleModel, "vehicleModel");
    this.commandQueueCapacity = checkInRange(commandQueueCapacity,
                                             1,
                                             Integer.MAX_VALUE,
                                             "commandQueueCapacity");
    this.sentQueueCapacity = checkInRange(sentQueueCapacity,
                                          1,
                                          Integer.MAX_VALUE,
                                          "sentQueueCapacity");
    this.rechargeOperation = requireNonNull(rechargeOperation, "rechargeOperation");
    this.executor = requireNonNull(executor, "executor");
  }

  /**
   * Creates a new instance.
   *
   * @param vehicleModel An observable model of the vehicle's and its comm adapter's attributes.
   * @param commandQueueCapacity The number of commands this comm adapter's command queue accepts.
   * Must be at least 1.
   * @param sentQueueCapacity The maximum number of orders to be sent to a vehicle.
   * Must be at least 1.
   * @param rechargeOperation The string to recognize as a recharge operation.
   * @param executor The executor to run tasks on.
   * @deprecated Use constructor with {@code commandsCapacity} parameter instead.
   */
  @Deprecated
  @ScheduledApiChange(when = "6.0", details = "Will be removed")
  public BasicVehicleCommAdapter(VehicleProcessModel vehicleModel,
                                 int commandQueueCapacity,
                                 int sentQueueCapacity,
                                 String rechargeOperation,
                                 ScheduledExecutorService executor) {
    this(vehicleModel,
         commandQueueCapacity,
         sentQueueCapacity,
         rechargeOperation,
         (Executor) executor);
  }

  /**
   * Creates a new instance.
   *
   * @param vehicleModel An observable model of the vehicle's and its comm adapter's attributes.
   * @param commandsCapacity The number of commands this comm adapter accepts. Must be at least 1.
   * @param rechargeOperation The string to recognize as a recharge operation.
   * @param executor The executor to run tasks on.
   */
  public BasicVehicleCommAdapter(VehicleProcessModel vehicleModel,
                                 int commandsCapacity,
                                 String rechargeOperation,
                                 ScheduledExecutorService executor) {
    this(vehicleModel,
         commandsCapacity,
         commandsCapacity,
         rechargeOperation,
         (Executor) executor);
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
    if (isEnabled()) {
      return;
    }
    LOG.info("Vehicle comm adapter is being enabled: {}", getName());
    connectVehicle();
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
    if (!isEnabled()) {
      return;
    }
    LOG.info("Vehicle comm adapter is being disabled: {}", getName());
    disconnectVehicle();
    enabled = false;
    // Update the vehicle's state for the rest of the system.
    getProcessModel().setCommAdapterEnabled(false);
    getProcessModel().setState(Vehicle.State.UNKNOWN);
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
  public VehicleProcessModelTO createTransferableProcessModel() {
    return createCustomTransferableProcessModel()
        .setName(getProcessModel().getName())
        .setCommAdapterConnected(getProcessModel().isCommAdapterConnected())
        .setCommAdapterEnabled(getProcessModel().isCommAdapterEnabled())
        .setEnergyLevel(getProcessModel().getEnergyLevel())
        .setLoadHandlingDevices(getProcessModel().getLoadHandlingDevices())
        .setNotifications(getProcessModel().getNotifications())
        .setOrientationAngle(getProcessModel().getOrientationAngle())
        .setPrecisePosition(getProcessModel().getPrecisePosition())
        .setPosition(getProcessModel().getPosition())
        .setState(getProcessModel().getState())
        .setLength(getProcessModel().getLength());
  }

  @Override
  public synchronized Queue<MovementCommand> getUnsentCommands() {
    return getCommandQueue();
  }

  @Override
  public synchronized Queue<MovementCommand> getSentCommands() {
    return getSentQueue();
  }

  @Override
  public int getCommandsCapacity() {
    return getCommandQueueCapacity();
  }

  @Override
  @Deprecated
  public int getCommandQueueCapacity() {
    return commandQueueCapacity;
  }

  @Override
  @Deprecated
  public synchronized Queue<MovementCommand> getCommandQueue() {
    return commandQueue;
  }

  @Override
  public boolean canAcceptNextCommand() {
    return (getUnsentCommands().size() + getSentCommands().size()) < getCommandsCapacity();
  }

  @Override
  @Deprecated
  public int getSentQueueCapacity() {
    return sentQueueCapacity;
  }

  @Override
  @Deprecated
  public synchronized Queue<MovementCommand> getSentQueue() {
    return sentQueue;
  }

  @Override
  public String getRechargeOperation() {
    return rechargeOperation;
  }

  @Override
  public synchronized boolean enqueueCommand(MovementCommand newCommand) {
    requireNonNull(newCommand, "newCommand");

    if (!canAcceptNextCommand()) {
      return false;
    }

    LOG.debug("{}: Adding command: {}", getName(), newCommand);
    getUnsentCommands().add(newCommand);
    getProcessModel().commandEnqueued(newCommand);
    return true;
  }

  @Override
  public synchronized void clearCommandQueue() {
    getUnsentCommands().clear();
    getSentCommands().clear();
  }

  @Override
  public void execute(AdapterCommand command) {
    command.execute(this);
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
    if (Objects.equals(evt.getPropertyName(), COMMAND_ENQUEUED.name())
        || Objects.equals(evt.getPropertyName(), COMMAND_EXECUTED.name())) {
      executor.execute(commandDispatcherTask);
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
   * Returns the executor to run tasks on.
   *
   * @return The executor to run tasks on.
   */
  @ScheduledApiChange(when = "6.0", details = "Will return ScheduledExectorService instead")
  public Executor getExecutor() {
    return executor;
  }

  /**
   * Converts the given command to something the vehicle can understand and sends the resulting data
   * to the vehicle.
   * <p>
   * Note that this method is called from the kernel executor and thus should not block.
   * </p>
   *
   * @param cmd The command to be sent.
   * @throws IllegalArgumentException If there was a problem with interpreting the command or
   * communicating it to the vehicle.
   */
  public abstract void sendCommand(MovementCommand cmd)
      throws IllegalArgumentException;

  /**
   * Checks whether a new command can be sent to the vehicle.
   * <p>
   * This method returns <code>true</code> only if there is at least one command in the
   * {@link #getUnsentCommands() queue of unsent commands} waiting to be sent.
   * </p>
   *
   * @return <code>true</code> if, and only if, a new command can be sent to the vehicle.
   */
  protected synchronized boolean canSendNextCommand() {
    // Since canAcceptNextCommand() already ensures that the combined sizes of the queues of unsent
    // and sent commands don't exceeed the number of commands the comm adapters can accept, the only
    // thing to do here is to check if there are any commands to be sent.
    return !getUnsentCommands().isEmpty();
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
   * vehicle is considered to be working/responding correctly.
   * </p>
   *
   * @return <code>true</code> if, and only if, the communication channel to the
   * vehicle is open.
   */
  protected abstract boolean isVehicleConnected();

  /**
   * Creates a transferable process model with the specific attributes of this comm adapter's
   * process model set.
   * <p>
   * This method should be overriden by implementing classes.
   * </p>
   *
   * @return A transferable process model.
   */
  protected VehicleProcessModelTO createCustomTransferableProcessModel() {
    return new VehicleProcessModelTO();
  }

  /**
   * The task processing the command queue.
   */
  private class CommandDispatcherTask
      implements Runnable {

    CommandDispatcherTask() {
    }

    @Override
    public void run() {
      synchronized (BasicVehicleCommAdapter.this) {
        if (!isEnabled()) {
          LOG.debug("{}: Not enabled, skipping.", getName());
          return;
        }
        if (!canSendNextCommand()) {
          LOG.debug("{}: Cannot send another command, skipping.", getName());
          return;
        }
        MovementCommand curCmd = getUnsentCommands().poll();
        if (curCmd == null) {
          LOG.debug("{}: Nothing to send, skipping.", getName());
          return;
        }
        try {
          LOG.debug("{}: Sending command: {}", getName(), curCmd);
          sendCommand(curCmd);
          // Remember that we sent this command to the vehicle.
          getSentCommands().add(curCmd);
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
