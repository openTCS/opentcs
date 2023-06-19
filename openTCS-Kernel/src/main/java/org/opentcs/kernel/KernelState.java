/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.kernel;

import static java.util.Objects.requireNonNull;
import org.opentcs.access.Kernel.State;
import org.opentcs.components.Lifecycle;
import org.opentcs.kernel.persistence.ModelPersister;
import org.opentcs.kernel.workingset.PlantModelManager;

/**
 * The abstract base class for classes that implement state specific kernel
 * behaviour.
 */
public abstract class KernelState
    implements Lifecycle {

  /**
   * A global object to be used for synchronization within the kernel.
   */
  private final Object globalSyncObject;
  /**
   * The model facade to the object pool.
   */
  private final PlantModelManager plantModelManager;
  /**
   * The persister loading and storing model data.
   */
  private final ModelPersister modelPersister;

  /**
   * Creates a new state.
   *
   * @param globalSyncObject The kernel threads' global synchronization object.
   * @param plantModelManager The plant model manager to be used.
   * @param modelPersister The model persister to be used.
   */
  public KernelState(Object globalSyncObject,
                     PlantModelManager plantModelManager,
                     ModelPersister modelPersister) {
    this.globalSyncObject = requireNonNull(globalSyncObject, "globalSyncObject");
    this.plantModelManager = requireNonNull(plantModelManager, "plantModelManager");
    this.modelPersister = requireNonNull(modelPersister, "modelPersister");
  }

  /**
   * Returns the current state.
   *
   * @return The current state.
   */
  public abstract State getState();

  protected Object getGlobalSyncObject() {
    return globalSyncObject;
  }

  protected ModelPersister getModelPersister() {
    return modelPersister;
  }

  protected PlantModelManager getPlantModelManager() {
    return plantModelManager;
  }
}
