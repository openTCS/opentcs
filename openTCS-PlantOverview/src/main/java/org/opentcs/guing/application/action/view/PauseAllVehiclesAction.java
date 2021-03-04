/*
 * openTCS copyright information:
 * Copyright (c) 2013 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.application.action.view;

import java.awt.event.ActionEvent;
import static java.util.Objects.requireNonNull;
import javax.inject.Inject;
import javax.swing.AbstractAction;
import org.opentcs.access.KernelServicePortal;
import org.opentcs.access.SharedKernelServicePortal;
import org.opentcs.access.SharedKernelServicePortalProvider;
import org.opentcs.components.kernel.services.ServiceUnavailableException;
import org.opentcs.drivers.vehicle.messages.SetSpeedMultiplier;
import org.opentcs.guing.model.ModelComponent;
import org.opentcs.guing.model.ModelManager;
import org.opentcs.guing.model.SystemModel;
import org.opentcs.guing.model.elements.VehicleModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Action for pausing all vehicles.
 *
 * @author Preity Gupta (Fraunhofer IML)
 * @author Stefan Walter (Fraunhofer IML)
 */
public class PauseAllVehiclesAction
    extends AbstractAction {

  /**
   * This action's ID.
   */
  public final static String ID = "openTCS.pauseAllVehicles";
  /**
   * This class's logger.
   */
  private static final Logger LOG = LoggerFactory.getLogger(PauseAllVehiclesAction.class);
  /**
   * Provides the current system model.
   */
  private final ModelManager modelManager;
  /**
   * Provides access to a portal.
   */
  private final SharedKernelServicePortalProvider portalProvider;
  /**
   * Whether the vehicles are currently paused or not.
   */
  private boolean paused;

  /**
   * Creates a new instance.
   *
   * @param modelManager Provides the current system model.
   * @param portalProvider Provides access to a portal.
   */
  @Inject
  public PauseAllVehiclesAction(ModelManager modelManager,
                                SharedKernelServicePortalProvider portalProvider) {
    this.modelManager = requireNonNull(modelManager, "modelManager");
    this.portalProvider = requireNonNull(portalProvider, "portalProvider");
  }

  @Override
  public void actionPerformed(ActionEvent evt) {
    try (SharedKernelServicePortal sharedPortal = portalProvider.register()) {
      paused = !paused;
      setVehicleSpeedMultiplier(paused ? 0 : 100, sharedPortal.getPortal());
    }
    catch (ServiceUnavailableException exc) {
      LOG.warn("Could not connect to kernel", exc);
    }
  }

  private void setVehicleSpeedMultiplier(int speed, KernelServicePortal portal) {
    if (portal == null) {
      return;
    }
    LOG.debug("Limiting to {}", speed);
    ModelComponent folder
        = modelManager.getModel().getMainFolder(SystemModel.FolderKey.VEHICLES);

    if (portalProvider.portalShared()) {
      for (ModelComponent component : folder.getChildComponents()) {
        VehicleModel vModel = (VehicleModel) component;
        portal.getVehicleService().sendCommAdapterMessage(vModel.getVehicle().getReference(),
                                                          new SetSpeedMultiplier(speed));
      }
    }
  }
}
