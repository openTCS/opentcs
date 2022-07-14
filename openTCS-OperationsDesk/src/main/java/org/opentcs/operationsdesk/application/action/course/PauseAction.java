/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.operationsdesk.application.action.course;

import com.google.inject.assistedinject.Assisted;
import java.awt.event.ActionEvent;
import java.util.Collection;
import static java.util.Objects.requireNonNull;
import javax.inject.Inject;
import javax.swing.AbstractAction;
import org.opentcs.access.KernelRuntimeException;
import org.opentcs.access.SharedKernelServicePortal;
import org.opentcs.access.SharedKernelServicePortalProvider;
import org.opentcs.guing.base.model.elements.VehicleModel;
import static org.opentcs.operationsdesk.util.I18nPlantOverviewOperating.VEHICLEPOPUP_PATH;
import org.opentcs.thirdparty.guing.common.jhotdraw.util.ResourceBundleUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public class PauseAction
    extends AbstractAction {

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
  public PauseAction(@Assisted Collection<VehicleModel> vehicles,
                     @Assisted boolean pause,
                     SharedKernelServicePortalProvider portalProvider) {
    this.vehicles = requireNonNull(vehicles, "vehicles");
    this.pause = requireNonNull(pause, "pause");
    this.portalProvider = requireNonNull(portalProvider, "portalProvider");

    putValue(NAME,
             pause
                 ? bundle.getString("pauseAction.pause.name")
                 : bundle.getString("pauseAction.resume.name"));
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
