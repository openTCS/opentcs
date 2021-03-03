/*
 * openTCS copyright information:
 * Copyright (c) 2014 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.kernel;

import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.opentcs.data.ObjectUnknownException;
import org.opentcs.data.TCSObjectReference;
import org.opentcs.data.model.visualization.ViewBookmark;
import org.opentcs.data.model.visualization.VisualLayout;
import org.opentcs.kernel.workingset.MessageBuffer;
import org.opentcs.kernel.workingset.Model;
import org.opentcs.kernel.workingset.TCSObjectPool;

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
  private static final Logger log
      = Logger.getLogger(KernelStateOnline.class.getName());
  /**
   * Whether to save the model when this state is terminated.
   */
  private final boolean saveModelOnTerminate;

  /**
   * Creates a new instance.
   *
   * @param kernel The kernel.
   * @param globalSyncObject The kernel threads' global synchronization object.
   * @param objectPool The object pool to be used.
   * @param model The model to be used.
   * @param messageBuffer The message buffer to be used.
   * @param saveModelOnTerminate Whether to save the model when this state is
   * terminated.
   */
  public KernelStateOnline(StandardKernel kernel,
                           Object globalSyncObject,
                           TCSObjectPool objectPool,
                           Model model,
                           MessageBuffer messageBuffer,
                           boolean saveModelOnTerminate) {
    super(kernel, globalSyncObject, objectPool, model, messageBuffer);
    this.saveModelOnTerminate = saveModelOnTerminate;
  }

  @Override
  public void terminate() {
    if (saveModelOnTerminate) {
      try {
        saveModel(null);
      }
      catch (IOException exc) {
        log.log(Level.WARNING, "Could not save model on termination", exc);
      }
    }
  }

  @Override
  public void saveModel(String modelName)
      throws IOException {
    synchronized (globalSyncObject) {
      kernel.modelPersister.saveModel(model, modelName);
    }
  }

  @Override
  public void setVisualLayoutViewBookmarks(TCSObjectReference<VisualLayout> ref,
                                           List<ViewBookmark> bookmarks)
      throws ObjectUnknownException {
    synchronized (globalSyncObject) {
      model.setVisualLayoutViewBookmarks(ref, bookmarks);
    }
  }

}
