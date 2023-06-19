/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.kernel;

import org.opentcs.kernel.persistence.ModelPersister;
import org.opentcs.kernel.workingset.PlantModelManager;

/**
 * The base class for the kernel's online states.
 */
public abstract class KernelStateOnline
    extends KernelState {

  /**
   * Whether to save the model when this state is terminated.
   */
  private final boolean saveModelOnTerminate;

  /**
   * Creates a new instance.
   *
   * @param globalSyncObject The kernel threads' global synchronization object.
   * @param plantModelManager The plant model manager to be used.
   * @param modelPersister The model persister to be used.
   * @param saveModelOnTerminate Whether to save the model when this state is terminated.
   */
  public KernelStateOnline(Object globalSyncObject,
                           PlantModelManager plantModelManager,
                           ModelPersister modelPersister,
                           boolean saveModelOnTerminate) {
    super(globalSyncObject, plantModelManager, modelPersister);
    this.saveModelOnTerminate = saveModelOnTerminate;
  }

  @Override
  public void terminate() {
    if (saveModelOnTerminate) {
      savePlantModel();
    }
  }

  private void savePlantModel()
      throws IllegalStateException {
    synchronized (getGlobalSyncObject()) {
      getModelPersister().saveModel(getPlantModelManager().createPlantModelCreationTO());
    }
  }
}
