/*
 * openTCS copyright information:
 * Copyright (c) 2006 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.virtualvehicle;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import javax.swing.JPanel;
import org.opentcs.data.model.Point;
import org.opentcs.data.model.Vehicle;
import org.opentcs.data.model.Vehicle.Orientation;
import org.opentcs.data.order.Route.Step;
import org.opentcs.drivers.BasicCommunicationAdapter;
import org.opentcs.drivers.LoadHandlingDevice;
import org.opentcs.drivers.messages.LimitSpeed;
import org.opentcs.drivers.MovementCommand;
import org.opentcs.drivers.Processability;
import org.opentcs.drivers.SimCommunicationAdapter;
import org.opentcs.util.CyclicTask;
import org.opentcs.util.configuration.ConfigurationStore;
import org.opentcs.virtualvehicle.VelocityController.WayEntry;

/**
 * An implementation of the interface <code>LocalCommunicationAdapter</code>
 * that does not really communicate with a physical vehicle
 * (but pretends to do so).
 * <hr>
 *
 * <h4>Configuration entries</h4>
 * <dl>
 * <dt><b>queueCapacity:</b></dt>
 * <dd>This adapter's command queue's capacity.</dd>
 * </dl>
 * <hr>
 * @author Stefan Walter (Fraunhofer IML)
 */
