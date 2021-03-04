/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.kernel;

import java.io.IOException;
import java.util.List;
import org.opentcs.data.ObjectUnknownException;
import org.opentcs.data.TCSObjectReference;
import org.opentcs.data.model.visualization.VisualLayout;
import org.opentcs.kernel.persistence.ModelPersister;
import org.opentcs.kernel.workingset.Model;
import org.opentcs.kernel.workingset.NotificationBuffer;
import org.opentcs.kernel.workingset.TCSObjectPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The base class for the kernel's online states.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
abstract class KernelStateOnline
    extends KernelState {

  /**
   * This class's logger.
   */
  private static final Logger LOG = LoggerFactory.getLogger(KernelStateOnline.class);
  /**
   * Whether to save the model when this state is terminated.
   */
  private final boolean saveModelOnTerminate;

  /**
   * Creates a new instance.
   *
   * @param globalSyncObject The kernel threads' global synchronization object.
   * @param objectPool The object pool to be used.
   * @param model The model to be used.
   * @param messageBuffer The message buffer to be used.
   * @param modelPersister The model persister to be used.
   * @param saveModelOnTerminate Whether to save the model when this state is terminated.
   */
  public KernelStateOnline(Object globalSyncObject,
                           TCSObjectPool objectPool,
                           Model model,
                           NotificationBuffer messageBuffer,
                           ModelPersister modelPersister,
                           boolean saveModelOnTerminate) {
    super(globalSyncObject, objectPool, model, messageBuffer, modelPersister);
    this.saveModelOnTerminate = saveModelOnTerminate;
  }

  @Override
  public void terminate() {
    if (saveModelOnTerminate) {
      savePlantModel();
    }
  }

  @Override
  @Deprecated
  public void savePlantModel()
      throws IllegalStateException {
    synchronized (getGlobalSyncObject()) {
      getModelPersister().saveModel(getModel(), getModel().getName());
    }
  }

  @Override
  @Deprecated
  public void saveModel(String modelName)
      throws IOException {
    synchronized (getGlobalSyncObject()) {
      getModelPersister().saveModel(getModel(), modelName);
    }
  }

  @Override
  @Deprecated
  public void setVisualLayoutViewBookmarks(
      TCSObjectReference<VisualLayout> ref,
      List<org.opentcs.data.model.visualization.ViewBookmark> bookmarks)
      throws ObjectUnknownException {
    synchronized (getGlobalSyncObject()) {
      getModel().setVisualLayoutViewBookmarks(ref, bookmarks);
    }
  }

}
