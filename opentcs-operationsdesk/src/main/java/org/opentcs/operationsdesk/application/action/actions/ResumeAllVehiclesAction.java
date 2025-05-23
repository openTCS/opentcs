// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.operationsdesk.application.action.actions;

import static java.util.Objects.requireNonNull;
import static org.opentcs.operationsdesk.util.I18nPlantOverviewOperating.TOOLBAR_PATH;

import jakarta.inject.Inject;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import org.opentcs.access.KernelServicePortal;
import org.opentcs.access.SharedKernelServicePortal;
import org.opentcs.access.SharedKernelServicePortalProvider;
import org.opentcs.components.kernel.services.ServiceUnavailableException;
import org.opentcs.guing.base.model.ModelComponent;
import org.opentcs.guing.base.model.elements.VehicleModel;
import org.opentcs.guing.common.model.SystemModel;
import org.opentcs.guing.common.persistence.ModelManager;
import org.opentcs.guing.common.util.ImageDirectory;
import org.opentcs.thirdparty.guing.common.jhotdraw.util.ResourceBundleUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Action for resuming all vehicles.
 */
public class ResumeAllVehiclesAction
    extends
      AbstractAction {

  /**
   * This action's ID.
   */
  public static final String ID = "openTCS.resumeAllVehicles";

  private static final ResourceBundleUtil BUNDLE = ResourceBundleUtil.getBundle(TOOLBAR_PATH);
  /**
   * This class's logger.
   */
  private static final Logger LOG = LoggerFactory.getLogger(ResumeAllVehiclesAction.class);
  /**
   * Provides the current system model.
   */
  private final ModelManager modelManager;
  /**
   * Provides access to a portal.
   */
  private final SharedKernelServicePortalProvider portalProvider;

  /**
   * Creates a new instance.
   *
   * @param modelManager Provides the current system model.
   * @param portalProvider Provides access to a portal.
   */
  @Inject
  @SuppressWarnings("this-escape")
  public ResumeAllVehiclesAction(
      ModelManager modelManager,
      SharedKernelServicePortalProvider portalProvider
  ) {
    this.modelManager = requireNonNull(modelManager, "modelManager");
    this.portalProvider = requireNonNull(portalProvider, "portalProvider");

    putValue(NAME, BUNDLE.getString("resumeAllVehiclesAction.name"));
    putValue(SHORT_DESCRIPTION, BUNDLE.getString("resumeAllVehiclesAction.shortDescription"));

    ImageIcon iconSmall = ImageDirectory.getImageIcon("/toolbar/resume-vehicles.16.png");
    ImageIcon iconLarge = ImageDirectory.getImageIcon("/toolbar/resume-vehicles.22.png");
    putValue(SMALL_ICON, iconSmall);
    putValue(LARGE_ICON_KEY, iconLarge);
  }

  @Override
  public void actionPerformed(ActionEvent evt) {
    try (SharedKernelServicePortal sharedPortal = portalProvider.register()) {
      resumeVehicles(sharedPortal.getPortal());
    }
    catch (ServiceUnavailableException exc) {
      LOG.warn("Could not connect to kernel", exc);
    }
  }

  private void resumeVehicles(KernelServicePortal portal) {
    ModelComponent folder = modelManager.getModel().getMainFolder(SystemModel.FolderKey.VEHICLES);

    for (ModelComponent component : folder.getChildComponents()) {
      VehicleModel vModel = (VehicleModel) component;
      LOG.info("Resuming vehicle {}...", vModel.getVehicle().getName());
      portal.getVehicleService().updateVehiclePaused(vModel.getVehicle().getReference(), false);
    }
  }
}