public class LoopbackCommunicationAdapter
    extends BasicCommunicationAdapter
    implements SimCommunicationAdapter {

  /**
   * This class's Logger.
   */
  private static final Logger log =
      Logger.getLogger(LoopbackCommunicationAdapter.class.getName());
  /**
   * This class's ConfigurationStore.
   */
  private static final ConfigurationStore configStore =
      ConfigurationStore.getStore(LoopbackCommunicationAdapter.class.getName());
  /**
   * The time by which to advance the velocity controller per step (in ms).
   */
  private static final int advanceTime = 100;
  /**
   * The vehicle's recharge operation.
   */
  private final String rechargeOperation;
  /**
   * The energy storage of this vehicle.
   */
  private EnergyStorage energyStorage = new StaticEnergyStorage(1000);
  /**
   * The velocity controller for calculating the simulated vehicle's velocity
   * and current position.
   */
  private final VelocityController velocityController;
  /**
   * This driver's simulation time factor.
   */
  private double simTimeFactor = 1.0;
  /**
   * The task simulating the virtual vehicle's behaviour.
   */
  private CyclicTask vehicleSimulationTask;
  /**
   * Additional specifications for operations. 
   */
  private final Map<String, OperationSpec> operationSpecs = new HashMap<>();
  /**
   * The time needed for executing operations without explicit operation times. 
   */
  private int defaultOperatingTime = 5000;
  /**
   * Indicates whether this communication adapter is in single step mode or not
   * (i.e. in automatic mode). This flag is initialized with false because we
   * always start execution in automatic mode.
   */
  private boolean singleStepModeEnabled;
  /**
   * The boolean flag to check if execution of the next command is allowed.
   */
  private boolean executionAllowed;
  /**
   * A set of points that exist in the model currently loaded by the kernel.
   */
  private Set<Point> modelPoints = new HashSet<>();
  /**
   * Amount of energy that is consumed during Movement per second. [W]
   */
  private double movementPower;
  /**
   * Amount of energy that is consumed during Operation per second. [W]
   */
  private double operationPower;
  /**
   * Amount of energy that is consumed during idle state per second. [W]
   */
  private double idlePower;

  /**
   * Creates a new LoopbackCommunicationAdapter.
   *
   * @param adapterName The new communication adapter's name. Mainly used for
   * identifying the adapter in log messages.
   * @throws IllegalArgumentException If <code>queueCapacity</code> is less than
   * 1.
   */
  public LoopbackCommunicationAdapter(final String adapterName) {
    super(adapterName);
    log.finer("method entry");
    rechargeOperation = configStore.getString("rechargeOperation", "CHARGE");
    setVehicleRechargeOperation(rechargeOperation);
    int queueCapacity = configStore.getInt("queueCapacity", 2);
    if (queueCapacity < 1) {
      throw new IllegalArgumentException("queueCapacity is less than 1");
    }
    setCommandQueueCapacity(queueCapacity);
    velocityController = new VelocityController(-500, 500, -1000, 1000);
    velocityController.addVelocityListener(this);
  }

  // Implementation of abstract parent BasicCommunicationAdapter starts here.
  @Override
  public synchronized void enable() {
    if (!isEnabled()) {
      // Create task for vehicle simulation.
      vehicleSimulationTask = new VehicleSimulationTask();
      Thread simThread = new Thread(vehicleSimulationTask,
                                    getName() + "-simulationTask");

      simThread.start();
    }
    super.enable();
  }

  @Override
  public synchronized void disable() {
    if (isEnabled()) {
      // Disable vehicle simulation.
      vehicleSimulationTask.terminate();
      vehicleSimulationTask = null;
    }
    super.disable();
  }

  @Override
  public synchronized List<JPanel> getCustomDisplayPanels() {
    List<JPanel> panelList = new LinkedList<>();
    LoopbackCommunicationAdapterPanel panel =
        new LoopbackCommunicationAdapterPanel(this);
    panelList.add(panel);
    addView(panel);
    return panelList;
  }

  @Override
  protected synchronized void connectVehicle() {
    log.finer("method entry");
  }

  @Override
  protected synchronized void disconnectVehicle() {
    log.finer("method entry");
  }

  @Override
  protected synchronized boolean isVehicleConnected() {
    return true;
  }

  @Override
  public synchronized boolean isVehicleAlive() {
    return isEnabled();
  }

  // Implementation of interface CommunicationAdapter starts here.
  @Override
  public synchronized Processability canProcess(List<String> operations) {
    if (operations == null) {
      throw new NullPointerException("operations is null");
    }
    final boolean canProcess = isEnabled();
    final String reason = canProcess ? "" : "adapter not enabled";
    return new Processability(canProcess, reason);
  }

  // Implementation of interface SimulatingCommunicationAdapter starts here.
  @Override
  public synchronized void setSimTimeFactor(double factor) {
    if (factor <= 0.0) {
      throw new IllegalArgumentException("Illegal factor value: " + factor);
    }
    simTimeFactor = factor;
  }

  /**
   * Get the current absolute energy value of the simulated {@link EnergyStorage}.
   * @return the current energy in Ws
   */
  public double getEnergy() {
    return energyStorage.getEnergy();
  }

  /**
   * Get the current energy level of the simulated {@link EnergyStorage}.
   * @return the current energy level in %
   */
  public int getEnergyLevel() {
    return energyStorage.getEnergyLevel();
  }

  /**
   * Get the energy capacity of the simulated {@link EnergyStorage}.
   * @return the energy capacity
   */
  public double getEnergyCapacity() {
    return energyStorage.getCapacity();
  }

  // Class-specific code starts here.
  /**
   * Sets this communication adapter's <em>single step mode</em> flag.
   *
   * @param mode If <code>true</code>, sets this adapter to single step mode,
   * otherwise sets this adapter to flow mode.
   */
  public synchronized void setSingleStepModeEnabled(final boolean mode) {
    log.finer("method entry");
    singleStepModeEnabled = mode;
    updateViews();
  }

  /**
   * Returns this communication adapter's <em>single step mode</em> flag.
   *
   * @return <code>true</code> if, and only if, this adapter is currently in
   * single step mode.
   */
  public synchronized boolean isSingleStepModeEnabled() {
    log.finer("method entry");
    return singleStepModeEnabled;
  }

  /**
   * Triggers the <code>VehicleCommunicatorTask</code> for execution in single
   * step mode.
   */
  synchronized void trigger() {
    log.finer("method entry");
    executionAllowed = true;
  }

  /**
   * Returns the additional operation specifications. 
   * 
   * @return  operation specification 
   */
  public synchronized Map<String, OperationSpec> getOperationSpecs() {
    return operationSpecs;
  }

  /**
   * Sets the operation specifications.
   * 
   * @param operationSpecs The new operation specifications.
   */
  public synchronized void setOperationSpecs(Map<String, OperationSpec> operationSpecs) {
    if (operationSpecs == null) {
      throw new NullPointerException("operationSpec is null");
    }
    this.operationSpecs.clear();
    this.operationSpecs.putAll(operationSpecs);
    updateViews();
  }

  /**
   * Returns the default operating time.
   * 
   * @return The default operating time
   */
  public synchronized int getDefaultOperatingTime() {
    return defaultOperatingTime;
  }

  /**
   * Sets the default operating time.
   * 
   * @param defaultOperatingTime The new default operating time
   */
  public synchronized void setDefaultOperatingTime(int defaultOperatingTime) {
    this.defaultOperatingTime = defaultOperatingTime;
    updateViews();
  }

  /**
   * Returns the maximum deceleration.
   * 
   * @return The maximum deceleration
   */
  public synchronized int getMaxDeceleration() {
    return velocityController.getMaxDeceleration();
  }

  /**
   * Sets the maximum deceleration.
   * 
   * @param maxDeceleration The new maximum deceleration
   */
  public synchronized void setMaxDeceleration(int maxDeceleration) {
    velocityController.setMaxDeceleration(maxDeceleration);
    updateViews();
  }

  /**
   * Returns the maximum acceleration.
   * 
   * @return The maximum acceleration
   */
  public synchronized int getMaxAcceleration() {
    return velocityController.getMaxAcceleration();
  }

  /**
   * Sets the maximum acceleration.
   * 
   * @param maxAcceleration The new maximum acceleration
   */
  public synchronized void setMaxAcceleration(int maxAcceleration) {
    velocityController.setMaxAcceleration(maxAcceleration);
    updateViews();
  }

  /**
   * Returns the maximum reverse velocity.
   * 
   * @return The maximum reverse velocity.
   */
  public synchronized int getMaxRevVelocity() {
    return velocityController.getMaxRevVelocity();
  }

  /**
   * Sets the maximum reverse velocity.
   * 
   * @param maxRevVelocity The new maximum reverse velocity
   */
  public synchronized void setMaxRevVelocity(int maxRevVelocity) {
    velocityController.setMaxRevVelocity(maxRevVelocity);
    updateViews();
  }

  /**
   * Returns the maximum forward velocity.
   * 
   * @return The maximum forward velocity.
   */
  public synchronized int getMaxFwdVelocity() {
    return velocityController.getMaxFwdVelocity();
  }

  /**
   * Sets the maximum forward velocity.
   * 
   * @param maxFwdVelocity The new maximum forward velocity.
   */
  public synchronized void setMaxFwdVelocity(int maxFwdVelocity) {
    velocityController.setMaxFwdVelocity(maxFwdVelocity);
    updateViews();
  }

  /**
   * Returns whether the vehicle is paused.
   * 
   * @return paused
   */
  public synchronized boolean isVehiclePaused() {
    return velocityController.isVehiclePaused();
  }

  /**
   * Pause the vehicle (i.e. set it's velocity to zero).
   * 
   * @param  pause True, if vehicle shall be paused. False, otherwise.
   */
  public synchronized void setVehiclePaused(boolean pause) {
    velocityController.setVehiclePaused(pause);
  }

  /**
   * Returns the model points.
   * 
   * @return The model points
   */
  public synchronized Set<Point> getModelPoints() {
    return modelPoints;
  }

  /**
   * Sets the model points.
   * 
   * @param modelPoints The model points
   */
  public synchronized void setModelPoints(Set<Point> modelPoints) {
    this.modelPoints = modelPoints;
    updateViews();
  }

  @Override
  protected synchronized boolean canSendNextCommand() {
    return super.canSendNextCommand()
        && (!singleStepModeEnabled || executionAllowed);
  }

  @Override
  public synchronized void sendCommand(MovementCommand cmd) {
    assert cmd != null;

    // Reset the execution flag for single-step mode.
    executionAllowed = false;
    // Don't do anything else - the command will be put into the sentQueue
    // automatically, where it will be picked up by the simulation task.
  }

  @Override
  public synchronized void initVehiclePosition(String newPos) {
    super.setVehiclePosition(newPos);
    updateViews();
  }

  /**
   * Sets the energy storage for this vehicle.
   * 
   * @param energyStorage The energy storage for the vehicle to simulate
   */
  void setEnergyStorage(EnergyStorage energyStorage) {
    this.energyStorage = energyStorage;
  }

  /**
   * Get the energy storage of this vehicle.
   * 
   * @return The energy storage of this vehicle.
   */
  EnergyStorage getEnergyStorage() {
    return energyStorage;
  }

  @Override
  public void processMessage(Object message) {
    // Process LimitSpeeed message which might pause the vehicle.
    if (message instanceof LimitSpeed) {
      LimitSpeed lsMessage = (LimitSpeed) message;
      int speed = lsMessage.getSpeed();
      setVehiclePaused(speed == 0);
      updateViews();
    }
  }

  /**
   * Set the energy that is consumed during movement per second.
   * @param power power in W
   */
  public void setMovementPower(double power) {
    movementPower = power;
  }

  /**
   * Get the energy that is consumed during movement per second.
   * @return power in W
   */
  public double getMovementPower() {
    return movementPower;
  }

  /**
   * Set the energy that is consumed during operation per second.
   * @param power power in W
   */
  public void setOperationPower(double power) {
    operationPower = power;
  }

  /**
   * Get the energy that is consumed during operation per second.
   * @return power in W
   */
  public double getOperationPower() {
    return operationPower;
  }

  /**
   * Set the energy that is consumed during idle state per second.
   * @param power power in W
   */
  public void setIdlePower(double power) {
    idlePower = power;
  }

  /**
   * Get the energy that is consumed during idle state per second.
   * @return power in W
   */
  public double getIdlePower() {
    return idlePower;
  }

  /**
   * Set the energy of vehicle.
   * @param energy Energy level in percentage.
   */
  public void setEnergyLevel(int energy) {
    // Set new energy level in VehicleModel
    setVehicleEnergyLevel(energy);
    // Set new energy in energyStorage
    energyStorage.setEnergyLevel(energy);
  }

  /**
   * A task simulating a vehicle's behaviour.
   */
  private final class VehicleSimulationTask
      extends CyclicTask {

    /**
     * The time that has passed for the velocity controller whenever
     * <em>advanceTime</em> has passed for real.
     */
    private int simAdvanceTime;
    /**
     * Energy level of the energy storage, the last time it was
     * changed by task.
     */
    private int lastEnergyLevel = energyStorage.getEnergyLevel();

    /**
     * Creates a new VehicleSimluationTask.
     */
    private VehicleSimulationTask() {
      super(0);
    }

    /**
     * Discharge the vehicle's {@code energyStorage} by the given amount over 
     * {@code simAdvanceTime}.
     * Views and the {@code BasicCommunicationAdapter} are updated if needed. 
     * @param power power in W
     */
    private void dischargeEnergy(double power) {
      energyStorage.discharge(power, simAdvanceTime);
      int currentEnergyLevel = energyStorage.getEnergyLevel();
      // Update energy level only if changed
      if (currentEnergyLevel != lastEnergyLevel) {
        setVehicleEnergyLevel(currentEnergyLevel);
        updateViews();
        lastEnergyLevel = currentEnergyLevel;
      }
    }

    /**
     * Charge the vehicle's {@code energyStorage} by the given amount over 
     * {@code simAdvanceTime}.
     * Views and the {@code BasicCommunicationAdapter} are updated if needed. 
     * @param power power in W
     */
    private void chargeEnergy(double power) {
      energyStorage.charge(power, simAdvanceTime);
      int currentEnergyLevel = energyStorage.getEnergyLevel();
      // Update energy level only if changed
      if (currentEnergyLevel != lastEnergyLevel) {
        setVehicleEnergyLevel(currentEnergyLevel);
        updateViews();
        lastEnergyLevel = currentEnergyLevel;
      }
    }

    @Override
    protected void runActualTask() {
      try {
        // Don't do anything if no energy left
        if (energyStorage.getEnergy() <= 0.0) {
          Thread.sleep(advanceTime);
          velocityController.advanceTime(simAdvanceTime);
          return;
        }
        final MovementCommand curCommand;
        synchronized (LoopbackCommunicationAdapter.this) {
          curCommand = getSentQueue().peek();
        }
        simAdvanceTime = (int) (advanceTime * simTimeFactor);
        if (curCommand == null) {
          Thread.sleep(advanceTime);
          dischargeEnergy(idlePower);
          velocityController.advanceTime(simAdvanceTime);
        }
        else {
          // If we were told to move somewhere, simulate the journey.
          log.config("Processing MovementCommand");
          final Step curStep = curCommand.getStep();
          // Simulate the movement.
          simulateMovement(curStep);
          // Simulate processing of an operation.
          if (!curCommand.isWithoutOperation()) {
            simulateOperation(curCommand.getOperation());
          }
          log.config("Processed MovementCommand");
          if (!isTerminated()) {
            // Set the vehicle's state back to IDLE, but only if there aren't 
            // any more movements to be processed.
            if (getSentQueue().size() <= 1 && getCommandQueue().isEmpty()) {
              setVehicleState(Vehicle.State.IDLE);
            }
            // Update GUI.
            updateViews();
            synchronized (LoopbackCommunicationAdapter.this) {
              MovementCommand sentCmd = getSentQueue().poll();
              // If the command queue was cleared in the meantime, the kernel
              // might be surprised to hear we executed a command we shouldn't
              // have, so we only peek() at the beginning of this method and
              // poll() here. If sentCmd is null, the queue was probably cleared
              // and we shouldn't report anything back.
              if (sentCmd != null && sentCmd.equals(curCommand)) {
                // Let the vehicle manager know we've finished this command.
                commandExecuted(curCommand);
                LoopbackCommunicationAdapter.this.notify();
              }
            }
          }
        }
      }
      catch (InterruptedException iexc) {
        throw new IllegalStateException("Unexpectedly interrupted", iexc);
      }
    }

    /**
     * Simulates the vehicle's movement. If the method parameter is null,
     * then the vehicle's state is failure and some false movement
     * must be simulated. In the other case normal step
     * movement will be simulated.
     * 
     * @param step A step
     * @throws InterruptedException If an exception occured while sumulating
     */
    private void simulateMovement(Step step)
        throws InterruptedException {
      long pathLength;
      int maxVelocity;
      String pointName;
      Orientation orientation;
      if (step.getPath() == null) {
        return;
      }
      else {
        orientation = step.getVehicleOrientation();
        pathLength = step.getPath().getLength();
        switch (orientation) {
          case BACKWARD:
            maxVelocity = step.getPath().getMaxReverseVelocity();
            break;
          default:
            maxVelocity = step.getPath().getMaxVelocity();
            break;
        }
        maxVelocity = step.getPath().getMaxVelocity();
        pointName = step.getDestinationPoint().getName();
      }
      setVehicleState(Vehicle.State.EXECUTING);
      velocityController.addWayEntry(new WayEntry(pathLength,
                                                  maxVelocity,
                                                  pointName,
                                                  orientation));
      // Advance the velocity controller by small steps until the
      // controller has processed all way entries.
      while (velocityController.hasWayEntries() && !isTerminated()) {
        final WayEntry wayEntry = velocityController.getCurrentWayEntry();
        Thread.sleep(advanceTime);
        dischargeEnergy(movementPower);
        velocityController.advanceTime(simAdvanceTime);
        final WayEntry nextWayEntry = velocityController.getCurrentWayEntry();
        if (wayEntry != nextWayEntry) {
          // Let the vehicle manager know that the vehicle has reached
          // the way entry's destination point.
          setVehiclePosition(wayEntry.getDestPointName());
        }
      }
    }

    /**
     * Simulates a operation.
     * 
     * @param operation A operation
     * @throws InterruptedException If an exception occured while simulating
     */
    private void simulateOperation(String operation)
        throws InterruptedException {
      assert operation != null;
      if (!isTerminated()) {
        log.config("Operating...");
        boolean charging = operation.equals(rechargeOperation);
        // Get additional specifications for this operation
        final OperationSpec opSpec = operationSpecs.get(operation);
        final int operatingTime;
        if (opSpec != null) {
          operatingTime = opSpec.getOperatingTime();
        }
        else {
          operatingTime = defaultOperatingTime;
        }
        // Calculate amount of power that needs to be charged per second to
        // get an energy level of 100% at the end of a charging-operation.
        double chargingPower = 0.0; // Power to charge per second 
        if (charging) {
          setVehicleState(Vehicle.State.CHARGING);
          double toCharge = energyStorage.getCapacity() - energyStorage.getEnergy();
          chargingPower = toCharge / (operatingTime / 1000.0);
        }
        else {
          setVehicleState(Vehicle.State.EXECUTING);
        }
        for (int timePassed = 0; timePassed < operatingTime && !isTerminated();
             timePassed += simAdvanceTime) {
          Thread.sleep(advanceTime);
          if (charging) {
            chargeEnergy(chargingPower);
          }
          else {
            dischargeEnergy(operationPower);
          }
          velocityController.advanceTime(simAdvanceTime);
        }
        if (opSpec != null && opSpec.changesLoadCondition()) {
          // Update load handling devices as defined by this operation
          List<LoadHandlingDevice> devices = opSpec.getLoadCondition();
          setVehicleLoadHandlingDevices(devices);
        }
      }
    }
  }
}
