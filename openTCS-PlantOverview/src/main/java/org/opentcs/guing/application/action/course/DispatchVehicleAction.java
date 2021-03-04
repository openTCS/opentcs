/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.application.action.course;

import java.awt.event.ActionEvent;
import static java.util.Objects.requireNonNull;
import javax.inject.Inject;
import javax.swing.AbstractAction;
import org.opentcs.access.KernelRuntimeException;
import org.opentcs.access.KernelServicePortal;
import org.opentcs.access.SharedKernelServicePortal;
import org.opentcs.access.SharedKernelServicePortalProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public class DispatchVehicleAction
    extends AbstractAction {

  /**
   * Dispatches the vehicle.
   */
  public static final String ID = "course.vehicle.dispatchVehicle";
  /**
   * This class's logger.
   */
  private static final Logger LOG = LoggerFactory.getLogger(DispatchVehicleAction.class);
  /**
   * Provides access to a portal.
   */
  private final SharedKernelServicePortalProvider portalProvider;

  /**
   * Creates a new instance.
   *
   * @param portalProvider Provides access to a shared portal.
   */
  @Inject
  public DispatchVehicleAction(SharedKernelServicePortalProvider portalProvider) {
    this.portalProvider = requireNonNull(portalProvider, "portalProvider");
  }

  @Override
  public void actionPerformed(ActionEvent evt) {
    try (SharedKernelServicePortal sharedPortal = portalProvider.register()) {
      KernelServicePortal portal = sharedPortal.getPortal();
      portal.getDispatcherService().dispatch();
    }
    catch (KernelRuntimeException e) {
      LOG.warn("Unexpected exception", e);
    }
  }
}
