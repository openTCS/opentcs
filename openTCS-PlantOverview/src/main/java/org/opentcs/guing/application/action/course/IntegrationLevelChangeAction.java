/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.application.action.course;

import com.google.inject.assistedinject.Assisted;
import java.awt.event.ActionEvent;
import java.util.Collection;
import static java.util.Objects.requireNonNull;
import javax.inject.Inject;
import javax.swing.AbstractAction;
import org.opentcs.access.KernelRuntimeException;
import org.opentcs.access.SharedKernelServicePortal;
import org.opentcs.access.SharedKernelServicePortalProvider;
import org.opentcs.data.model.Vehicle;
import org.opentcs.guing.model.elements.VehicleModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Leonard Schuengel (Fraunhofer IML)
 */
public class IntegrationLevelChangeAction
    extends AbstractAction {

  /**
   * This class's logger.
   */
  private static final Logger LOG = LoggerFactory.getLogger(IntegrationLevelChangeAction.class);
  /**
   * Resource path to the correct lables.
   */
  public static final String IGNORE_ID = "course.vehicle.integrationLevelIgnore";
  public static final String NOTICE_ID = "course.vehicle.integrationLevelNotice";
  public static final String RESPECT_ID = "course.vehicle.integrationLevelRespect";
  public static final String UTILIZE_ID = "course.vehicle.integrationLevelUtilize";

  /**
   * The vehicles to change the level of.
   */
  private final Collection<VehicleModel> vehicles;

  /**
   * Sets the level to to change the vehicles to.
   */
  private final Vehicle.IntegrationLevel level;
  /**
   * Provides access to a portal.
   */
  private final SharedKernelServicePortalProvider portalProvider;

  /**
   * Creates a new instance.
   *
   * @param vehicles The selected vehicles.
   * @param level The level to to change the vehicles to.
   * @param portalProvider Provides access to a shared portal.
   */
  @Inject
  public IntegrationLevelChangeAction(@Assisted Collection<VehicleModel> vehicles,
                                      @Assisted Vehicle.IntegrationLevel level,
                                      SharedKernelServicePortalProvider portalProvider) {
    this.vehicles = requireNonNull(vehicles, "vehicles");
    this.level = requireNonNull(level, "level");
    this.portalProvider = requireNonNull(portalProvider, "portalProvider");
  }

  @Override
  public void actionPerformed(ActionEvent evt) {
    try (SharedKernelServicePortal sharedPortal = portalProvider.register()) {

      for (VehicleModel vehicle : vehicles) {
        sharedPortal.getPortal().getVehicleService().updateVehicleIntegrationLevel(
            vehicle.getVehicle().getReference(), level);
      }

    }
    catch (KernelRuntimeException e) {
      LOG.warn("Unexpected exception", e);
    }
  }
}
