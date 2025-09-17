// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.kernel.extensions.watchdog;

import static java.util.Objects.requireNonNull;

import jakarta.inject.Inject;
import org.opentcs.components.kernel.KernelExtension;

/**
 * A kernel extension to periodicly monitor the state of the kernel with check tasks.
 */
public class Watchdog
    implements
      KernelExtension {

  /**
   * Whether this kernel extension is initialized.
   */
  private boolean initialized;
  /**
   * The task to check for consistency of blocks.
   */
  private final BlockConsistencyCheck blockCheck;
  /**
   * The task to check for stranded vehicles.
   */
  private final StrandedVehicleCheck strandedVehicleCheck;
  /**
   * The task to check for idle and expired transport orders.
   */
  private final TransportOrderCheck transportOrderCheck;
  /**
   * The task to log the heartbeat for the kernel.
   */
  private final KernelHeartbeatLogger kernelHeartbeatLogger;

  /**
   * Creates a new instance.
   *
   * @param blockCheck The block check task.
   * @param strandedVehicleCheck The stranded vehicle check task.
   * @param transportOrderCheck The transport order check task.
   * @param kernelHeartbeatLogger The kernel heartbeat logger.
   */
  @Inject
  public Watchdog(
      BlockConsistencyCheck blockCheck,
      StrandedVehicleCheck strandedVehicleCheck,
      TransportOrderCheck transportOrderCheck,
      KernelHeartbeatLogger kernelHeartbeatLogger
  ) {
    this.blockCheck = requireNonNull(blockCheck, "blockCheck");
    this.strandedVehicleCheck = requireNonNull(strandedVehicleCheck, "strandedVehicleCheck");
    this.transportOrderCheck = requireNonNull(transportOrderCheck, "transportOrderCheck");
    this.kernelHeartbeatLogger = requireNonNull(kernelHeartbeatLogger, "kernelHeartbeatLogger");
  }

  @Override
  public void initialize() {
    if (isInitialized()) {
      return;
    }

    blockCheck.initialize();
    strandedVehicleCheck.initialize();
    transportOrderCheck.initialize();
    kernelHeartbeatLogger.initialize();
    initialized = true;
  }

  @Override
  public boolean isInitialized() {
    return initialized;
  }

  @Override
  public void terminate() {
    if (!isInitialized()) {
      return;
    }

    blockCheck.terminate();
    strandedVehicleCheck.terminate();
    transportOrderCheck.terminate();
    kernelHeartbeatLogger.terminate();
    initialized = false;
  }

}
