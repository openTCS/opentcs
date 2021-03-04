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
import static java.util.Objects.requireNonNull;
import javax.inject.Inject;
import javax.swing.AbstractAction;
import org.opentcs.access.KernelRuntimeException;
import org.opentcs.access.KernelServicePortal;
import org.opentcs.access.SharedKernelServicePortal;
import org.opentcs.access.SharedKernelServicePortalProvider;
import org.opentcs.data.TCSObjectReference;
import org.opentcs.data.model.Vehicle;
import org.opentcs.guing.model.elements.VehicleModel;
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
   * The vehicle.
   */
  private final VehicleModel vehicleModel;
  /**
   * Provides access to a portal.
   */
  private final SharedKernelServicePortalProvider portalProvider;

  /**
   * Creates a new instance.
   *
   * @param vehicle The selected vehicle.
   * @param portalProvider Provides access to a shared portal.
   */
  @Inject
  public DispatchVehicleAction(@Assisted VehicleModel vehicle,
                               SharedKernelServicePortalProvider portalProvider) {
    this.vehicleModel = requireNonNull(vehicle, "vehicle");
    this.portalProvider = requireNonNull(portalProvider, "portalProvider");
  }

  @Override
  public void actionPerformed(ActionEvent evt) {
    try (SharedKernelServicePortal sharedPortal = portalProvider.register()) {
      KernelServicePortal portal = sharedPortal.getPortal();
      TCSObjectReference<Vehicle> vehicleRef = portal.getVehicleService()
          .fetchObject(Vehicle.class, vehicleModel.getName()).getReference();
      portal.getVehicleService().updateVehicleIntegrationLevel(
          vehicleRef,
          Vehicle.IntegrationLevel.TO_BE_UTILIZED);
      portal.getDispatcherService().dispatch();
    }
    catch (KernelRuntimeException e) {
      LOG.warn("Unexpected exception", e);
    }
  }
}
