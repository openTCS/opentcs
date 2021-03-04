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
import org.opentcs.access.KernelServicePortal;
import org.opentcs.access.SharedKernelServicePortal;
import org.opentcs.access.SharedKernelServicePortalProvider;
import org.opentcs.components.kernel.services.ServiceUnavailableException;
import org.opentcs.data.model.Path;
import org.opentcs.guing.application.ApplicationState;
import org.opentcs.guing.application.OperationMode;
import org.opentcs.guing.components.properties.event.AttributesChangeEvent;
import org.opentcs.guing.components.properties.event.AttributesChangeListener;
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
   * Provides access to a portal.
   */
  private final SharedKernelServicePortalProvider portalProvider;
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
   * @param portalProvider A portal provider.
   * @param applicationState Keeps the plant overview's state.
   * @param model The path model.
   */
  public PathLockAdapter(SharedKernelServicePortalProvider portalProvider,
                         ApplicationState applicationState,
                         PathModel model) {
    this.portalProvider = requireNonNull(portalProvider, "portalProvider");
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
    return (Boolean) model.getPropertyLocked().getValue();
  }

  private void updateLockInKernel(boolean locked) {
    try (SharedKernelServicePortal sharedPortal = portalProvider.register()) {
      KernelServicePortal portal = sharedPortal.getPortal();
      // Check if the kernel is in operating mode, too.
      if (portal.getState() == Kernel.State.OPERATING) {
        // Update the path in the kernel if it exists and its locked state is different.
        Path path = portal.getPlantModelService().fetchObject(Path.class, model.getName());
        if (path != null && path.isLocked() != locked) {
          portal.getRouterService().updatePathLock(path.getReference(), locked);
          portal.getRouterService().updateRoutingTopology();
        }
      }

    }
    catch (ServiceUnavailableException exc) {
      LOG.warn("Could not connect to kernel", exc);
    }
  }

}
