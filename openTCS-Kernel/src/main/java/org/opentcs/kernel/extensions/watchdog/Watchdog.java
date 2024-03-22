/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.kernel.extensions.watchdog;

import static java.util.Objects.requireNonNull;
import javax.inject.Inject;
import org.opentcs.components.kernel.KernelExtension;

/**
 * A kernel extension to periodicly monitor the state of the kernel with check tasks.
 */
public class Watchdog
    implements KernelExtension {

  /**
   * Whether this kernel extension is initialized.
   */
  private boolean initialized;
  /**
   * The task to check for consistency of blocks.
   */
  private final BlockConsistencyCheck blockCheck;

  /**
   * Creates a new instance.
   *
   * @param blockCheck The block check task.
   */
  @Inject
  public Watchdog(BlockConsistencyCheck blockCheck) {
    this.blockCheck = requireNonNull(blockCheck, "blockCheck");
  }

  @Override
  public void initialize() {
    if (isInitialized()) {
      return;
    }

    blockCheck.initialize();
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
    initialized = false;
  }

}
