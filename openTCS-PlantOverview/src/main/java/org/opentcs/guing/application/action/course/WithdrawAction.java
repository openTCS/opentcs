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
import org.opentcs.access.SharedKernelServicePortal;
import org.opentcs.access.SharedKernelServicePortalProvider;
import org.opentcs.components.kernel.services.VehicleService;
import org.opentcs.data.TCSObjectReference;
import org.opentcs.data.model.Vehicle;
import org.opentcs.guing.model.elements.VehicleModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public class WithdrawAction
    extends AbstractAction {

  /**
   * Withdraws the current transport order from a vehicle.
   */
  public static final String ID = "course.vehicle.withdrawTransportOrder";
  /**
   * This class's logger.
   */
  private static final Logger LOG = LoggerFactory.getLogger(WithdrawAction.class);
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
  public WithdrawAction(@Assisted VehicleModel vehicle,
                        SharedKernelServicePortalProvider portalProvider) {
    this.vehicleModel = requireNonNull(vehicle, "vehicle");
    this.portalProvider = requireNonNull(portalProvider, "portalProvider");
  }

  @Override
  public void actionPerformed(ActionEvent evt) {
    try (SharedKernelServicePortal sharedPortal = portalProvider.register()) {
      sharedPortal.getPortal().getDispatcherService().withdrawByVehicle(
          vehicleReference(sharedPortal.getPortal().getVehicleService()), false);
    }
    catch (KernelRuntimeException e) {
      LOG.warn("Unexpected exception", e);
    }
  }

  private TCSObjectReference<Vehicle> vehicleReference(VehicleService vehicleService) {
    return vehicleService.fetchObject(Vehicle.class, vehicleModel.getName()).getReference();
  }

}
