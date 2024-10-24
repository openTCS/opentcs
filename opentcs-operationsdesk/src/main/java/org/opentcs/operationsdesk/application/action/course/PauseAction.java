// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.operationsdesk.application.action.course;

import static java.util.Objects.requireNonNull;
import static org.opentcs.operationsdesk.util.I18nPlantOverviewOperating.VEHICLEPOPUP_PATH;

import com.google.inject.assistedinject.Assisted;
import jakarta.inject.Inject;
import java.awt.event.ActionEvent;
import java.util.Collection;
import javax.swing.AbstractAction;
import org.opentcs.access.KernelRuntimeException;
import org.opentcs.access.SharedKernelServicePortal;
import org.opentcs.access.SharedKernelServicePortalProvider;
import org.opentcs.guing.base.model.elements.VehicleModel;
import org.opentcs.thirdparty.guing.common.jhotdraw.util.ResourceBundleUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 */
public class PauseAction
    extends
      AbstractAction {

  /**
   * This class's logger.
   */
  private static final Logger LOG = LoggerFactory.getLogger(PauseAction.class);
  /**
   * This instance's resource bundle.
   */
  private final ResourceBundleUtil bundle = ResourceBundleUtil.getBundle(VEHICLEPOPUP_PATH);
  /**
   * The vehicles to change the level of.
   */
  private final Collection<VehicleModel> vehicles;
  /**
   * Indicates whether to pause or unpause the vehicles.
   */
  private final boolean pause;
  /**
   * Provides access to a portal.
   */
  private final SharedKernelServicePortalProvider portalProvider;

  /**
   * Creates a new instance.
   *
   * @param vehicles The selected vehicles.
   * @param pause The paused state to set the vehicles to.
   * @param portalProvider Provides access to a shared portal.
   */
  @Inject
  @SuppressWarnings("this-escape")
  public PauseAction(
      @Assisted
      Collection<VehicleModel> vehicles,
      @Assisted
      boolean pause,
      SharedKernelServicePortalProvider portalProvider
  ) {
    this.vehicles = requireNonNull(vehicles, "vehicles");
    this.pause = requireNonNull(pause, "pause");
    this.portalProvider = requireNonNull(portalProvider, "portalProvider");

    putValue(
        NAME,
        pause
            ? bundle.getString("pauseAction.pause.name")
            : bundle.getString("pauseAction.resume.name")
    );
  }

  @Override
  public void actionPerformed(ActionEvent evt) {
    try (SharedKernelServicePortal sharedPortal = portalProvider.register()) {

      for (VehicleModel vehicle : vehicles) {
        sharedPortal.getPortal().getVehicleService().updateVehiclePaused(
            vehicle.getVehicle().getReference(), pause
        );
      }

    }
    catch (KernelRuntimeException e) {
      LOG.warn("Unexpected exception", e);
    }
  }
}
