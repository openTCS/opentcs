/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.strategies.basic.dispatching;

import static java.util.Objects.requireNonNull;
import javax.inject.Inject;
import org.opentcs.components.Lifecycle;
import org.opentcs.strategies.basic.dispatching.phase.AssignReservedOrdersPhase;
import org.opentcs.strategies.basic.dispatching.phase.AssignSequenceSuccessorsPhase;
import org.opentcs.strategies.basic.dispatching.phase.CheckNewOrdersPhase;
import org.opentcs.strategies.basic.dispatching.phase.FinishWithdrawalsPhase;
import org.opentcs.strategies.basic.dispatching.phase.assignment.AssignFreeOrdersPhase;
import org.opentcs.strategies.basic.dispatching.phase.assignment.AssignNextDriveOrdersPhase;
import org.opentcs.strategies.basic.dispatching.phase.parking.ParkIdleVehiclesPhase;
import org.opentcs.strategies.basic.dispatching.phase.parking.PrioritizedParkingPhase;
import org.opentcs.strategies.basic.dispatching.phase.parking.PrioritizedReparkPhase;
import org.opentcs.strategies.basic.dispatching.phase.recharging.RechargeIdleVehiclesPhase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Performs a full dispatch run.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public class FullDispatchTask
    implements Runnable,
               Lifecycle {

  /**
   * This class's logger.
   */
  private static final Logger LOG = LoggerFactory.getLogger(FullDispatchTask.class);

  private final CheckNewOrdersPhase checkNewOrdersPhase;
  private final FinishWithdrawalsPhase finishWithdrawalsPhase;
  private final AssignNextDriveOrdersPhase assignNextDriveOrdersPhase;
  private final AssignReservedOrdersPhase assignReservedOrdersPhase;
  private final AssignSequenceSuccessorsPhase assignSequenceSuccessorsPhase;
  private final AssignFreeOrdersPhase assignFreeOrdersPhase;
  private final RechargeIdleVehiclesPhase rechargeIdleVehiclesPhase;
  private final PrioritizedReparkPhase prioritizedReparkPhase;
  private final PrioritizedParkingPhase prioritizedParkingPhase;
  private final ParkIdleVehiclesPhase parkIdleVehiclesPhase;
  /**
   * Indicates whether this component is enabled.
   */
  private boolean initialized;

  @Inject
  public FullDispatchTask(CheckNewOrdersPhase checkNewOrdersPhase,
                          FinishWithdrawalsPhase finishWithdrawalsPhase,
                          AssignNextDriveOrdersPhase assignNextDriveOrdersPhase,
                          AssignReservedOrdersPhase assignReservedOrdersPhase,
                          AssignSequenceSuccessorsPhase assignSequenceSuccessorsPhase,
                          AssignFreeOrdersPhase assignFreeOrdersPhase,
                          RechargeIdleVehiclesPhase rechargeIdleVehiclesPhase,
                          PrioritizedReparkPhase prioritizedReparkPhase,
                          PrioritizedParkingPhase prioritizedParkingPhase,
                          ParkIdleVehiclesPhase parkIdleVehiclesPhase) {
    this.checkNewOrdersPhase = requireNonNull(checkNewOrdersPhase, "checkNewOrdersPhase");
    this.finishWithdrawalsPhase = requireNonNull(finishWithdrawalsPhase, "finishWithdrawalsPhase");
    this.assignNextDriveOrdersPhase = requireNonNull(assignNextDriveOrdersPhase,
                                                     "assignNextDriveOrdersPhase");
    this.assignReservedOrdersPhase = requireNonNull(assignReservedOrdersPhase,
                                                    "assignReservedOrdersPhase");
    this.assignSequenceSuccessorsPhase = requireNonNull(assignSequenceSuccessorsPhase,
                                                        "assignSequenceSuccessorsPhase");
    this.assignFreeOrdersPhase = requireNonNull(assignFreeOrdersPhase, "assignFreeOrdersPhase");
    this.rechargeIdleVehiclesPhase = requireNonNull(rechargeIdleVehiclesPhase,
                                                    "rechargeIdleVehiclesPhase");
    this.prioritizedReparkPhase = requireNonNull(prioritizedReparkPhase, "prioritizedReparkPhase");
    this.prioritizedParkingPhase = requireNonNull(prioritizedParkingPhase,
                                                  "prioritizedParkingPhase");
    this.parkIdleVehiclesPhase = requireNonNull(parkIdleVehiclesPhase, "parkIdleVehiclesPhase");
  }

  @Override
  public void initialize() {
    if (isInitialized()) {
      return;
    }

    checkNewOrdersPhase.initialize();
    finishWithdrawalsPhase.initialize();
    assignNextDriveOrdersPhase.initialize();
    assignReservedOrdersPhase.initialize();
    assignSequenceSuccessorsPhase.initialize();
    assignFreeOrdersPhase.initialize();
    rechargeIdleVehiclesPhase.initialize();
    prioritizedReparkPhase.initialize();
    prioritizedParkingPhase.initialize();
    parkIdleVehiclesPhase.initialize();

    initialized = true;
  }

  @Override
  public void terminate() {
    if (!isInitialized()) {
      return;
    }

    checkNewOrdersPhase.terminate();
    finishWithdrawalsPhase.terminate();
    assignNextDriveOrdersPhase.terminate();
    assignReservedOrdersPhase.terminate();
    assignSequenceSuccessorsPhase.terminate();
    assignFreeOrdersPhase.terminate();
    rechargeIdleVehiclesPhase.terminate();
    prioritizedReparkPhase.terminate();
    prioritizedParkingPhase.terminate();
    parkIdleVehiclesPhase.terminate();

    initialized = false;
  }

  @Override
  public boolean isInitialized() {
    return initialized;
  }

  @Override
  public final void run() {
    LOG.debug("Starting full dispatch run...");

    checkNewOrdersPhase.run();
    // Check what vehicles involved in a process should do.
    finishWithdrawalsPhase.run();
    assignNextDriveOrdersPhase.run();
    assignSequenceSuccessorsPhase.run();
    // Check what vehicles not already in a process should do.
    assignOrders();
    rechargeVehicles();
    parkVehicles();

    LOG.debug("Finished full dispatch run.");
  }

  /**
   * Assignment of orders to vehicles.
   * <p>
   * Default: Assigns reserved and then free orders to vehicles.
   * </p>
   */
  protected void assignOrders() {
    assignReservedOrdersPhase.run();
    assignFreeOrdersPhase.run();
  }

  /**
   * Recharging of vehicles.
   * <p>
   * Default: Sends idle vehicles with a degraded energy level to recharge locations.
   * </p>
   */
  protected void rechargeVehicles() {
    rechargeIdleVehiclesPhase.run();
  }

  /**
   * Parking of vehicles.
   * <p>
   * Default: Sends idle vehicles to parking positions.
   * </p>
   */
  protected void parkVehicles() {
    prioritizedReparkPhase.run();
    prioritizedParkingPhase.run();
    parkIdleVehiclesPhase.run();
  }
}
