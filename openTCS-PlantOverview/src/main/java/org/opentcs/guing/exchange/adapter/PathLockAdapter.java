/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.exchange.adapter;

import static java.util.Objects.requireNonNull;
import org.opentcs.access.Kernel;
import org.opentcs.access.SharedKernelProvider;
import org.opentcs.data.model.Path;
import org.opentcs.guing.application.ApplicationState;
import org.opentcs.guing.application.OperationMode;
import org.opentcs.guing.components.properties.event.AttributesChangeEvent;
import org.opentcs.guing.components.properties.event.AttributesChangeListener;
import org.opentcs.guing.components.properties.type.BooleanProperty;
import org.opentcs.guing.model.elements.PathModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Updates a path's lock state with the kernel when it changes.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public class PathLockAdapter
    implements AttributesChangeListener {

  /**
   * This class's logger.
   */
  private static final Logger LOG = LoggerFactory.getLogger(PathLockAdapter.class);
  /**
   * The path model.
   */
  private final PathModel model;
  /**
   * Provides access to a kernel.
   */
  private final SharedKernelProvider kernelProvider;
  /**
   * The state of the plant overview.
   */
  private final ApplicationState applicationState;
  /**
   * Indicates whether the path was locked the last time we checked.
   */
  private boolean lockedPreviously;

  /**
   * Creates a new instance.
   *
   * @param kernelProvider A kernel provider.
   * @param applicationState Keeps the plant overview's state.
   * @param model The path model.
   */
  public PathLockAdapter(SharedKernelProvider kernelProvider,
                         ApplicationState applicationState,
                         PathModel model) {
    this.kernelProvider = requireNonNull(kernelProvider, "kernelProvider");
    this.applicationState = requireNonNull(applicationState, "applicationState");
    this.model = requireNonNull(model, "model");
    this.lockedPreviously = isPathLocked();
  }

  @Override
  public void propertiesChanged(AttributesChangeEvent e) {
    if (e.getModel() != model) {
      return;
    }
    if (applicationState.getOperationMode() != OperationMode.OPERATING) {
      LOG.debug("Ignoring PathLockedEvent because the application is not in operating mode.");
      return;
    }

    boolean locked = isPathLocked();
    if (locked == lockedPreviously) {
      return;
    }
    lockedPreviously = locked;

    new Thread(() -> updateLockInKernel(locked)).start();
  }

  private boolean isPathLocked() {
    BooleanProperty locked = (BooleanProperty) model.getProperty(PathModel.LOCKED);
    return (Boolean) locked.getValue();
  }

  private void updateLockInKernel(boolean locked) {
    Object localKernelClient = new Object();
    try {
      // Try to connect to the kernel.
      kernelProvider.register(localKernelClient);
      if (kernelProvider.kernelShared()) {
        Kernel kernel = kernelProvider.getKernel();
        // Check if the kernel is in operating mode, too.
        if (kernel.getState() == Kernel.State.OPERATING) {
          // Update the path in the kernel if it exists and its locked state is different.
          Path path = kernel.getTCSObject(Path.class, model.getName());
          if (path != null && path.isLocked() != locked) {
            kernel.setPathLocked(path.getReference(), locked);
            kernel.updateRoutingTopology();
          }
        }
      }
    }
    finally {
      kernelProvider.unregister(localKernelClient);
    }
  }

}
