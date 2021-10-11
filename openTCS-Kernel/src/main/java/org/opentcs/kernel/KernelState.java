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
import org.opentcs.kernel.workingset.Model;
import org.opentcs.kernel.workingset.TCSObjectPool;

/**
 * The abstract base class for classes that implement state specific kernel
 * behaviour.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
abstract class KernelState
    implements Lifecycle {

  /**
   * A global object to be used for synchronization within the kernel.
   */
  private final Object globalSyncObject;
  /**
   * The container of all course model and transport order objects.
   */
  private final TCSObjectPool globalObjectPool;
  /**
   * The model facade to the object pool.
   */
  private final Model model;
  /**
   * The persister loading and storing model data.
   */
  private final ModelPersister modelPersister;

  /**
   * Creates a new state.
   *
   * @param globalSyncObject The kernel threads' global synchronization object.
   * @param objectPool The object pool to be used.
   * @param model The model to be used.
   * @param modelPersister The model persister to be used.
   */
  KernelState(Object globalSyncObject,
              TCSObjectPool objectPool,
              Model model,
              ModelPersister modelPersister) {
    this.globalSyncObject = requireNonNull(globalSyncObject, "globalSyncObject");
    this.globalObjectPool = requireNonNull(objectPool, "objectPool");
    this.model = requireNonNull(model, "model");
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

  protected TCSObjectPool getGlobalObjectPool() {
    return globalObjectPool;
  }

  protected Model getModel() {
    return model;
  }
}
