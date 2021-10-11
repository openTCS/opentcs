/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.kernel;

import javax.inject.Inject;
import org.opentcs.access.Kernel;
import org.opentcs.customizations.kernel.GlobalSyncObject;
import org.opentcs.kernel.persistence.ModelPersister;
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
   * Indicates whether this component is enabled.
   */
  private boolean initialized;

  /**
   * Creates a new StandardKernelShutdownState.
   *
   * @param globalSyncObject The kernel threads' global synchronization object
   * @param objectPool The object pool to be used.
   * @param model The model to be used.
   * @param modelPersister The model persister to be used.
   */
  @Inject
  public KernelStateShutdown(@GlobalSyncObject Object globalSyncObject,
                             TCSObjectPool objectPool,
                             Model model,
                             ModelPersister modelPersister) {
    super(globalSyncObject,
          objectPool,
          model,
          modelPersister);
  }

  // Methods that HAVE to be implemented/overridden start here.
  @Override
  public void initialize() {
    initialized = true;
  }

  @Override
  public boolean isInitialized() {
    return initialized;
  }

  @Override
  public void terminate() {
    initialized = false;
  }

  @Override
  public Kernel.State getState() {
    return Kernel.State.SHUTDOWN;
  }
}
