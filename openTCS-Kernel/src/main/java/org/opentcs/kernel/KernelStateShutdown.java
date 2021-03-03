/*
 * openTCS copyright information:
 * Copyright (c) 2012 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.kernel;

import javax.inject.Inject;
import org.opentcs.access.Kernel;
import org.opentcs.kernel.workingset.MessageBuffer;
import org.opentcs.kernel.workingset.Model;
import org.opentcs.kernel.workingset.TCSObjectPool;

/**
 * This class implements the standard openTCS kernel when it's shut down.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
final class KernelStateShutdown
    extends KernelState {

  /**
   * Creates a new StandardKernelShutdownState.
   *
   * @param kernel The kernel.
   * @param objectPool The object pool to be used.
   * @param messageBuffer The message buffer to be used.
   */
  @Inject
  public KernelStateShutdown(StandardKernel kernel,
                             @GlobalKernelSync Object globalSyncObject,
                             TCSObjectPool objectPool,
                             Model model,
                             MessageBuffer messageBuffer) {
    super(kernel,
          globalSyncObject,
          objectPool,
          model,
          messageBuffer);
  }

  // Methods that HAVE to be implemented/overridden start here.
  @Override
  public void initialize() {
    // Do nada.
  }

  @Override
  public void terminate() {
    // Do nada.
  }

  @Override
  public Kernel.State getState() {
    return Kernel.State.SHUTDOWN;
  }
}
